package warehouse.core.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import warehouse.core.document.Product;
import warehouse.core.dto.ProductDTO;
import warehouse.core.service.ProductService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public void postProduct(@RequestBody ProductDTO productDTO) {
        log.info("POST api/products called with DTO: {}", productDTO.toString());
        productService.save(productDTO.toProduct());
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getProduct() {
        log.info("GET api/products called");
        return new ResponseEntity<>(productService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable String id) {
        log.info("GET api/products/{} called", id);
        ResponseEntity<ProductDTO> responseEntity;
        Optional<Product> product = productService.findById(id);
        responseEntity = product.map(value -> new ResponseEntity<>(value.toDTO(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        return responseEntity;
    }

}
