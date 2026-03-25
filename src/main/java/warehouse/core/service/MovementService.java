package warehouse.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import warehouse.core.document.Movement;
import warehouse.core.dto.MovementDTO;
import warehouse.core.repository.MovementRepository;

import java.util.List;

@Service
@Slf4j
public class MovementService {

    private final MovementRepository movementRepository;

    @Autowired
    public MovementService(MovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    public List<MovementDTO> findAll(Integer pageSize) {
        log.info("Retrieving all movement information limited by page size: {}", pageSize);
        if (pageSize == null) {
            List<Movement> movements = movementRepository.findAll();
            return movements.stream().map(Movement::toDTO).toList();
        } else if(pageSize > 0) {
            Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
            Pageable pageable = PageRequest.of(0, pageSize, sort);
            Page<Movement> movementPage = movementRepository.findAll(pageable);
            return movementPage.stream().map(Movement::toDTO).toList();
        } else {
            return null;
        }
    }

    public void deleteAll() {
        log.info("Deleting all movements");
        movementRepository.deleteAll();
    }

    public void saveAll(List<Movement> movements) {
        log.info("Saving all movement information");
        movementRepository.saveAll(movements);
    }

}
