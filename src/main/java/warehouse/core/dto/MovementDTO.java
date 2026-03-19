package warehouse.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import warehouse.core.document.Movement;
import warehouse.core.document.type.MovementTypes;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class MovementDTO {

    String id;
    String productId;
    Integer quantity;
    String unit;
    String fromLocationId;
    String toLocationId;
    Enum<MovementTypes> movementType;
    String reason;
    String comment;
    Instant timestamp;

    public Movement toMovement() {
        return new Movement(this.id, this.productId, this.quantity, this.unit, this.fromLocationId,
                this.toLocationId, this.movementType, this.reason, this.comment, this.timestamp);
    }
}
