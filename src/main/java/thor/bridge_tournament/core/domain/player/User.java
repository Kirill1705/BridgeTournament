package thor.bridge_tournament.core.domain.player;

import lombok.Setter;

import java.util.UUID;

public final class User {
    @Setter
    private UUID uuid;
    private final String name;
    private final String surname;
    private final SportCategory category;
    private final String username;

    public User(UUID uuid, String name, String surname, SportCategory category, String username) {
        this.uuid = uuid;
        this.name = name;
        this.surname = surname;
        this.category = category;
        this.username = username;
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public String surname() {
        return surname;
    }

    public SportCategory category() {
        return category;
    }

    public String username() {
        return username;
    }
}
