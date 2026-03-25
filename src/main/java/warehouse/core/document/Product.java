package warehouse.core.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import warehouse.core.dto.ProductDTO;

@Document("Product")
@Getter
@Setter
@AllArgsConstructor
@ToString
public class Product {

    @Id
    String sku;

    String name;
    String category;
    String unit;

    public ProductDTO toDTO() {
        return new ProductDTO(this.sku, this.name, this.category, this.unit);
    }

}
