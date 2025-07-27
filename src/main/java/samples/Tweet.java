package samples;

import org.json.JSONObject;

import java.time.Instant;

public record Tweet(
        String username,
        String message,
        Instant createdAt
) {

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("message", message);
        json.put("createdAt", createdAt.toString());
        return json;
    }

}
