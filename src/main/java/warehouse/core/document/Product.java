package warehouse.core.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import warehouse.core.dto.ProductDTO;

@Document("Product")
@Getter
@Setter
public class Product {

    String sku;
    String name;
    String category;
    String unit;

    public ProductDTO toDTO() {
        return new ProductDTO(this.sku, this.name, this.category, this.unit);
    }

}
