package warehouse.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import warehouse.core.document.Location;
import warehouse.core.dto.LocationDTO;
import warehouse.core.repository.LocationRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class LocationService {

    LocationRepository locationRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public void save(Location location) {
        log.info("Saving location {}", location.toString());
        locationRepository.save(location);
    }

    public void saveAll(List<Location> locations) {
        log.info("Saving locations {}", locations.toString());
        locationRepository.saveAll(locations);
    }

    public void deleteById(String locationId) {
        log.info("Deleting location with id: {}", locationId);
        locationRepository.deleteById(locationId);
    }

    public void deleteAll() {
        log.info("Deleting all locations");
        locationRepository.deleteAll();
    }

    public List<LocationDTO> findAll() {
        log.info("Retrieving all locations");
        List<Location> locations = locationRepository.findAll();
        return locations.stream().map(Location::toDTO).toList();
    }

    public Optional<Location> findById(String id) {
        log.info("Retrieving location with id: {}", id);
        return locationRepository.findById(id);
    }

}
