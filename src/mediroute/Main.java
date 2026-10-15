package mediroute;

import mediroute.app.MediRouteApp;
import mediroute.data.DataRepository;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Entry point for the MediRoute Application.
 * Supports configurable data directory via command-line arguments or environment.
 */
public class Main {

    public static void main(String[] args) {
        Path dataDir;
        if (args.length > 0) {
            dataDir = Paths.get(args[0]);
        } else {
            dataDir = DataRepository.resolveDefaultDataDirectory();
        }

        MediRouteApp app = new MediRouteApp(dataDir);
        app.start();
    }
}
