package warehouse.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductDTO {

    String sku;
    String name;
    String category;
    String unit;

}
