package warehouse.core.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import warehouse.core.document.enums.MovementTypes;
import warehouse.core.dto.MovementDTO;

import java.time.Instant;
import java.util.UUID;

@Document("Movement")
@Getter
@Setter
public class Movement {

    public Movement(String productId, int quantity, String unit, String fromLocationId, String toLocationId,
                    Enum<MovementTypes> movementType, String reason, Instant timestamp) {
        this.productId = productId;
        this.quantity = quantity;
        this.unit = unit;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.movementType = movementType;
        this.reason = reason;
        this.timestamp = timestamp;

        this.id = UUID.randomUUID().toString();

    }


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
