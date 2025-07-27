package samples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunLocal {

    private static final Logger log = LoggerFactory.getLogger(RunLocal.class);

    public static void main(String[] args) {
        int port = 8080; // Default port can be changed as needed

        App app = new App(port);
        app.start();
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));

        log.info("Server started on {}", app.getUri());
    }

}
