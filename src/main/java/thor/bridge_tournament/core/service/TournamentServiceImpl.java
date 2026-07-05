package thor.bridge_tournament.core.service;

import lombok.AllArgsConstructor;
import thor.bridge_tournament.core.domain.board.Board;
import thor.bridge_tournament.core.domain.movement.AverageBoardSelector;
import thor.bridge_tournament.core.domain.movement.Movement;
import thor.bridge_tournament.core.domain.tournament.CountType;
import thor.bridge_tournament.core.domain.tournament.CurrentTournamentManager;
import thor.bridge_tournament.core.domain.tournament.Tournament;
import thor.bridge_tournament.core.domain.tournament.TournamentNode;
import thor.bridge_tournament.core.exception.MovementNotFoundException;
import thor.bridge_tournament.core.mapping.BoardMapper;
import thor.bridge_tournament.core.mapping.MovementMapper;
import thor.bridge_tournament.core.mapping.TournamentMapper;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.dto.movement.MovementDto;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;
import thor.bridge_tournament.core.port.dto.tournament.TournamentDto;
import thor.bridge_tournament.core.port.dto.tournament.TournamentPlayers;
import thor.bridge_tournament.core.port.input.TournamentService;
import thor.bridge_tournament.core.port.output.ConfirmationSender;
import thor.bridge_tournament.core.port.output.TransactionalManager;
import thor.bridge_tournament.core.port.output.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TournamentServiceImpl implements TournamentService {
    private final TournamentRepository tournamentRepository;
    private final CurrentTournamentRepository currentTournamentRepository;
    private final CurrentTournamentManager currentTournamentManager;
    private final ConfirmationSender confirmationSender;
    private final UserRepository userRepository;
    private final TransactionalManager transactionalManager;
    private final MovementRepository movementRepository;
    private final BoardRepository boardRepository;
    private final TournamentNodeRepository tournamentGamesRepository;
    private final PairRepository pairRepository;

    @Override
    public void createTournament(String ownerUserName, int boards, String countType, String name, Integer rounds) {
        UUID ownerId = currentTournamentManager.getUserIdOrRegister(ownerUserName);
        Tournament tournament = new Tournament(ownerId, boards, rounds, CountType.fromStandardName(countType), name);
        TournamentDto tournamentDto = TournamentMapper.toDto(tournament);
        UUID uuid = tournamentRepository.save(tournamentDto);
        currentTournamentRepository.switchTd(uuid, ownerId);
    }

    @Override
    public void addTournamentDirector(String ownerUserName, String tdUserName) {
        Tournament tournament = currentTournamentManager.getByTd(ownerUserName);
        UUID tdId = currentTournamentManager.getUserIdOrRegister(tdUserName);
        tournament.addTd(tdId);
        tournamentRepository.save(TournamentMapper.toDto(tournament));
    }

    @Override
    public void addPlayer(String ownerUserName, String playerUserName) {
        Tournament tournament = currentTournamentManager.getByTd(ownerUserName);
        tournamentRepository.addPlayerWithoutPair(currentTournamentManager.getUserIdOrRegister(playerUserName), tournament.getUuid());
    }

    @Override
    public void addPair(String ownerUserName, String initiatorUserName, String partnerUserName) {
        Tournament tournament = currentTournamentManager.getByTd(ownerUserName);
        addPairToTournament(
                tournament.getUuid(),
                userRepository.getById(currentTournamentManager.getUserIdOrRegister(initiatorUserName)),
                userRepository.getById(currentTournamentManager.getUserIdOrRegister(partnerUserName))
        );
    }

    @Override
    public List<String> getTds(String tdUserName) {
        Tournament tournament = currentTournamentManager.getByTd(tdUserName);
        return tournament.getTds().stream()
                .map(uuid -> userRepository.getById(uuid).name())
                .toList();
    }

    @Override
    public TournamentPlayers getAllPlayers(String tdUserName) {
        Tournament tournament = currentTournamentManager.getByTd(tdUserName);
        List<UserDto> playersWithoutPairs = userRepository.filterByIds(tournamentRepository.getPlayersWithoutPair(tournament.getUuid()));
        List<PairDto> pairs = pairRepository.filterByIds(tournamentRepository.getAllPairs(tournament.getUuid()));
        return new TournamentPlayers(playersWithoutPairs, pairs);
    }

    @Override
    public void startTournament(String ownerUserName) {
        Tournament tournament = currentTournamentManager.getByTd(ownerUserName);
        List<UserDto> playersWithOutPair = userRepository.filterByIds(tournamentRepository.getPlayersWithoutPair(tournament.getUuid()));
        if (playersWithOutPair.size() % 2 != 0) {
            confirmationSender.oddNumberOfPlayers(playersWithOutPair.removeLast().username(), tournament.getName());
        }
        transactionalManager.executeTransactional(() -> {
            for (int i = 0; i < playersWithOutPair.size(); i += 2) {
                addPairToTournament(tournament.getUuid(), playersWithOutPair.get(i), playersWithOutPair.get(i + 1));
            }
            tournamentRepository.removePlayersWithOutPairs(tournament.getUuid());
        });

        List<PairDto> pairs = pairRepository.filterByIds(tournamentRepository.getAllPairs(tournament.getUuid()));
        int pairsCount = pairs.size() % 2 == 0 ? pairs.size() : pairs.size() + 1;
        Optional<MovementDto> movementDto = movementRepository.find(pairsCount, tournament.getPossibleRoundsQuantity());
        if (movementDto.isEmpty()) {
            throw new MovementNotFoundException(pairsCount);
        }
        Movement movement = MovementMapper.fromDto(movementDto.get());
        List<TournamentNode> tournamentNodes = movement.scheduleTournament(
                new AverageBoardSelector(getBoards(tournament), movement.getRoundsCount()),
                pairs,
                tournament.getUuid()
        );
        transactionalManager.executeTransactional(() -> {
            tournamentGamesRepository.addNodes(tournamentNodes);
            tournament.start();
            tournamentRepository.save(TournamentMapper.toDto(tournament));
        });
    }

    private List<Integer> getBoards(Tournament tournament) {
        List<Board> alreadyAdded = boardRepository.getBoardsForTournament(tournament.getUuid()).stream()
                .map(BoardMapper::fromDto)
                .toList();
        List<Board> added = tournament.generateBoards(alreadyAdded.stream()
                .map(Board::getNumber)
                .collect(Collectors.toSet()));
        transactionalManager.executeTransactional(() -> {
            for (Board board: added) {
                boardRepository.save(BoardMapper.toDto(board));
                boardRepository.addBoardToTournament(board.getId(), tournament.getUuid());
            }
        });
        List<Board> allBoards = new ArrayList<>(alreadyAdded);
        allBoards.addAll(added);
        return allBoards.stream().map(Board::getNumber).toList();
    }

    @Override
    public void addBoardToTournament(int boardId, String tdUserName) {
        Tournament tournament = currentTournamentManager.getByTd(tdUserName);
        boardRepository.addBoardToTournament(boardId, tournament.getUuid());
    }

    private void addPairToTournament(UUID tournamentId, UserDto firstPlayer, UserDto secondPlayer) {
        PairDto pair = new PairDto(null, firstPlayer, secondPlayer);
        UUID pairId = pairRepository.addPair(pair);
        tournamentRepository.addPair(pairId, tournamentId);
    }
}
