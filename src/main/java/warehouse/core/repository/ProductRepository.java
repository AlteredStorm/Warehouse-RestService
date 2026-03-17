package warehouse.core.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import warehouse.core.document.Product;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
}
