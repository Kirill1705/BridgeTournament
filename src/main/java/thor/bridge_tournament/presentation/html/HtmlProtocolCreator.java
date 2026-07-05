package thor.bridge_tournament.presentation.html;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import thor.bridge_tournament.core.port.dto.board.BoardEntryDto;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class HtmlProtocolCreator {
    private final Map<String, String> suitsMap = Map.of("S", "♠", "H", "♥", "D", "♦", "C", "♣");

    public File create(Map<BoardEntryDto, Double> protocol, String countType) {
        BoardEntryDto example = protocol.entrySet().stream().findFirst().get().getKey();
        int boardId = example.board().id();
        Document document = Document.createShell("");
        document.outputSettings().prettyPrint();
        document.title("Protocol for board " + boardId);
        document.head().appendElement("meta").attr("charset", "UTF-8");
        Element style = createStyles(document);
        Element boardElement = document.body().appendElement("div").addClass("board-card");
        Element table = boardElement.appendElement("table");
        Element headers = table.appendElement("tr");
        List<String> headerNames = List.of("Contract", "Declarer", "Lead", "Result", "Points", countType, "NS", "EW", "Author");
        for (String header: headerNames) {
            headers.appendElement("th").text(header);
        }
        for (Map.Entry<BoardEntryDto, Double> entry: protocol.entrySet()) {
            createRow(entry.getKey(), table, entry.getValue());
        }
        try {
            File file = new File("protocol.html");
            FileWriter writer = new FileWriter(file);
            writer.write(document.outerHtml());
            writer.close();
            return file;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String replaceSuit(String s) {
        if (s.equals("pass")) {
            return s.toUpperCase();
        }
        s = s.toUpperCase();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            String suit = String.valueOf(s.charAt(i));
            builder.append(suitsMap.getOrDefault(suit, suit));
        }
        return builder.toString();
    }

    private void createRow(BoardEntryDto boardEntry, Element table, double duplicatePoints) {
        Element row = table.appendElement("tr");
        row.appendElement("td").text(replaceSuit(boardEntry.contract()));
        row.appendElement("td").text(boardEntry.declarer() != null ? boardEntry.declarer() : "");
        row.appendElement("td").text(replaceSuit(boardEntry.lead() != null ? boardEntry.lead() : ""));
        row.appendElement("td").text(convertResult(boardEntry.result()));
        row.appendElement("td").text(String.valueOf(boardEntry.points()));
        row.appendElement("td").text(String.valueOf(duplicatePoints));
        row.appendElement("td").text(convertPair(boardEntry.ns()));
        row.appendElement("td").text(convertPair(boardEntry.ew()));
        row.appendElement("td").text(boardEntry.writer().username());
    }

    private String convertResult(int result) {
        if (result > 0) {
            return "+" + result;
        }
        else {
            return String.valueOf(result);
        }
    }

    private String convertPair(PairDto pair) {
        if (pair == null) {
            return "";
        }
        return pair.firstPlayer().username() + " & " + pair.secondPlayer().username();
    }

    private Element createStyles(Document document) {
        Element style = document.head().appendElement("style");
        style.text(
                "body { font-family: Arial, sans-serif; font-size: 13px; color: #333; background: #f9f9f9; }" +
                        ".board-card { background: white; border: 1px solid #ccc; width: 850px; margin: 20px auto; padding: 15px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }" +
                        ".top-section { display: flex; justify-content: space-between; margin-bottom: 15px; font-weight: bold; }" +
                        ".hands-grid { display: grid; grid-template-columns: 80px 100px 80px; grid-template-rows: auto auto auto; align-items: center; justify-content: center; }" +
                        ".hand-n { grid-column: 2; grid-row: 1; text-align: left; }" +
                        ".hand-w { grid-column: 1; grid-row: 2; text-align: left; }" +
                        ".hand-center { grid-column: 2; grid-row: 2; text-align: center; font-weight: bold; border: 1px solid #777; padding: 5px; background: #f0f0f0; margin: 5px; }" +
                        ".hand-e { grid-column: 3; grid-row: 2; text-align: left; }" +
                        ".hand-s { grid-column: 2; grid-row: 3; text-align: left; }" +
                        ".spade { color: black; } .heart { color: red; } .diamond { color: #ff8c00; } .club { color: green; }" +
                        "table { width: 100%; border-collapse: collapse; margin-top: 15px; font-size: 12px; }" +
                        "th, td { border: 1px solid #ddd; padding: 6px; text-align: left; }" +
                        "th { background-color: #e6e6e6; font-weight: bold; }" +
                        "tr:nth-child(even) { background-color: #fcfcfc; }"
        );
        return style;
    }
}
