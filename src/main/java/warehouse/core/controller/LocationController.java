package warehouse.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import warehouse.core.document.Location;
import warehouse.core.service.LocationService;

import java.util.List;

@RestController("api/locations")
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    public void postLocation(@RequestBody Location location) {
        locationService.save(location);
    }

    @GetMapping
    public ResponseEntity<List<Location>> getLocations() {
        return new ResponseEntity<>(locationService.findAll(), HttpStatus.OK);
    }



}
