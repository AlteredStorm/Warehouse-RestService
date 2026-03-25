package warehouse.core.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import warehouse.core.document.StockLevel;
import warehouse.core.dto.StockLevelDTO;
import warehouse.core.service.StockLevelService;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@Slf4j
public class StockLevelController {

    private final StockLevelService stockLevelService;

    @Autowired
    public StockLevelController(StockLevelService stockLevelService) {
        this.stockLevelService = stockLevelService;
    }

    @GetMapping("/levels")
    public ResponseEntity<List<StockLevelDTO>> getStockLevels() {
        log.info("GET api/stock/levels called");
        return new ResponseEntity<>(stockLevelService.findAll(), HttpStatus.OK);
    }

    @PostMapping("/receipts")
    public ResponseEntity<StockLevelDTO> postStockReceipts(@RequestBody StockLevelDTO stockLevelDTO) {
        log.info("GET api/stock/receipts called with DTO {}", stockLevelDTO.toString());
        StockLevel stockLevel = stockLevelService.receipts(stockLevelDTO.toStock());
        if (stockLevel != null) {
            return  new ResponseEntity<>(HttpStatus.OK);
        } else  {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/adjustments")
    public ResponseEntity<String> postStockAdjustments(@RequestBody StockLevelDTO stockLevelDTO) {
        log.info("GET api/stock/adjustments called with DTO {}", stockLevelDTO.toString());
        if (stockLevelService.adjustments(stockLevelDTO)) {
            return  new ResponseEntity<>(HttpStatus.OK);
        } else  {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
