package warehouse.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import warehouse.core.dto.ProductDTO;
import warehouse.core.service.ProductService;

import java.util.List;

@RestController("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public void postProduct(@RequestBody ProductDTO productDTO) {
        productService.save(productDTO.toProduct());
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getProduct() {
        return new ResponseEntity<>(productService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable String id) {
        ResponseEntity<ProductDTO> responseEntity;
        ProductDTO product = productService.findById(id);
        if (product == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            responseEntity = new ResponseEntity<>(product, HttpStatus.OK);
        }
        return responseEntity;
    }

}
