package thor.bridge_tournament.presentation.telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.dto.tournament.TournamentPlayers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class TournamentMessages {
    private TournamentMessages() {}

    public static String user(UserDto user) {
        String fullName = Stream.of(user.name(), user.surname())
                .filter(value -> value != null && !value.isBlank())
                .map(value -> shorten(value.strip().replaceAll("\\s+", " "), 80))
                .reduce((first, last) -> first + " " + last).orElse("");
        String username = user.username() == null ? "" : user.username().strip().replaceFirst("^@", "");
        String handle = username.isBlank() ? "" : "@" + shorten(username.replaceAll("\\s+", " "), 64);
        if (fullName.isEmpty()) {
            return handle.isEmpty() ? "Пользователь без имени" : handle;
        }
        return handle.isEmpty() ? fullName : fullName + " (" + handle + ")";
    }

    public static List<UserDto> sortedUsers(List<UserDto> users) {
        return users.stream().sorted(Comparator.comparing(TournamentMessages::user, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(UserDto::id)).toList();
    }

    public static String shorten(String text, int length) {
        return text.codePointCount(0, text.length()) <= length ? text
                : text.substring(0, text.offsetByCodePoints(0, length - 1)) + "…";
    }

    public static void send(TelegramClient client, long chatId, String text) throws TelegramApiException {
        client.execute(SendMessage.builder().chatId(chatId).text(text).build());
    }

    public static void directors(TelegramClient client, long chatId, List<UserDto> directors) throws TelegramApiException {
        var lines = new ArrayList<String>();
        lines.add("Судьи турнира: " + directors.size());
        if (directors.isEmpty()) {
            lines.add("Список судей пуст.");
        }
        int index = 1;
        for (var director : sortedUsers(directors)) {
            lines.add(index++ + ". " + user(director));
        }
        sendLines(client, chatId, lines);
    }

    public static void players(TelegramClient client, long chatId, TournamentPlayers players) throws TelegramApiException {
        var lines = new ArrayList<String>();
        lines.add("Участники турнира: " + (players.pairs().size() * 2 + players.playersWithOutPair().size()));
        lines.add("\nПары: " + players.pairs().size());
        players.pairs().stream().map(pair -> "• " + user(pair.firstPlayer()) + " — " + user(pair.secondPlayer()))
                .sorted(String.CASE_INSENSITIVE_ORDER).forEach(lines::add);
        if (players.pairs().isEmpty()) {
            lines.add("Пары ещё не добавлены.");
        }
        lines.add("\nБез пары: " + players.playersWithOutPair().size());
        sortedUsers(players.playersWithOutPair()).forEach(player -> lines.add("• " + user(player)));
        if (players.playersWithOutPair().isEmpty()) {
            lines.add("Игроков без пары нет.");
        }
        sendLines(client, chatId, lines);
    }

    private static void sendLines(TelegramClient client, long chatId, List<String> lines) throws TelegramApiException {
        var message = new StringBuilder();
        for (String line : lines) {
            if (message.length() + line.length() + 1 > 4000) {
                send(client, chatId, message.toString());
                message.setLength(0);
            }
            if (!message.isEmpty()) {
                message.append('\n');
            }
            message.append(line);
        }
        if (!message.isEmpty()) {
            send(client, chatId, message.toString());
        }
    }
}
