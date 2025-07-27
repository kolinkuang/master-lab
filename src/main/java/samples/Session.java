package samples;

import org.json.JSONObject;

import java.time.Instant;

public record Session(
    String username,
    String cookie,
    String hash,
    Instant expiredAt
) {
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("username", username);
//        json.put("cookie", cookie);
        json.put("hash", hash);
        json.put("expiredAt", expiredAt.toString());
        return json;
    }
}
