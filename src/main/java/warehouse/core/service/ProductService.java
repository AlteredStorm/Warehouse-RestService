package warehouse.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import warehouse.core.document.Product;
import warehouse.core.dto.ProductDTO;
import warehouse.core.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ProductService {

    ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void save(Product product) {
        log.info("Saving product information {}", product);
        productRepository.save(product);
    }

    public void saveAll(List<Product> products) {
        log.info("Saving all products information {}", products);
        productRepository.saveAll(products);
    }

    public List<ProductDTO> findAll() {
        log.info("Retrieving all products information");
        List<Product> products = productRepository.findAll();
        return products.stream().map(Product::toDTO).toList();
    }

    public List<Product> findAllById(List<String> ids) {
        log.info("Retrieving all products information specified by ids: {}", ids);
        return productRepository.findAllById(ids);
    }

    public Optional<Product> findById(String id) {
        log.info("Retrieving product information by Id {}", id);
        return productRepository.findById(id);
    }

    public void deleteAll() {
        log.info("Deleting all products information");
        productRepository.deleteAll();
    }
}
