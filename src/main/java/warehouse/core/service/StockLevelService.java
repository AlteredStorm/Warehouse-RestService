package warehouse.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import warehouse.core.document.StockLevel;
import warehouse.core.dto.StockLevelDTO;
import warehouse.core.repository.StockLevelRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StockLevelService {

    private final StockLevelRepository stockLevelRepository;

    @Autowired
    public StockLevelService(StockLevelRepository stockRepository) {
        this.stockLevelRepository = stockRepository;
    }

    public List<StockLevelDTO> findAll() {
        List<StockLevel> stockLevelList = stockLevelRepository.findAll();
        return stockLevelList.stream().map(StockLevel::toDTO).toList();
    }

    public Map<String, List<StockLevel>> findAllByProductId(List<String> productIds) {
        Map<String, List<StockLevel>> stockLevelMapByProduct = new HashMap<>();
        List<StockLevel> stockLevels = stockLevelRepository.findAllByProductIdIn(productIds);
        for (String productId : productIds) {
            List<StockLevel> temp = stockLevels.stream()
                    .filter(stockLevel -> stockLevel.getProductId().equals(productId)).toList();
            stockLevelMapByProduct.put(productId, temp);
        }
        return stockLevelMapByProduct;
    }

    public Optional<StockLevel> findByProductIdAndLocationId(String productId, String locationId) {
        return stockLevelRepository.findByProductIdAndLocationId(productId, locationId);
    }

    public void saveOrDeleteAll(List<StockLevel> stockLevels) {
        for (StockLevel stockLevel : stockLevels) {
            if (stockLevel.getQuantity() <= 0) {
                stockLevelRepository.delete(stockLevel);
            } else {
                stockLevelRepository.save(stockLevel);
            }
        }
    }

    public void saveAll(List<StockLevel> stockLevels) {
        stockLevelRepository.saveAll(stockLevels);
    }

    public void deleteAll(List<StockLevel> stockLevels) {
        stockLevelRepository.deleteAll(stockLevels);
    }

    public void deleteAll() {
        stockLevelRepository.deleteAll();
    }

    public StockLevel receipts(StockLevel stock) {
        if (stock.getQuantity() <= 0) {
            return null;
        }

        Optional<StockLevel> existingStock = stockLevelRepository.findByProductIdAndLocationId(
                stock.getProductId(),
                stock.getLocationId()
        );

        if (existingStock.isPresent()) {
            StockLevel currentStock = existingStock.get();
            currentStock.setQuantity(currentStock.getQuantity() + stock.getQuantity());
            return stockLevelRepository.save(currentStock);
        } else {
            return stockLevelRepository.save(stock);
        }
    }

    public boolean adjustments(StockLevelDTO stockLevelDTO) {
        Optional<StockLevel> stockLevel = stockLevelRepository.findByProductIdAndLocationId(stockLevelDTO.getProductId(),
                stockLevelDTO.getLocationId());

        if (stockLevel.isPresent() && (stockLevel.get().getQuantity() + stockLevelDTO.getQuantityDelta()) >= 0) {
            stockLevel.get().setQuantity(stockLevel.get().getQuantity() + stockLevelDTO.getQuantityDelta());
            stockLevelRepository.save(stockLevel.get());
            return true;
        } else {
            return false;
        }
    }
}
