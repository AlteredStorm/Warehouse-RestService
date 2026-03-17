package warehouse.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import warehouse.core.document.Product;

@Getter
@Setter
@AllArgsConstructor
public class ProductDTO {

    String sku;
    String name;
    String category;
    String unit;

    public Product toProduct() {
        return new Product(this.sku, this.name, this.category, this.unit);
    }

}
