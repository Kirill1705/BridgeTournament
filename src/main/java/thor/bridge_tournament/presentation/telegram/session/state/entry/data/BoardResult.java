package thor.bridge_tournament.presentation.telegram.session.state.entry.data;

import lombok.Getter;

public class BoardResult {
    @Getter
    private final int result;

    public BoardResult(int result) {
        this.result = result;
    }

    public String toFormattedString() {
        if (result == 0) {
            return "=";
        }
        else if (result > 0) {
            return "+" + result;
        }
        return String.valueOf(result);
    }
}
