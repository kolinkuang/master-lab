package samples;

import io.muserver.CookieBuilder;
import io.muserver.MuRequest;
import io.muserver.MuResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionHandler {

    private static final String COOKIE_NAME = "LAB-SESSION";

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public void login(MuRequest muRequest, MuResponse muResponse, Map<String, String> pathParams) throws IOException {
        final String username = muRequest.form().get("username");
        final String password = muRequest.form().get("password");

        final String cookie = random2(256);
        final Instant expiredAt = Instant.now().plus(2, ChronoUnit.HOURS);
        final String hash = sha256(cookie);
        final Session session = new Session(username, cookie, hash, expiredAt);

        sessions.put(hash, session);

        muResponse.addCookie(CookieBuilder.newSecureCookie()
                        .withName(COOKIE_NAME)
                        .withValue(cookie)
                        .withMaxAgeInSeconds(2 * 60 * 60) // 2 hours
                        .withPath("/")
                        .build());

        muResponse.status(302);
        muResponse.headers().set("Location", "/web/index.html");
    }

    private static String random(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomChar = (int) (Math.random() * 26) + 'a';
            sb.append((char) randomChar);
        }
        return sb.toString();
    }

    private static String random2(int length) {
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * characters.length());
            sb.append(characters.charAt(randomIndex));
        }
        return sb.toString();
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
