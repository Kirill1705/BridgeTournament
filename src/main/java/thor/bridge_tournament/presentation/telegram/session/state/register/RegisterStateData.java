package thor.bridge_tournament.presentation.telegram.session.state.register;

import lombok.Data;

@Data
public class RegisterStateData {
    private String name;

    private String surname;

    private Double sportCategory;
}
