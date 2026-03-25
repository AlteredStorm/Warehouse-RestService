package warehouse.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import warehouse.core.document.Product;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ProductDTO {

    String sku;
    String name;
    String category;
    String unit;

    public Product toProduct() {
        return new Product(this.sku, this.name, this.category, this.unit);
    }

}
