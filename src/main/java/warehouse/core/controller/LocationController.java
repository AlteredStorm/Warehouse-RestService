package warehouse.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import warehouse.core.document.Location;
import warehouse.core.dto.LocationDTO;
import warehouse.core.service.LocationService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/locations")
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    public void postLocation(@RequestBody LocationDTO locationDTO) {
        locationService.save(locationDTO.toLocation());
    }

    @GetMapping
    public ResponseEntity<List<LocationDTO>> getLocations() {
        return new ResponseEntity<>(locationService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getLocationById(@PathVariable String id) {
        ResponseEntity<LocationDTO> responseEntity;
        Optional<Location> location = locationService.findById(id);
        if (!location.isPresent()) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            responseEntity = new ResponseEntity<>(location.get().toDTO(), HttpStatus.OK);
        }
        return responseEntity;
    }
}
