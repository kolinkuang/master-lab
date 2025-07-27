package samples;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.handlers.ResourceHandlerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static io.muserver.ContextHandlerBuilder.context;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    private final int port;
    private MuServer muServer;

    public App(int port) {
        this.port = port;
    }

    public void start() {

        TweetHandler tweetHandler = new TweetHandler();

        muServer = MuServerBuilder.muServer()
                .withHttpPort(port)
                .addHandler(Method.POST, "/api/v1/tweets", tweetHandler::createTweet)
                .addHandler(Method.GET, "/api/v1/tweets", tweetHandler::getAllTweets)
                .addHandler(context("/web")
                        .addHandler(ResourceHandlerBuilder
                                .fileOrClasspath("src/main/resources/web", "web")
                                .withDefaultFile("index.html")
                        ))
                .start();
    }

    public void stop() {
        if (muServer != null) {
            muServer.stop();
            log.info("Server stopped");
        }
    }

    public String getUri() {
        return muServer.uri().toString();
    }

    public static void main(String[] args) {
        Map<String, String> settings = System.getenv();
        int port = Integer.parseInt(settings.getOrDefault("PORT", "9000"));

        App app = new App(port);
        app.start();
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));

        log.info("Server started on port {}", port);
    }

}
