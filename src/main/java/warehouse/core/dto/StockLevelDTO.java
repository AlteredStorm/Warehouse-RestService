package warehouse.core.dto;

import lombok.Getter;
import lombok.Setter;
import warehouse.core.document.StockLevel;

@Getter
@Setter
public class StockLevelDTO {

    String productId;
    String locationId;
    Integer quantity;
    Integer quantityDelta;

    public StockLevelDTO(String productId, String locationId, int quantity) {
        this.productId = productId;
        this.locationId = locationId;
        this.quantity = quantity;
    }

    public StockLevel toStock() {
        return new StockLevel(this.productId, this.locationId, this.quantity);
    }

}
