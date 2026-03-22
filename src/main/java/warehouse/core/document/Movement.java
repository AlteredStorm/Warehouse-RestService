package warehouse.core.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import warehouse.core.document.enums.MovementTypes;
import warehouse.core.dto.MovementDTO;

import java.time.Instant;

@Document("Movement")
@Getter
@Setter
@AllArgsConstructor
public class Movement {

    @Id
    String id;

    String productId;
    int quantity;
    String unit;
    String fromLocationId;
    String toLocationId;
    Enum<MovementTypes> movementType;
    String reason;
    Instant timestamp;

    public MovementDTO toDTO() {
        return new MovementDTO(this.id, this.productId, this.quantity, this.unit, this.fromLocationId,
                this.toLocationId, this.movementType, this.reason, this.timestamp);
    }

}
