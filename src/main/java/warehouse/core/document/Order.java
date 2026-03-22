package warehouse.core.document;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import warehouse.core.document.enums.OrderStatus;
import warehouse.core.dto.OrderDTO;

import java.util.List;

@Document("Order")
@Getter
@Setter
public class Order {

    public Order(String id, List<OrderItem> orderItems) {
        this.id = id;
        this.items = orderItems;
    }

    String id;

    @Setter(AccessLevel.NONE)
    OrderStatus status;
    List<OrderItem> items;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class OrderItem {
        String productId;
        String productSku;
        int requestedQuantity;
        int pickedQuantity;

        public OrderDTO.OrderItemDTO toDTO() {
            return new OrderDTO.OrderItemDTO(this.productId, this.productSku, this.requestedQuantity, this.pickedQuantity);
        }
    }

    public void created() {
        this.status = OrderStatus.CREATED;
    }


    public void startPicking() {
        this.status = OrderStatus.PICKING;
    }

    public void finishPicking() {
        this.status = OrderStatus.PICKED;
    }

    public void ship() {
        this.status = OrderStatus.SHIPPED;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELED;
    }

    public void returned() {
        this.status = OrderStatus.RETURNED;
    }

    public OrderDTO toDTO() {
        List<OrderDTO.OrderItemDTO> orderItems = this.items.stream().map(OrderItem::toDTO).toList();
        return new OrderDTO(this.id, this.status, orderItems);
    }

}


