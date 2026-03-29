package thor.bridge_tournament.core.type.player;

import java.util.UUID;

public record Player(UUID uuid, String name, String surname, SportCategory category, String username) {
}
