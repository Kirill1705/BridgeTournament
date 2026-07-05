package thor.bridge_tournament.core.mapping;

import thor.bridge_tournament.core.domain.player.SportCategory;
import thor.bridge_tournament.core.domain.player.User;
import thor.bridge_tournament.core.port.dto.UserDto;

public class UserMapper {
    public static UserDto toDto(User user) {
        return new UserDto(
                user.uuid(),
                user.username(),
                user.name(),
                user.surname(),
                user.category().value()
        );
    }

    public static User fromDto(UserDto dto) {
        return new User(
                dto.id(),
                dto.name(),
                dto.surname(),
                new SportCategory(dto.sportCategory()),
                dto.username()
        );
    }
}
