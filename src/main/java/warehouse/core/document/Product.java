package warehouse.core.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Product")
@Getter
@Setter
public class Product {

    String sku;
    String name;
    String category;
    String unit;

}
