package thor.bridge_tournament.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import thor.bridge_tournament.core.domain.tournament.CurrentTournamentManager;
import thor.bridge_tournament.core.port.input.*;
import thor.bridge_tournament.core.port.output.ConfirmationSender;
import thor.bridge_tournament.core.port.output.TransactionalManager;
import thor.bridge_tournament.core.port.output.repository.*;
import thor.bridge_tournament.core.service.*;

@Configuration
public class ServiceConfig {
    @Bean
    public CurrentTournamentManager currentTournamentManager(UserRepository userRepository, CurrentTournamentRepository currentTournamentRepository, TournamentRepository tournamentRepository) {
        return new CurrentTournamentManager(userRepository, currentTournamentRepository, tournamentRepository);
    }

    @Bean
    public BoardService boardService(BoardRepository repository) {
        return new BoardServiceImpl(repository);
    }

    @Bean
    public MovementService movementService(TournamentNodeRepository tournamentNodeRepository, UserRepository userRepository, BoardEntryRepository boardEntryRepository, TransactionalManager transactionalManager, BoardRepository boardRepository, CurrentTournamentManager currentTournamentManager, MovementRepository movementRepository) {
        return new MovementServiceImpl(
                currentTournamentManager,
                tournamentNodeRepository,
                userRepository,
                boardEntryRepository,
                transactionalManager,
                boardRepository,
                movementRepository
                );
    }

    @Bean
    public TournamentResultService tournamentResultService(BoardEntryRepository boardEntryRepository, CurrentTournamentManager manager) {
        return new TournamentResultServiceImpl(
                manager,
                boardEntryRepository
        );
    }

    @Bean
    public UserService userService(UserRepository userRepository, TransactionalManager transactionalManager) {
        return new UserServiceImpl(userRepository, transactionalManager);
    }

    @Bean
    public TournamentService tournamentService(PairRepository pairRepository, MovementRepository movementRepository, ConfirmationSender confirmationSender, CurrentTournamentRepository currentTournamentRepository, TournamentRepository tournamentRepository, TournamentNodeRepository tournamentNodeRepository, UserRepository userRepository, TransactionalManager transactionalManager, BoardRepository boardRepository, CurrentTournamentManager currentTournamentManager) {
        return new TournamentServiceImpl(
                tournamentRepository,
                currentTournamentRepository,
                currentTournamentManager,
                confirmationSender,
                userRepository,
                transactionalManager,
                movementRepository,
                boardRepository,
                tournamentNodeRepository,
                pairRepository
        );
    }
}
