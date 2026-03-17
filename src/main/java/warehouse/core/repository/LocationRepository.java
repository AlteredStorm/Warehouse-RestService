package warehouse.core.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import warehouse.core.document.Location;

@Repository
public interface LocationRepository extends MongoRepository<Location, String> {
}
