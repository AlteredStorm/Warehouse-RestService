package warehouse.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import warehouse.core.document.Location;
import warehouse.core.document.Product;
import warehouse.core.document.StockLevel;
import warehouse.core.document.enums.LocationTypes;
import warehouse.core.document.enums.OrderStatus;
import warehouse.core.repository.OrderRepository;
import warehouse.core.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class StartupService {

    private final ProductService productService;
    private final StockLevelService stockLevelService;
    private final LocationService locationService;
    private final OrderService orderService;
    private final MovementService movementService;

    @Autowired
    public StartupService(ProductService productService, StockLevelService stockLevelService,
                          LocationService locationService, OrderService orderService, MovementService movementService) {
        this.productService = productService;
        this.stockLevelService = stockLevelService;
        this.locationService = locationService;
        this.orderService = orderService;
        this.movementService = movementService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void fillDatabase() {
        List<Product> products = createProducts();
        List<Location> locations = createLocations();
        List<StockLevel> stockLevels;

        orderService.deleteAll();
        movementService.deleteAll();
        productService.deleteAll();
        locationService.deleteAll();
        stockLevelService.deleteAll();


        productService.saveAll(products);
        locationService.saveAll(locations);

        stockLevels = createStockLevels(products, locations);

        stockLevelService.saveAll(stockLevels);
    }

    private List<Product> createProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("APP-TSH-001", "T-Shirt", "Apparel", "pcs"));
        products.add(new Product("APP-TSH-002", "T-Shirt", "Apparel", "pcs"));
        products.add(new Product("ELE-HEA-001", "Headphones", "Clothing", "pcs"));
        products.add(new Product("ELE-MON-001", "Monitor", "Clothing", "pcs"));
        products.add(new Product("APP-JEA-001", "Jeans", "Clothing", "pcs"));
        products.add(new Product("FUR-TAB-001", "Table", "Clothing", "pcs"));
        return products;
    }

    private List<Location> createLocations() {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location("loc_" + LocationTypes.INTERNAL + "_A",
                LocationTypes.INTERNAL, "Internal Storage A"));
        locations.add(new Location("loc_" + LocationTypes.INTERNAL + "_B",
                LocationTypes.INTERNAL, "Internal Storage B"));
        locations.add(new Location("loc_" + LocationTypes.INTERNAL + "_C",
                LocationTypes.INTERNAL, "Internal Storage C"));
        locations.add(new Location("loc_" + LocationTypes.RETURN + "_R",
                LocationTypes.RETURN, "Storage for Return"));

        return locations;
    }

    private List<StockLevel> createStockLevels(List<Product> products, List<Location> locations) {
        List<Location> locationsWithoutReturn = locations.stream().filter(location -> !location.getType().equals(LocationTypes.RETURN)).toList();
        List<StockLevel> stockLevels = new ArrayList<>();
        int min = 20;
        int max = 50;

        for (Product product : products) {
            Location randomLocation = locationsWithoutReturn.get(ThreadLocalRandom.current().nextInt(locationsWithoutReturn.size()));
            int quantity = ThreadLocalRandom.current().nextInt(min, max + 1);
            StockLevel stockLevel = new StockLevel(product.getSku(), randomLocation.getCode(), quantity);
            stockLevels.add(stockLevel);
        }
        return stockLevels;
    }

}
