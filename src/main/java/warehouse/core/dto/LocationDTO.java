package warehouse.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import warehouse.core.document.Location;
import warehouse.core.document.enums.LocationTypes;

@Getter
@Setter
@AllArgsConstructor
public class LocationDTO {

    String id;
    String code;
    LocationTypes type;
    Boolean active;
    String description;

    public Location toLocation() {
        return new Location(id, code, type, active, description);
    }

}
