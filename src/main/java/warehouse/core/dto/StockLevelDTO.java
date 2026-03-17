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
    String referenceNo;
    String comment;

    public StockLevelDTO(String productId, String locationId, int quantity, String referenceNo, String comment) {
        this.productId = productId;
        this.locationId = locationId;
        this.quantity = quantity;
        this.referenceNo = referenceNo;
        this.comment = comment;
    }

    public StockLevel toStock() {
        return new StockLevel(this.productId, this.locationId, this.quantity, this.referenceNo, this.comment);
    }

}
