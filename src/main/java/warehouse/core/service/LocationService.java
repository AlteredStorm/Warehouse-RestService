package warehouse.core.service;

import org.springframework.stereotype.Service;
import warehouse.core.document.Location;
import warehouse.core.dto.LocationDTO;
import warehouse.core.repository.LocationRepository;

import java.util.List;

@Service
public class LocationService {

    LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public void save(Location location) {
        locationRepository.save(location);
    }

    public List<LocationDTO> findAll() {
        List<Location> locations = locationRepository.findAll();
        return locations.stream().map(Location::toDTO).toList();
    }

    public Location findById(String id) {
        return locationRepository.findById(id).orElse(null);
    }

}
