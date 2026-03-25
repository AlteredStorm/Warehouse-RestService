package warehouse.core.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import warehouse.core.dto.MovementDTO;
import warehouse.core.service.MovementService;

import java.util.List;

@RestController
@RequestMapping("/api/movements")
@Slf4j
public class MovementController {

    private final MovementService movementService;

    @Autowired
    public MovementController(MovementService movementService) {
        this.movementService = movementService;
    }

    @GetMapping
    public ResponseEntity<List<MovementDTO>> getMovements(@RequestParam(required = false) Integer pageSize) {
        log.info("GET api/movements called");
        ResponseEntity<List<MovementDTO>> responseEntity;
        List<MovementDTO> movementDTOS = movementService.findAll(pageSize);

        if (movementDTOS == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if (movementDTOS.isEmpty()) {
            responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            responseEntity = new ResponseEntity<>(movementDTOS, HttpStatus.OK);
        }
        return responseEntity;
    }

}
