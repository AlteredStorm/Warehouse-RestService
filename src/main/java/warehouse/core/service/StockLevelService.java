package warehouse.core.service;

import org.springframework.stereotype.Service;
import warehouse.core.document.StockLevel;
import warehouse.core.dto.StockLevelDTO;
import warehouse.core.repository.StockLevelRepository;

import java.util.List;
import java.util.Optional;

@Service
public class StockLevelService {

    private final StockLevelRepository stockLevelRepository;

    public StockLevelService(StockLevelRepository stockRepository) {
        this.stockLevelRepository = stockRepository;
    }

    public List<StockLevelDTO> findAll() {
        List<StockLevel> stockLevelList = stockLevelRepository.findAll();
        return stockLevelList.stream().map(StockLevel::toDTO).toList();
    }

    public void receipts(StockLevel stock) {
        stockLevelRepository.save(stock);
    }

    public boolean adjustments(StockLevelDTO stockLevelDTO) {
        Optional<StockLevel> stockLevel = stockLevelRepository.findByProductIdAndLocationId(stockLevelDTO.getProductId(), stockLevelDTO.getLocationId());

        if (stockLevel.isPresent()) {
            stockLevel.get().setQuantity(stockLevel.get().getQuantity() + stockLevelDTO.getQuantity());
            stockLevelRepository.save(stockLevel.get());
            return true;
        } else {
            return false;
        }
    }
}
