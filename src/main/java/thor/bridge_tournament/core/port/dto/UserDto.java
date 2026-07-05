package thor.bridge_tournament.core.port.dto;

import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        String name,
        String surname,
        Double sportCategory
) {
}
