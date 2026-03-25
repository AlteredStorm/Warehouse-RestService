package warehouse.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import warehouse.core.document.*;
import warehouse.core.document.enums.LocationTypes;
import warehouse.core.document.enums.MovementTypes;
import warehouse.core.dto.OrderDTO;
import warehouse.core.repository.OrderRepository;

import java.time.Instant;
import java.util.*;

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
        List<Order> order = orderRepository.findAll();
        return order.stream().map(Order::toDTO).toList();
    }

    public OrderDTO findById(String id) {
        Order order = orderRepository.findById(id).orElse(null);
        if  (order == null) {
            return null;
        } else {
            return order.toDTO();
        }
    }

    public void save(OrderDTO orderDTO) {
        Order order = orderDTO.toOrder();
        order.created();
        orderRepository.save(order);
    }

    public void deleteAll() {
        orderRepository.deleteAll();
    }

    public OrderDTO startPicking(String id) {
        Optional<Order> order = orderRepository.findById(id);

        if (order.isPresent()) {

            order.get().startPicking();
            List<Order.OrderItem> orderItems = order.get().getItems();

            for (Order.OrderItem item : orderItems) {
                item.setPickedQuantity(0);
            }

            List<String> productIds = orderItems.stream().map(Order.OrderItem::getProductSku).toList();
            List<Product> products = productService.findAllById(productIds);

            if (!products.isEmpty()) {
                Map<String, List<StockLevel>> stockLevelByProductId = stockLevelService.findAllByProductId(productIds);

                if (!stockLevelByProductId.isEmpty()) {
                    return processPicking(stockLevelByProductId, products, order.get(), orderItems);
                }
            } else {
                order.get().cancel();
                orderRepository.save(order.get());
                return order.get().toDTO();
            }
        }
        return null;
    }

    private OrderDTO processPicking(Map<String, List<StockLevel>> stockLevelByProductId, List<Product> products,
                                    Order order, List<Order.OrderItem> orderItems) {

        String description = "Ware für Bestellung ::orderID::";
        description = description.replace("::orderID::", order.getId());
        Location location = new Location("loc_STAGE_" + order.getId(), LocationTypes.STAGE, description);
        List<Movement> movements = new ArrayList<>();
        List<StockLevel> reservedStockLevel = new ArrayList<>();

        for (Map.Entry<String, List<StockLevel>> entry : stockLevelByProductId.entrySet()) {
            String productSku = entry.getKey();

            Optional<Product> product = products.stream()
                    .filter(productTemp -> productTemp.getSku().equals(productSku))
                    .findFirst();

            Optional<Order.OrderItem> orderItem = orderItems.stream()
                    .filter(item -> item.getProductSku().equals(productSku))
                    .findFirst();

            List<StockLevel> stockLevels = entry.getValue();

            if (orderItem.isPresent() && product.isPresent()) {
                int requestedQuantity = orderItem.get().getRequestedQuantity();

                Optional<StockLevel> bestSingleMatch = stockLevels.stream()
                        .filter(stockLevel -> stockLevel.getQuantity() >= requestedQuantity)
                        .min(Comparator.comparingInt(StockLevel::getQuantity));

                if (bestSingleMatch.isPresent()) {
                    orderItem.get().setPickedQuantity(requestedQuantity);

                    StockLevel stageStockLevel = new StockLevel(
                            productSku,
                            location.getCode(),
                            requestedQuantity
                    );

                    bestSingleMatch.get().setQuantity(bestSingleMatch.get().getQuantity() - requestedQuantity);

                    reservedStockLevel.add(bestSingleMatch.get());
                    reservedStockLevel.add(stageStockLevel);

                    Movement movement = new Movement(
                            productSku,
                            requestedQuantity,
                            product.get().getUnit(),
                            bestSingleMatch.get().getLocationId(),
                            location.getCode(),
                            MovementTypes.INTERNAL_TRANSFER,
                            "Order",
                            Instant.now()
                    );
                    movements.add(movement);
                    continue;
                }

                List<StockLevel> sortedStockLevels = stockLevels.stream()
                        .filter(stockLevel -> stockLevel.getQuantity() > 0)
                        .sorted(Comparator.comparingInt(StockLevel::getQuantity).reversed())
                        .toList();

                int reservedTotal = 0;
                StockLevel stageStockLevel = new StockLevel(productSku, location.getCode(), 0);

                for (StockLevel stockLevel : sortedStockLevels) {
                    if (reservedTotal >= requestedQuantity) {
                        break;
                    }

                    int remaining = requestedQuantity - reservedTotal;
                    int quantityToTake = Math.min(stockLevel.getQuantity(), remaining);

                    if (quantityToTake > 0) {
                        stockLevel.setQuantity(stockLevel.getQuantity() - quantityToTake);
                        stageStockLevel.setQuantity(stageStockLevel.getQuantity() + quantityToTake);

                        reservedStockLevel.add(stockLevel);

                        Movement movement = new Movement(
                                productSku,
                                quantityToTake,
                                product.get().getUnit(),
                                stockLevel.getLocationId(),
                                location.getCode(),
                                MovementTypes.INTERNAL_TRANSFER,
                                "Order",
                                Instant.now()
                        );
                        movements.add(movement);

                        orderItem.get().setPickedQuantity(orderItem.get().getPickedQuantity() + quantityToTake);
                        reservedTotal += quantityToTake;
                    }
                }

                if (stageStockLevel.getQuantity() > 0) {
                    reservedStockLevel.add(stageStockLevel);
                }
            }
        }

        order.finishPicking();
        locationService.save(location);
        stockLevelService.saveOrDeleteAll(reservedStockLevel);
        movementService.saveAll(movements);
        orderRepository.save(order);
        return order.toDTO();
    }

    public OrderDTO ship(String id) {
        Optional<Order> order = orderRepository.findById(id);
        List<StockLevel> stockLevels = new ArrayList<>();
        List<Movement> movements = new ArrayList<>();
        if (order.isPresent()) {
            String locationId = "loc_STAGE_" + order.get().getId();
            for (Order.OrderItem orderItem : order.get().getItems()) {
                Optional<StockLevel> stockLevel = stockLevelService.findByProductIdAndLocationId(orderItem.getProductSku(), locationId);
                Optional<Product> product = productService.findById(orderItem.getProductSku());
                if (stockLevel.isPresent() && product.isPresent()) {
                    if (stockLevel.get().getQuantity() != orderItem.getPickedQuantity()) {
                        return order.get().toDTO();
                    }
                    stockLevels.add(stockLevel.get());
                    Movement movement = new Movement(product.get().getSku(), orderItem.getPickedQuantity(), product.get().getUnit(),
                            locationId, LocationTypes.CUSTOMER.toString(), MovementTypes.OUTBOUND,
                            "Order", Instant.now());
                    movements.add(movement);
                }
            }

            order.get().ship();
            orderRepository.save(order.get());
            movementService.saveAll(movements);
            stockLevelService.deleteAll(stockLevels);
            return order.get().toDTO();
        } else {
            return null;
        }
    }

    public OrderDTO cancel(String id) {
        Optional<Order> order = orderRepository.findById(id);
        List<Movement> movements = new ArrayList<>();
        List<Order.OrderItem> newOrderItems = new ArrayList<>();
        List<StockLevel> reservedStockLevels = new ArrayList<>();
        if (order.isPresent()) {
            String locationId = "loc_STAGE_" + order.get().getId();
            order.get().cancel();
            List<String> productIds = order.get().getItems().stream().map(Order.OrderItem::getProductSku).toList();
            Map<String, List<StockLevel>> stockLevelMapByProduct = stockLevelService.findAllByProductId(productIds);
            for (Map.Entry<String, List<StockLevel>> entry : stockLevelMapByProduct.entrySet()) {
                Optional<Product> product = productService.findById(entry.getKey());
                Optional<Order.OrderItem> orderItem = order.get().getItems().stream().filter(item -> item.getProductSku().equals(entry.getKey())).findFirst();
                if (orderItem.isPresent() && product.isPresent()) {
                    List<StockLevel> stockLevels = entry.getValue();
                    List<StockLevel> sortedStockLevels = stockLevels.stream()
                            .sorted(Comparator.comparingInt(StockLevel::getQuantity).reversed())
                            .toList();
                    if (!sortedStockLevels.isEmpty()) {
                        StockLevel stockLevel = sortedStockLevels.getFirst();
                        stockLevel.setQuantity(stockLevel.getQuantity() + orderItem.get().getPickedQuantity());
                        reservedStockLevels.add(stockLevel);
                        Movement movement = new Movement(orderItem.get().getProductSku(), orderItem.get().getPickedQuantity(), product.get().getUnit(),
                                locationId, stockLevel.getLocationId(), MovementTypes.INTERNAL_TRANSFER, "Canceled", Instant.now());
                        orderItem.get().setPickedQuantity(0);
                        newOrderItems.add(orderItem.get());
                        movements.add(movement);
                    }
                }
            }
            order.get().setItems(newOrderItems);
            stockLevelService.saveOrDeleteAll(reservedStockLevels);
            locationService.deleteById(locationId);
            movementService.saveAll(movements);
            return orderRepository.save(order.get()).toDTO();
        } else {
            return null;
        }
    }

    public OrderDTO returnOrder(String id) {
        Optional<Order> order = orderRepository.findById(id);
        List<Movement> movements = new ArrayList<>();
        List<StockLevel> reservedStockLevels = new ArrayList<>();
        if (order.isPresent()) {
            Location location = new Location("loc_RETURN_" + order.get().getId(), LocationTypes.RETURN,
                    "Return location for Order " + order.get().getId());
            List<String> productIds = order.get().getItems().stream().map(Order.OrderItem::getProductSku).toList();
            Map<String, List<StockLevel>> stockLevelMapByProduct = stockLevelService.findAllByProductId(productIds);
            for (Map.Entry<String, List<StockLevel>> entry : stockLevelMapByProduct.entrySet()) {
                Optional<Product> product = productService.findById(entry.getKey());
                Optional<Order.OrderItem> orderItem = order.get().getItems().stream().filter(item -> item.getProductSku().equals(entry.getKey())).findFirst();
                if (orderItem.isPresent() && product.isPresent()) {
                    String locationId = "loc_RETURN_" + order.get().getId();
                    StockLevel stockLevel = new StockLevel(entry.getKey(), locationId, orderItem.get().getPickedQuantity());
                    reservedStockLevels.add(stockLevel);
                    Movement movement = new Movement(product.get().getSku(), orderItem.get().getPickedQuantity(), product.get().getUnit(),
                            LocationTypes.CUSTOMER.toString(), locationId, MovementTypes.INBOUND, "Returned", Instant.now());
                    movements.add(movement);
                }
            }

            order.get().returned();
            stockLevelService.saveOrDeleteAll(reservedStockLevels);
            locationService.save(location);
            movementService.saveAll(movements);
            return orderRepository.save(order.get()).toDTO();
        } else {
            return null;
        }


    }
}
