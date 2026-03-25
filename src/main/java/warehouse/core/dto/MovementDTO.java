package warehouse.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import warehouse.core.document.enums.MovementTypes;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class MovementDTO {

    String id;
    String productId;
    Integer quantity;
    String unit;
    String fromLocationId;
    String toLocationId;
    Enum<MovementTypes> movementType;
    String reason;
    Instant timestamp;
}
