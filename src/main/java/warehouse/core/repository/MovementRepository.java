package warehouse.core.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import warehouse.core.document.Movement;

@Repository
public interface MovementRepository extends MongoRepository<Movement, String> {

}
