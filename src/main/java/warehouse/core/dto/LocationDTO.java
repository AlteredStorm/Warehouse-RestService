package warehouse.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import warehouse.core.document.Location;

@Getter
@Setter
@AllArgsConstructor
public class LocationDTO {

    String id;
    String code;
    String zone;
    String aisle;
    String rack;
    String bin;
    String type;
    Boolean active;
    String description;

    public Location toLocation() {
        return new Location(id, code, zone, aisle, rack, bin, type, active, description);
    }

}
