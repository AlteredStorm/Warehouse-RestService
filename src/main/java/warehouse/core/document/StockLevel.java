package warehouse.core.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import warehouse.core.dto.StockLevelDTO;

@Document("Stock")
@CompoundIndex(def = "{'productId': 1, 'locationId': 1}", unique = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StockLevel {

    public StockLevel(String productId, String locationId, int quantity) {
        this.productId = productId;
        this.locationId = locationId;
        this.quantity = quantity;
    }

    @Id
    String id;

    String productId;
    String locationId;
    int quantity;

    public StockLevelDTO toDTO() {
        return new StockLevelDTO(this.productId, this.locationId, this.quantity);
    }
}
