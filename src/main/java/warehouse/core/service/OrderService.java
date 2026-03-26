package warehouse.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import warehouse.core.document.*;
import warehouse.core.document.enums.LocationTypes;
import warehouse.core.document.enums.MovementTypes;
import warehouse.core.dto.OrderDTO;
import warehouse.core.repository.OrderRepository;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class OrderService {

    OrderRepository orderRepository;

    ProductService productService;
    StockLevelService stockLevelService;
    MovementService movementService;
    LocationService locationService;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductService productService,
                        StockLevelService stockLevelService,MovementService movementService,
                        LocationService locationService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.stockLevelService = stockLevelService;
        this.movementService = movementService;
        this.locationService = locationService;
    }

    public List<OrderDTO> findAll() {
        log.info("Retrieving all orders");
        List<Order> order = orderRepository.findAll();
        return order.stream().map(Order::toDTO).toList();
    }

    public OrderDTO findById(String id) {
        log.info("Retrieving order by id {}", id);
        Order order = orderRepository.findById(id).orElse(null);
        if  (order == null) {
            return null;
        } else {
            return order.toDTO();
        }
    }

    public void save(OrderDTO orderDTO) {
        log.info("Saving order {}", orderDTO);
        Order order = orderDTO.toOrder();
        order.created();
        orderRepository.save(order);
    }

    public void deleteAll() {
        log.info("Deleting all orders");
        orderRepository.deleteAll();
    }

    public OrderDTO startPicking(String id) {
        log.info("Picking process started for order {}", id);
        Order order = findOrder(id);

        if (order == null) {
            return null;
        }

        order.startPicking();
        List<Order.OrderItem> orderItems = order.getItems();

        log.info("Resetting picked quantity for order {}", order.getId());
        for (Order.OrderItem item : orderItems) {
            item.setPickedQuantity(0);
        }

        List<String> productIds = orderItems.stream().map(Order.OrderItem::getProductSku).toList();
        List<Product> products = productService.findAllById(productIds);

        if (products.isEmpty()) {
            log.info("No products found for order {}", order.getId());
            order.cancel();
            orderRepository.save(order);
            return order.toDTO();
        }


        Map<String, List<StockLevel>> stockLevelByProductId = stockLevelService.findAllByProductId(productIds);

        if (stockLevelByProductId.isEmpty()) {
            log.info("No stock found for products: {}", productIds);
            return null;
        }

        return processPicking(stockLevelByProductId, products, order, orderItems);

    }

    private OrderDTO processPicking(Map<String, List<StockLevel>> stockLevelByProductId, List<Product> products,
                                    Order order, List<Order.OrderItem> orderItems) {

        List<StockLevel> stockLevels;
        List<Movement> movements = new ArrayList<>();
        List<StockLevel> reservedStockLevel = new ArrayList<>();

        Order.OrderItem orderItem;

        Optional<Product> product;
        Optional<Order.OrderItem> optionalOrderItem;
        Optional<StockLevel> bestSingleMatch;
        StockLevel stageStockLevel;

        int reservedTotal = 0;
        int remaining;
        int quantityToTake;


        String description = "Products for Order ::orderID::";
        description = description.replace("::orderID::", order.getId());

        Location location = new Location("loc_STAGE_" + order.getId(), LocationTypes.STAGE, description);

        for (Map.Entry<String, List<StockLevel>> entry : stockLevelByProductId.entrySet()) {
            String productSku = entry.getKey();

            product = products.stream()
                    .filter(productTemp -> productTemp.getSku().equals(productSku))
                    .findFirst();

            optionalOrderItem = orderItems.stream()
                    .filter(item -> item.getProductSku().equals(productSku))
                    .findFirst();

            stockLevels = entry.getValue();

            if (optionalOrderItem.isEmpty() || product.isEmpty()) {
                return null;
            }

            orderItem = optionalOrderItem.get();

            int requestedQuantity = orderItem.getRequestedQuantity();

            bestSingleMatch = stockLevels.stream()
                    .filter(stockLevel -> stockLevel.getQuantity() >= requestedQuantity)
                    .min(Comparator.comparingInt(StockLevel::getQuantity));

            if (bestSingleMatch.isPresent()) {
                orderItem.setPickedQuantity(requestedQuantity);

                stageStockLevel = new StockLevel(productSku, location.getCode(), requestedQuantity);

                bestSingleMatch.get().setQuantity(bestSingleMatch.get().getQuantity() - requestedQuantity);

                reservedStockLevel.add(bestSingleMatch.get());
                reservedStockLevel.add(stageStockLevel);

                Movement movement = new Movement(productSku, requestedQuantity, product.get().getUnit(),
                        bestSingleMatch.get().getLocationId(), location.getCode(), MovementTypes.INTERNAL_TRANSFER,
                        "Order", Instant.now());

                log.info("Generating movement {}", movement);
                movements.add(movement);

            } else {
                log.info("No best single match found for product {}",  productSku);
                List<StockLevel> sortedStockLevels = stockLevels.stream()
                        .filter(stockLevel -> stockLevel.getQuantity() > 0)
                        .sorted(Comparator.comparingInt(StockLevel::getQuantity).reversed())
                        .toList();

                stageStockLevel = new StockLevel(productSku, location.getCode(), 0);

                for (StockLevel stockLevel : sortedStockLevels) {
                    if (reservedTotal >= requestedQuantity) {
                        log.info("Reserved Products equals the requested quantity");
                        break;
                    }

                    remaining = requestedQuantity - reservedTotal;
                    quantityToTake = Math.min(stockLevel.getQuantity(), remaining);

                    if (quantityToTake > 0) {
                        stockLevel.setQuantity(stockLevel.getQuantity() - quantityToTake);
                        stageStockLevel.setQuantity(stageStockLevel.getQuantity() + quantityToTake);

                        reservedStockLevel.add(stockLevel);

                        Movement movement = new Movement(productSku, quantityToTake, product.get().getUnit(),
                                stockLevel.getLocationId(), location.getCode(), MovementTypes.INTERNAL_TRANSFER,
                                "Order", Instant.now());

                        log.info("Generating movement {}", movement);
                        movements.add(movement);

                        orderItem.setPickedQuantity(orderItem.getPickedQuantity() + quantityToTake);
                        reservedTotal += quantityToTake;
                    }
                    log.info("Quantity to take is zero. No more products needed");
                }

                if (stageStockLevel.getQuantity() > 0) {
                    reservedStockLevel.add(stageStockLevel);
                }
            }
        }

        log.info("All items processed. Storing information in the database");
        order.finishPicking();
        locationService.save(location);
        stockLevelService.saveOrDeleteAll(reservedStockLevel);
        movementService.saveAll(movements);
        orderRepository.save(order);
        return order.toDTO();
    }

    public OrderDTO ship(String id) {
        log.info("Shipping process for Order with id {} started", id);
        List<StockLevel> stockLevels = new ArrayList<>();
        List<Movement> movements = new ArrayList<>();
        String locationCode;
        Optional<StockLevel> optionalStockLevel;
        Optional<Product> optionalProduct;
        Order order;
        Product product;
        StockLevel stockLevel;
        Movement movement;

        order = findOrder(id);

        if (order == null) {
            return null;
        }

        locationCode = "loc_STAGE_" + order.getId();
        for (Order.OrderItem orderItem : order.getItems()) {
            log.info("Processing item {} ", orderItem.getProductSku());
            optionalStockLevel = stockLevelService.findByProductIdAndLocationId(orderItem.getProductSku(), locationCode);
            optionalProduct = productService.findById(orderItem.getProductSku());
            if (optionalStockLevel.isEmpty() || optionalProduct.isEmpty()) {
                log.warn("Either stock ({}) or product ({}) not found", optionalStockLevel.isEmpty(), optionalProduct.isEmpty());
                return null;
            }

            stockLevel = optionalStockLevel.get();
            product = optionalProduct.get();
            log.info("Stock level for location {} with product {} found", locationCode, product.getSku());
            if (stockLevel.getQuantity() != orderItem.getPickedQuantity()) {
                log.warn("Quantity in the stage location was wrong for the given order {}. Cannot proceed", order.getId());
                return order.toDTO();
            }

            stockLevels.add(stockLevel);
            movement = new Movement(product.getSku(), orderItem.getPickedQuantity(), product.getUnit(),
                    locationCode, LocationTypes.CUSTOMER.toString(), MovementTypes.OUTBOUND,
                    "Order", Instant.now());
            movements.add(movement);
        }

        log.info("All items processed. Storing information in the database");
        order.ship();
        orderRepository.save(order);
        movementService.saveAll(movements);
        stockLevelService.deleteAll(stockLevels);
        return order.toDTO();
    }

    public OrderDTO cancel(String id) {
        log.info("Cancel process for Order with id {} started", id);
        List<Movement> movements = new ArrayList<>();
        List<Order.OrderItem> newOrderItems = new ArrayList<>();
        List<StockLevel> reservedStockLevels = new ArrayList<>();
        List<String> productIds;
        List<StockLevel> stockLevels;
        List<StockLevel> sortedStockLevels;
        List<Object> response;
        Map<String, List<StockLevel>> stockLevelMapByProduct;
        String locationCode;

        Product product;
        Order.OrderItem orderItem;
        Order order;
        Movement movement;

        order = findOrder(id);

        if (order == null) {
            return null;
        }

        locationCode = "loc_STAGE_" + order.getId();
        productIds = order.getItems().stream().map(Order.OrderItem::getProductSku).toList();
        stockLevelMapByProduct = stockLevelService.findAllByProductId(productIds);

        for (Map.Entry<String, List<StockLevel>> entry : stockLevelMapByProduct.entrySet()) {
            log.info("Processing item with product id {} ", entry.getKey());
            response = findOrderItemAndProduct(entry.getKey(), order);
            if (response == null) {
                return null;
            }

            orderItem = (Order.OrderItem) response.get(0);
            product = (Product) response.get(1);

            log.info("Sorting stock levels by quantity in descending order");
            stockLevels = entry.getValue();
            sortedStockLevels = stockLevels.stream()
                    .sorted(Comparator.comparingInt(StockLevel::getQuantity).reversed())
                    .toList();
            if (!sortedStockLevels.isEmpty()) {
                StockLevel stockLevel = sortedStockLevels.getFirst();
                stockLevel.setQuantity(stockLevel.getQuantity() + orderItem.getPickedQuantity());
                reservedStockLevels.add(stockLevel);

                movement = new Movement(orderItem.getProductSku(), orderItem.getPickedQuantity(), product.getUnit(),
                        locationCode, stockLevel.getLocationId(), MovementTypes.INTERNAL_TRANSFER, "Canceled", Instant.now());

                log.info("Generating movement: {}", movement);
                orderItem.setPickedQuantity(0);
                newOrderItems.add(orderItem);
                movements.add(movement);
            }
        }

        log.info("All items processed. Storing information in the database");

        order.cancel();
        order.setItems(newOrderItems);
        stockLevelService.saveOrDeleteAll(reservedStockLevels);
        locationService.deleteById(locationCode);
        movementService.saveAll(movements);
        return orderRepository.save(order).toDTO();
    }

    public OrderDTO returnOrder(String id) {
        List<Movement> movements = new ArrayList<>();
        List<StockLevel> reservedStockLevels = new ArrayList<>();
        List<String> productIds;
        Map<String, List<StockLevel>> stockLevelMapByProduct;
        List<Object> response;

        String locationCode;
        Optional<Order> optionalOrder;
        Order.OrderItem orderItem;
        Order order;
        Product product;
        StockLevel stockLevel;
        Location location;
        Movement movement;

        optionalOrder = orderRepository.findById(id);

        if (optionalOrder.isEmpty()) {
            log.error("Order with id {} not found", id);
            return null;
        }

        order = optionalOrder.get();

        locationCode = "loc_RETURN_" + order.getId();
        location = new Location(locationCode, LocationTypes.RETURN,
                "Return location for Order " + order.getId());

        productIds = order.getItems().stream().map(Order.OrderItem::getProductSku).toList();
        stockLevelMapByProduct = stockLevelService.findAllByProductId(productIds);

        for (Map.Entry<String, List<StockLevel>> entry : stockLevelMapByProduct.entrySet()) {
            log.info("Processing item with product id {} ", entry.getKey());
            response = findOrderItemAndProduct(entry.getKey(), order);
            if (response == null) {
                return null;
            }

            orderItem = (Order.OrderItem) response.get(0);
            product = (Product) response.get(1);

            stockLevel = new StockLevel(entry.getKey(), locationCode, orderItem.getPickedQuantity());
            reservedStockLevels.add(stockLevel);
            movement = new Movement(product.getSku(), orderItem.getPickedQuantity(), product.getUnit(),
                    LocationTypes.CUSTOMER.toString(), locationCode, MovementTypes.INBOUND, "Returned", Instant.now());
            log.info("Generating movement: {}", movement);
            movements.add(movement);
        }

        log.info("All items processed. Storing information in the database");

        order.returned();
        stockLevelService.saveOrDeleteAll(reservedStockLevels);
        locationService.save(location);
        movementService.saveAll(movements);
        return orderRepository.save(order).toDTO();
    }

    private  List<Object> findOrderItemAndProduct(String id, Order order) {
        List<Object> response = new ArrayList<>();
        Optional<Product> product = productService.findById(id);
        Optional<Order.OrderItem> orderItem = order.getItems().stream().filter(item -> item.getProductSku().equals(id)).findFirst();

        if (orderItem.isEmpty() || product.isEmpty()) {
            log.warn("Either orderItem ({}) or product ({}) not found", orderItem.isEmpty(), product.isEmpty());
            return null;
        }
        response.add(orderItem.get());
        response.add(product.get());
        return response;
    }

    private Order findOrder(String id) {
        log.info("Looking for Order with id {}", id);
        Optional<Order> optionalOrder = orderRepository.findById(id);

        if (optionalOrder.isEmpty()) {
            log.error("Order with id {} not found", id);
            return null;
        }

       return optionalOrder.get();
    }
}
