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

    String code;
    LocationTypes type;
    String description;

    public Location toLocation() {
        return new Location(code, type, description);
    }

}
