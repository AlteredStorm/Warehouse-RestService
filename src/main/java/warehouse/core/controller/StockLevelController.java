package warehouse.core.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import warehouse.core.dto.StockLevelDTO;
import warehouse.core.service.StockLevelService;

import java.util.List;

@RestController("/api/stock")
public class StockLevelController {

    private final StockLevelService stockLevelService;

    public StockLevelController(StockLevelService stockLevelService) {
        this.stockLevelService = stockLevelService;
    }

    @GetMapping("/levels")
    public ResponseEntity<List<StockLevelDTO>> getStockLevels() {
        return new ResponseEntity<>(stockLevelService.findAll(), HttpStatus.OK);
    }

    @PostMapping("/receipts")
    public void postStockReceipts(@RequestBody StockLevelDTO stockLevelDTO) {
        stockLevelService.receipts(stockLevelDTO.toStock());
    }

    @PostMapping("/adjustments")
    public ResponseEntity<String> postStockAdjustments(@RequestBody StockLevelDTO stockLevelDTO) {
        if (stockLevelService.adjustments(stockLevelDTO)) {
            return  new ResponseEntity<>(HttpStatus.OK);
        } else  {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
