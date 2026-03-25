package warehouse.core.dto;

import lombok.*;
import warehouse.core.document.Order;
import warehouse.core.document.enums.OrderStatus;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderDTO {

    String id;
    OrderStatus status;
    List<OrderItemDTO> items;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemDTO {
        String productSku;
        Integer requestedQuantity;
        Integer pickedQuantity;

        public Order.OrderItem toOrderItem() {
            return new Order.OrderItem(productSku, requestedQuantity, pickedQuantity);
        }
    }

    public Order toOrder() {
        List<Order.OrderItem> orderItems = this.items.stream().map(OrderItemDTO::toOrderItem).toList();
        return new Order(this.id, orderItems);
    }

}
