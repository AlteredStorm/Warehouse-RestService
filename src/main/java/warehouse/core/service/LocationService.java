package warehouse.core.service;

import org.springframework.stereotype.Service;
import warehouse.core.document.Location;
import warehouse.core.dto.LocationDTO;
import warehouse.core.repository.LocationRepository;

import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public void save(Location location) {
        locationRepository.save(location);
    }

    public void saveAll(List<Location> locations) {
        locationRepository.saveAll(locations);
    }

    public void deleteById(String locationId) {
        locationRepository.deleteById(locationId);
    }

    public List<LocationDTO> findAll() {
        List<Location> locations = locationRepository.findAll();
        return locations.stream().map(Location::toDTO).toList();
    }

    public Optional<Location> findById(String id) {
        return locationRepository.findById(id);
    }

}
