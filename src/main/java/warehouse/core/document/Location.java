package warehouse.core.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import warehouse.core.document.enums.LocationTypes;
import warehouse.core.dto.LocationDTO;

@Document("Location")
@Getter
@Setter
@AllArgsConstructor
@ToString
public class Location {

    @Id
    String code;

    LocationTypes type;
    String description;

    public LocationDTO toDTO() {
        return new LocationDTO(code, type, description);
    }

}
