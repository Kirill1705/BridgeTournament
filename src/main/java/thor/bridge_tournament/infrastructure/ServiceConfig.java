package thor.bridge_tournament.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import thor.bridge_tournament.core.domain.BoardEntryManager;
import thor.bridge_tournament.core.domain.CurrentTournamentManager;
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
    public MovementService movementService(TournamentNodeRepository tournamentNodeRepository, TransactionalManager transactionalManager, CurrentTournamentManager currentTournamentManager, MovementRepository movementRepository) {
        return new MovementServiceImpl(
                currentTournamentManager,
                tournamentNodeRepository,
                transactionalManager,
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
    public UserService userService(UserRepository userRepository) {
        return new UserServiceImpl(userRepository);
    }

    @Bean
    public TournamentService tournamentService(PairRepository pairRepository, MovementRepository movementRepository, ConfirmationSender confirmationSender, TournamentNodeRepository tournamentNodeRepository, TransactionalManager transactionalManager, BoardRepository boardRepository, CurrentTournamentManager currentTournamentManager) {
        return new TournamentServiceImpl(
                currentTournamentManager,
                confirmationSender,
                transactionalManager,
                movementRepository,
                boardRepository,
                tournamentNodeRepository,
                pairRepository
        );
    }

    @Bean
    public TournamentBoardEntryService tournamentBoardEntryService(CurrentTournamentManager currentTournamentManager, TournamentNodeRepository tournamentNodeRepository, TransactionalManager transactionalManager, BoardEntryManager boardEntryManager) {
        return new TournamentBoardEntryServiceImpl(
                currentTournamentManager,
                tournamentNodeRepository,
                transactionalManager,
                boardEntryManager
        );
    }

    @Bean
    public BoardEntryManager boardEntryManager(CurrentTournamentManager currentTournamentManager, BoardEntryRepository boardEntryRepository, BoardRepository boardRepository) {
        return new BoardEntryManager(
                currentTournamentManager,
                boardEntryRepository,
                boardRepository
        );
    }

    @Bean
    public BoardEntryService boardEntryService(BoardEntryManager boardEntryManager) {
        return new BoardEntryServiceImpl(boardEntryManager);
    }
}
