package warehouse.core.dto;

import lombok.*;
import warehouse.core.document.StockLevel;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StockLevelDTO {

    String productId;
    String locationId;
    Integer quantity;
    Integer quantityDelta;

    public StockLevelDTO(String productId, String locationId, Integer quantity) {
        this.productId = productId;
        this.locationId = locationId;
        this.quantity = quantity;
    }

    public StockLevel toStock() {
        return new StockLevel(this.productId, this.locationId, this.quantity);
    }

}
