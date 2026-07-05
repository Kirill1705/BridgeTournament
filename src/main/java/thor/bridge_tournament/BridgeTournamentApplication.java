package thor.bridge_tournament;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BridgeTournamentApplication {
    public static void main(String[] args) {
        SpringApplication.run(BridgeTournamentApplication.class);
    }
}
