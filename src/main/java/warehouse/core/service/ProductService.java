package warehouse.core.service;

import org.springframework.stereotype.Service;
import warehouse.core.document.Product;
import warehouse.core.dto.ProductDTO;
import warehouse.core.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void save(Product product) {
        productRepository.save(product);
    }

    public void saveAll(List<Product> products) {
        productRepository.saveAll(products);
    }

    public List<ProductDTO> findAll() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(Product::toDTO).toList();
    }

    public List<Product> findAllById(List<String> ids) {
        return productRepository.findAllById(ids);
    }

    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

}
