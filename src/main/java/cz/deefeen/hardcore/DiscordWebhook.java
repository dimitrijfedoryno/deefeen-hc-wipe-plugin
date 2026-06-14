package cz.deefeen.hardcore;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class DiscordWebhook {

    private final String url;
    private final String botName;
    private final String avatarUrl;

    public DiscordWebhook(String url, String botName, String avatarUrl) {
        this.url = url;
        this.botName = botName;
        this.avatarUrl = avatarUrl;
    }

    public void send(String message) {
        if (url == null || url.isBlank()) return;
        try {
            String json = buildJson(message);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
            }

            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception ignored) {
        }
    }

    public void sendEmbed(String title, String description, int color, String... fields) {
        if (url == null || url.isBlank()) return;
        try {
            StringBuilder fieldsJson = new StringBuilder();
            for (int i = 0; i < fields.length; i += 2) {
                if (i + 1 >= fields.length) break;
                fieldsJson.append("{\"name\":\"").append(escape(fields[i]))
                        .append("\",\"value\":\"").append(escape(fields[i + 1]))
                        .append("\",\"inline\":true},");
            }
            if (fieldsJson.length() > 0) {
                fieldsJson.setLength(fieldsJson.length() - 1);
            }

            String json = "{" +
                    "\"username\":\"" + escape(botName != null ? botName : "HardcoreWipe") + "\"," +
                    (avatarUrl != null && !avatarUrl.isBlank() ?
                            "\"avatar_url\":\"" + escape(avatarUrl) + "\"," : "") +
                    "\"embeds\":[{" +
                    "\"title\":\"" + escape(title) + "\"," +
                    "\"description\":\"" + escape(description) + "\"," +
                    "\"color\":" + color + "," +
                    "\"timestamp\":\"" + Instant.now().toString() + "\"," +
                    "\"fields\":[" + fieldsJson + "]" +
                    "}]}";

            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
            }

            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception ignored) {
        }
    }

    private String buildJson(String message) {
        return "{" +
                "\"content\":\"" + escape(message) + "\"," +
                "\"username\":\"" + escape(botName != null ? botName : "HardcoreWipe") + "\"" +
                (avatarUrl != null && !avatarUrl.isBlank() ?
                        ",\"avatar_url\":\"" + escape(avatarUrl) + "\"" : "") +
                "}";
    }

    private String escape(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
