package warehouse.core.controller;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    public void postLocation(@RequestBody LocationDTO locationDTO) {
        log.info("POST api/location called with DTO: {}", locationDTO.toString());
        locationService.save(locationDTO.toLocation());
    }

    @GetMapping
    public ResponseEntity<List<LocationDTO>> getLocations() {
        log.info("GET api/location called");
        return new ResponseEntity<>(locationService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getLocationById(@PathVariable String id) {
        log.info("GET api/location/{} called", id);
        ResponseEntity<LocationDTO> responseEntity;
        Optional<Location> location = locationService.findById(id);
        responseEntity = location.map(value -> new ResponseEntity<>(value.toDTO(), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        return responseEntity;
    }
}
