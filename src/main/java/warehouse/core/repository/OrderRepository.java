package warehouse.core.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import warehouse.core.document.Order;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
}
