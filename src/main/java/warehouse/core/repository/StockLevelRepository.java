package warehouse.core.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import warehouse.core.document.StockLevel;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockLevelRepository extends MongoRepository<StockLevel, String> {

    Optional<StockLevel> findByProductIdAndLocationId(String productId, String locationId);

    List<StockLevel> findAllByProductIdIn(List<String> productIds);

    List<StockLevel> findAllByProductIdInAndLocationId(List<String> productIds, String locationId);
}
