package mediroute.app;

import mediroute.data.DataRepository;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main application initializer for MediRoute.
 */
public class MediRouteApp {

    private final DataRepository repository;
    private final ConsoleMenu menu;

    public MediRouteApp(Path dataDir) {
        this.repository = new DataRepository(dataDir);
        boolean loaded = this.repository.loadAll();
        if (!loaded) {
            System.err.println("[WARNING] Data loading completed with notices: " + repository.getValidationErrors());
        }
        this.menu = new ConsoleMenu(this.repository);
    }

    public void start() {
        menu.run();
    }
}
