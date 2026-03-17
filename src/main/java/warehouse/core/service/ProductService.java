package warehouse.core.service;

import org.springframework.stereotype.Service;
import warehouse.core.document.Product;
import warehouse.core.dto.ProductDTO;
import warehouse.core.repository.ProductRepository;

import java.util.List;

@Service
public class ProductService {

    ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void save(Product product) {
        productRepository.save(product);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public ProductDTO findById(String id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return null;
        } else {
            return product.toDTO();
        }
    }
}
