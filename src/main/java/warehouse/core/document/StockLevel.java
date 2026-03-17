package warehouse.core.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import warehouse.core.dto.StockLevelDTO;

@Document("Stock")
@Getter
@Setter
@AllArgsConstructor
public class StockLevel {

    String productId;
    String locationId;
    int quantity;
    String referenceNo;
    String comment;

    public StockLevelDTO toDTO() {
        return new StockLevelDTO(this.productId, this.locationId, this.quantity, this.referenceNo, this.comment);
    }

}
