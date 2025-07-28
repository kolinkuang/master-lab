package samples;

import io.muserver.*;
import io.muserver.handlers.ResourceHandlerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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

        SessionHandler sessionHandler = new SessionHandler();
        TweetHandler tweetHandler = new TweetHandler();

        muServer = MuServerBuilder.muServer()
                .withHttpPort(port)
                .addResponseCompleteListener(logResponse())
                .addHandler(logRequest())
                .addHandler(authFilter(sessionHandler, List.of(
                        "/api/v1/sessions/validate",
                        "/api/v1/tweets",
                        "/api/v1/tweets/sse"
                )))
                .addHandler(Method.POST, "/api/v1/sessions/login", sessionHandler::login)
                .addHandler(Method.POST, "/api/v1/sessions/logout", sessionHandler::logout)
                .addHandler(Method.GET, "/api/v1/sessions/validate", sessionHandler::getSessionForUi)
                .addHandler(Method.POST, "/api/v1/tweets", tweetHandler::createTweet)
                .addHandler(Method.GET, "/api/v1/tweets", tweetHandler::getAllTweets)
                .addHandler(Method.GET, "/api/v1/tweets/sse", tweetHandler::sse)
                .addHandler(context("/web")
                        .addHandler(ResourceHandlerBuilder
                                .fileOrClasspath("src/main/resources/web", "web")
                                .withDefaultFile("index.html")
                        ))
                .start();
    }

    private MuHandler logRequest() {
        return (muRequest, muResponse) -> {
            log.info("Received request: {} {} from {}",
                    muRequest.method(),
                    muRequest.uri(),
                    muRequest.remoteAddress());
            return false; // Continue processing the request
        };
    }

    private ResponseCompleteListener logResponse() {
        return info -> log.info("Completed request {} with status {} in {} ms",
                info.request().uri(),
                info.response().status(),
                info.duration());
    }

    private MuHandler authFilter(SessionHandler sessionHandler, List<String> apiPathsToBeValidated) {
        return (muRequest, muResponse) -> {
            Session session = sessionHandler.getSession(muRequest, muResponse);
            if (session == null && isApiMatched(apiPathsToBeValidated, muRequest)) {
                muResponse.status(401);
                return true;
            }

            if (session != null) {
                muRequest.attribute("session", session);
            }

            return false;
        };
    }

    private static boolean isApiMatched(List<String> apiPathsToBeValidated, MuRequest muRequest) {
        return apiPathsToBeValidated.stream().anyMatch(api -> muRequest.uri().getPath().equals(api));
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
