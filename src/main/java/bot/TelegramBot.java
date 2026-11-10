package bot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// Long polling позволяет запускать бота без публичного веб-сервера.
public class TelegramBot {
    private final HttpClient client;
    private final String apiUrl;
    private final ProgressService service;

    public TelegramBot(String token, String proxyHost, int proxyPort, ProgressService service) {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("Не задан TELEGRAM_BOT_TOKEN");
        HttpClient.Builder builder = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15));
        if (proxyHost != null && !proxyHost.isBlank()) {
            if (proxyPort < 1 || proxyPort > 65535) throw new IllegalArgumentException("Неверный порт прокси");
            builder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)));
        }
        client = builder.build();
        apiUrl = "https://api.telegram.org/bot" + token + "/";
        this.service = service;
    }

    private JsonObject request(String method, JsonObject body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl + method))
                .timeout(Duration.ofSeconds(45)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        if (response.statusCode() != 200 || !json.get("ok").getAsBoolean()) {
            throw new IOException("Ошибка Telegram API: HTTP " + response.statusCode());
        }
        return json;
    }

    private void send(long chatId, String text) throws IOException, InterruptedException {
        // Большую сводку отправляем частями.
        while (!text.isEmpty()) {
            int end = Math.min(text.length(), 4000);
            if (end < text.length() && Character.isHighSurrogate(text.charAt(end - 1))) end--;
            JsonObject body = new JsonObject();
            body.addProperty("chat_id", chatId);
            body.addProperty("text", text.substring(0, end));
            request("sendMessage", body);
            text = text.substring(end);
        }
    }

    public void run() throws InterruptedException {
        long offset = 0;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                JsonObject params = new JsonObject();
                params.addProperty("offset", offset);
                params.addProperty("timeout", 30);
                JsonArray updates = request("getUpdates", params).getAsJsonArray("result");
                for (var item : updates) {
                    JsonObject update = item.getAsJsonObject();
                    long nextOffset = update.get("update_id").getAsLong() + 1;
                    if (!update.has("message")) {
                        offset = nextOffset;
                        continue;
                    }
                    JsonObject message = update.getAsJsonObject("message");
                    if (!message.has("text") || !message.has("from") || !message.has("chat")) {
                        offset = nextOffset;
                        continue;
                    }
                    JsonObject chat = message.getAsJsonObject("chat");
                    if (!"private".equals(chat.get("type").getAsString())) {
                        offset = nextOffset;
                        continue;
                    }
                    JsonObject from = message.getAsJsonObject("from");
                    String username = from.has("username") ? from.get("username").getAsString() : null;
                    String answer;
                    try {
                        answer = service.handle(from.get("id").getAsLong(), username, message.get("text").getAsString());
                    } catch (IOException e) {
                        System.err.println("Ошибка чтения таблицы: " + e.getMessage());
                        answer = "Не удалось прочитать таблицу. Попробуйте позже.";
                    }
                    send(chat.get("id").getAsLong(), answer);
                    offset = nextOffset;
                }
            } catch (IOException | RuntimeException e) {
                // URL запроса содержит токен, поэтому не выводим текст ошибки.
                System.err.println("Ошибка связи с Telegram. Повтор через 5 секунд.");
                Thread.sleep(5000);
            }
        }
    }
}
