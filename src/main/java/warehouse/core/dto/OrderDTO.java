package warehouse.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import warehouse.core.document.Order;
import warehouse.core.document.enums.OrderStatus;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class OrderDTO {

    String id;
    OrderStatus status;
    List<OrderItemDTO> items;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class OrderItemDTO {
        String productId;
        String productSku;
        Integer requestedQuantity;
        Integer pickedQuantity;

        public Order.OrderItem toOrderItem() {
            return new Order.OrderItem(productId, productSku, requestedQuantity, pickedQuantity);
        }
    }

    public Order toOrder() {
        List<Order.OrderItem> orderItems = this.items.stream().map(OrderItemDTO::toOrderItem).toList();
        return new Order(this.id, orderItems);
    }

}
