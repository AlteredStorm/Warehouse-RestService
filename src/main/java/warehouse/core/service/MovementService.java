package warehouse.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import warehouse.core.document.Movement;
import warehouse.core.dto.MovementDTO;
import warehouse.core.repository.MovementRepository;

import java.util.List;

@Component
public class MovementService {

    private final MovementRepository movementRepository;

    @Autowired
    public MovementService(MovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    public List<MovementDTO> findAll(Integer pageSize) {
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

    public void save(Movement movement) {
        movementRepository.save(movement);
    }

    public void saveAll(List<Movement> movements) {
        movementRepository.saveAll(movements);
    }

}
