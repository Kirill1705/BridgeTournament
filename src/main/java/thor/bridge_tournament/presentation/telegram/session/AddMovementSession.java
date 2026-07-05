package thor.bridge_tournament.presentation.telegram.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.domain.movement.MovementBodyNode;
import thor.bridge_tournament.core.port.dto.movement.MovementDto;
import thor.bridge_tournament.core.port.input.MovementService;
import thor.bridge_tournament.presentation.telegram.TelegramFileUtils;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class AddMovementSession implements UserSession {
    private final MovementService movementService;
    private final TelegramFileUtils fileUtils;

    @Override
    public boolean handleMessage(Update update, TelegramClient client) {
        String jsonString = readFile(update, client);
        ObjectMapper mapper = new ObjectMapper();
        try {
            MovementAddRequest request = mapper.readValue(jsonString, MovementAddRequest.class);
            movementService.addMovement(new MovementDto(
                    request.type,
                    request.rounds,
                    request.pairs,
                    parseBody(request.body, request.pairs / 2)
            ));
            SendMessage message = SendMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .text("Движение добавлено успешно")
                    .build();
            client.execute(message);
            return true;
        } catch (JsonProcessingException | TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private List<MovementBodyNode> parseBody(int[][] data, int tablesCount) {
        List<MovementBodyNode> nodes = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            nodes.add(new MovementBodyNode(data[i][0], data[i][1], data[i][2], i / tablesCount + 1, i % tablesCount));
        }
        return nodes;
    }

    private String readFile(Update update, TelegramClient client) {
        Document document = update.getMessage().getDocument();
        return fileUtils.downloadFileAndRead(document.getFileId(), client);
    }

    @Data
    public static class MovementAddRequest {
        private String type;

        private Integer rounds;

        private Integer pairs;

        private int[][] body;
    }
}
