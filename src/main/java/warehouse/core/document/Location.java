package warehouse.core.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import warehouse.core.dto.LocationDTO;

@Document("Location")
@Getter
@Setter
@AllArgsConstructor
public class Location {

    String id;
    String code;
    String zone;
    String aisle;
    String rack;
    String bin;
    String type;
    boolean active;
    String description;

    public LocationDTO toDTO() {
        return new LocationDTO(id, code, zone, aisle, rack, bin, type, active, description);
    }

}
