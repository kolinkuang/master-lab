package samples;

import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.SsePublisher;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class TweetHandler {

    private final List<Tweet> tweets = new CopyOnWriteArrayList<>();
    private final List<SsePublisher> ssePublishers = new CopyOnWriteArrayList<>();

    public void createTweet(MuRequest muRequest, MuResponse muResponse, Map<String, String> pathParams) throws IOException {
        String message = muRequest.form().get("message");

        //TODO: Get user name from session
//        Session session = (Session) muRequest.attribute("session");

        Tweet tweet = new Tweet("Kolin", message, Instant.now());
        tweets.add(tweet);

        muResponse.status(201);
        muResponse.write(tweet.toJSON().toString());

        // Publish to SSE through each publisher
        for (SsePublisher publisher: ssePublishers) {
            publisher.send(tweet.toJSON().toString());
        }
    }

    public void getAllTweets(MuRequest muRequest, MuResponse muResponse, Map<String, String> pathParams) {
        List<JSONObject> list = tweets.stream().map(Tweet::toJSON).toList();
        JSONArray jsonArray = new JSONArray(list);

        muResponse.status(200);
        muResponse.write(jsonArray.toString());
    }

    public void sse(MuRequest muRequest, MuResponse muResponse, Map<String, String> pathParams) {
        SsePublisher publisher = SsePublisher.start(muRequest, muResponse);
        ssePublishers.add(publisher);

        muRequest.handleAsync().addResponseCompleteHandler(info -> ssePublishers.remove(publisher));
    }

}
