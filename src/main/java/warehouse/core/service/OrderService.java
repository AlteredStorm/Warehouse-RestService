package warehouse.core.service;

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

    public OrderDTO startPicking(String id) {
        Order order = orderRepository.findById(id).orElse(null);
        String description = "Ware für Bestellung ::orderID::";
        if (order != null) {
            description = description.replace("::orderID::", order.getId());
            Location location = new Location( "", "loc_STAGE_" + order.getId(), LocationTypes.STAGE, description);
            order.startPicking();
            List<Order.OrderItem> orderItems = order.getItems();
            List<String> productIds = orderItems.stream().map(Order.OrderItem::getProductSku).toList();
            List<Product> products = productService.findAllById(productIds);

            if (!products.isEmpty()) {
                Map<String, List<StockLevel>> stockLevelByProductId = stockLevelService.findAllByProductId(productIds);

                if (!stockLevelByProductId.isEmpty()) {
                    for (Map.Entry<String, List<StockLevel>> entry : stockLevelByProductId.entrySet()) {
                        String productId = entry.getKey();
                        Optional<Product> product = products.stream().filter(productTemp -> productTemp.getSku().equals(productId)).findFirst();
                        Optional<Order.OrderItem> orderItem = orderItems.stream().filter(item -> item.getProductSku().equals(productId)).findFirst();
                        List<StockLevel> stockLevels = entry.getValue();

                        if (orderItem.isPresent() && product.isPresent()) {
                            int requestedQuantity = orderItem.get().getRequestedQuantity();
                            Optional<StockLevel> bestSingleMatch = stockLevels.stream()
                                    .filter(stockLevel -> stockLevel.getQuantity() >= requestedQuantity)
                                    .min(Comparator.comparingInt(StockLevel::getQuantity));

                            if (bestSingleMatch.isPresent()) {
                                StockLevel stockLevel = new StockLevel(productId, location.getCode(), requestedQuantity);
                                bestSingleMatch.get().setQuantity(bestSingleMatch.get().getQuantity() - requestedQuantity);
                                stockLevelService.saveOrDelete(bestSingleMatch.get());
                                stockLevelService.saveOrDelete(stockLevel);
                                Movement movement = new Movement(productId, requestedQuantity, product.get().getUnit(),
                                        bestSingleMatch.get().getLocationId(), location.getCode(), MovementTypes.INTERNAL_TRANSFER,
                                        "Order", Instant.now());
                                movementService.save(movement);
                                order.finishPicking();
                                orderRepository.save(order);
                                return order.toDTO();
                            }

                            List<StockLevel> sortedStockLevels = stockLevels.stream()
                                    .filter(stockLevel -> stockLevel.getQuantity() > 0)
                                    .sorted(Comparator.comparingInt(StockLevel::getQuantity).reversed())
                                    .toList();

                            int reservedTotal = 0;
                            for (StockLevel stockLevel : sortedStockLevels) {
                                if (reservedTotal >= requestedQuantity) {
                                    break;
                                }

                                int remaining = requestedQuantity - reservedTotal;
                                int quantityToTake = Math.min(stockLevel.getQuantity(), remaining);

                                if (quantityToTake > 0 && stockLevel.getQuantity() >= quantityToTake) {
                                    stockLevel.setQuantity(stockLevel.getQuantity() - quantityToTake);
                                    stockLevelService.saveOrDelete(stockLevel);
                                    Movement movement = new Movement(productId, quantityToTake, product.get().getUnit(),
                                            stockLevel.getLocationId(), location.getCode(), MovementTypes.INTERNAL_TRANSFER,
                                            "Order", Instant.now());
                                    movementService.save(movement);
                                    reservedTotal += quantityToTake;
                                }

                            }

                        }
                    }
                }
            }
            order.finishPicking();
            orderRepository.save(order);
            return order.toDTO();
        } else {
            return null;
        }
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
                    if (stockLevel.get().getQuantity() != orderItem.getRequestedQuantity()) {
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
        if (order.isPresent()) {
            order.get().cancel();
            List<Order.OrderItem> orderItems = order.get().getItems();
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
                    StockLevel stockLevel = sortedStockLevels.getFirst();
                    stockLevel.setQuantity(stockLevel.getQuantity() + orderItem.get().getPickedQuantity());
                    stockLevelService.saveOrDelete(stockLevel);
                    Movement movement = new Movement(orderItem.get().getProductSku(), orderItem.get().getPickedQuantity(), product.get().getUnit(),
                            LocationTypes.STAGE.toString(), stockLevel.getLocationId(), MovementTypes.OUTBOUND, "Canceled", Instant.now());
                    orderItem.get().setPickedQuantity(0);
                    orderItems.add(orderItem.get());
                    movements.add(movement);
                }
            }
            order.get().setItems(orderItems);
            movementService.saveAll(movements);
            return orderRepository.save(order.get()).toDTO();
        } else {
            return null;
        }
    }

    public OrderDTO returnOrder(String id) {
        Optional<Order> order = orderRepository.findById(id);
        List<Movement> movements = new ArrayList<>();
        if (order.isPresent()) {
            List<String> productIds = order.get().getItems().stream().map(Order.OrderItem::getProductSku).toList();
            Map<String, List<StockLevel>> stockLevelMapByProduct = stockLevelService.findAllByProductId(productIds);
            for (Map.Entry<String, List<StockLevel>> entry : stockLevelMapByProduct.entrySet()) {
                Optional<Product> product = productService.findById(entry.getKey());
                Optional<Order.OrderItem> orderItem = order.get().getItems().stream().filter(item -> item.getProductSku().equals(entry.getKey())).findFirst();
                if (orderItem.isPresent() && product.isPresent()) {
                    StockLevel stockLevel = new StockLevel(entry.getKey(), "loc_RETURN_" + order.get().getId(), orderItem.get().getPickedQuantity());
                    stockLevelService.saveOrDelete(stockLevel);
                    Movement movement = new Movement(product.get().getSku(), orderItem.get().getPickedQuantity(), product.get().getUnit(),
                            LocationTypes.CUSTOMER.toString(), LocationTypes.RETURN.toString(), MovementTypes.INBOUND, "Returned", Instant.now());
                    movements.add(movement);
                }
            }

            order.get().returned();
            movementService.saveAll(movements);
            return orderRepository.save(order.get()).toDTO();
        } else {
            return null;
        }


    }
}
