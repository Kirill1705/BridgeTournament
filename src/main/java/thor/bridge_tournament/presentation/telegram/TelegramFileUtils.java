package thor.bridge_tournament.presentation.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class TelegramFileUtils {
    private final String botToken;

    public TelegramFileUtils(@Value("${bot.token}") String botToken) {
        this.botToken = botToken;
    }

    public String downloadFileAndRead(String fileId, TelegramClient client) {
        GetFile getFile = GetFile.builder()
                .fileId(fileId)
                .build();
        try {
            File file = client.execute(getFile);
            String path = file.getFilePath();
            String url = createUrl(path);
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return response.body();
        } catch (TelegramApiException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String createUrl(String path) {
        return "https://api.telegram.org/file/bot" + botToken + "/" + path;
    }
}
