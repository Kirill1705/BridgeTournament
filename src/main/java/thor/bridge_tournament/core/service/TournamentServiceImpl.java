package thor.bridge_tournament.core.service;

import lombok.AllArgsConstructor;
import thor.bridge_tournament.core.domain.board.Board;
import thor.bridge_tournament.core.domain.movement.AverageBoardSelector;
import thor.bridge_tournament.core.domain.movement.Movement;
import thor.bridge_tournament.core.domain.tournament.CountType;
import thor.bridge_tournament.core.domain.CurrentTournamentManager;
import thor.bridge_tournament.core.domain.tournament.Tournament;
import thor.bridge_tournament.core.domain.tournament.TournamentNode;
import thor.bridge_tournament.core.exception.MovementNotFoundException;
import thor.bridge_tournament.core.exception.DomainValidationException;
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
    private final CurrentTournamentManager currentTournamentManager;
    private final ConfirmationSender confirmationSender;
    private final TransactionalManager transactionalManager;
    private final MovementRepository movementRepository;
    private final BoardRepository boardRepository;
    private final TournamentNodeRepository tournamentGamesRepository;
    private final PairRepository pairRepository;

    @Override
    public void createTournament(UUID ownerId, int boards, String countType, String name, Integer rounds) {
        Tournament tournament = new Tournament(ownerId, boards, rounds, CountType.fromStandardName(countType), name);
        TournamentDto tournamentDto = TournamentMapper.toDto(tournament);
        UUID uuid = currentTournamentManager.getTournamentRepository().save(tournamentDto);
        currentTournamentManager.getCurrentTournamentRepository().switchTd(uuid, ownerId);
    }

    @Override
    public void addTournamentDirector(UUID ownerId, UUID tdId) {
        Tournament tournament = currentTournamentManager.getByTd(ownerId);
        tournament.addTd(tdId);
        transactionalManager.executeTransactional(() -> {
            currentTournamentManager.getTournamentRepository().save(TournamentMapper.toDto(tournament));
            currentTournamentManager.getCurrentTournamentRepository().switchTd(tournament.getUuid(), tdId);
        });
    }

    @Override
    public void addPlayer(UUID ownerId, UUID playerId) {
        Tournament tournament = currentTournamentManager.getByTd(ownerId);
        transactionalManager.executeTransactional(() -> {
            currentTournamentManager.getTournamentRepository().addPlayerWithoutPair(playerId, tournament.getUuid());
            currentTournamentManager.getCurrentTournamentRepository().switchPlayer(tournament.getUuid(), playerId);
        });
    }

    @Override
    public void addPair(UUID ownerId, UUID initiatorId, UUID partnerId) {
        transactionalManager.executeTransactional(() -> {
            Tournament tournament = currentTournamentManager.getByTd(ownerId);
            if (!tournament.getTds().contains(ownerId)) {
                throw new DomainValidationException("Добавлять пары может только судья турнира");
            }
            if (tournament.isStarted()) {
                throw new DomainValidationException("Нельзя добавлять пары после начала турнира");
            }
            if (initiatorId.equals(partnerId)) {
                throw new DomainValidationException("В паре должны быть два разных игрока");
            }
            var pairs = pairRepository.filterByIds(currentTournamentManager.getTournamentRepository().getAllPairs(tournament.getUuid()));
            if (pairs.stream().anyMatch(pair -> pair.firstPlayer().id().equals(initiatorId)
                    || pair.secondPlayer().id().equals(initiatorId) || pair.firstPlayer().id().equals(partnerId)
                    || pair.secondPlayer().id().equals(partnerId))) {
                throw new DomainValidationException("Один из игроков уже состоит в паре этого турнира");
            }
            addPairToTournament(tournament.getUuid(), currentTournamentManager.getUserRepository().getById(initiatorId),
                    currentTournamentManager.getUserRepository().getById(partnerId));
        });
    }

    @Override
    public List<UserDto> removePlayer(UUID tdId, UUID playerId) {
        return transactionalManager.executeTransactional(() -> {
            Tournament tournament = currentTournamentManager.getByTd(tdId);
            if (!tournament.getTds().contains(tdId)) {
                throw new DomainValidationException("Удалять участников может только судья турнира");
            }
            if (tournament.isStarted()) {
                throw new DomainValidationException("Нельзя удалять участников после начала турнира");
            }
            var repository = currentTournamentManager.getTournamentRepository();
            var pair = pairRepository.filterByIds(repository.getAllPairs(tournament.getUuid())).stream()
                    .filter(candidate -> candidate.firstPlayer().id().equals(playerId)
                            || candidate.secondPlayer().id().equals(playerId)).findFirst();
            List<UUID> removedIds;
            if (pair.isPresent()) {
                var selected = pair.get();
                removedIds = List.of(selected.firstPlayer().id(), selected.secondPlayer().id()).stream().distinct().toList();
                repository.removePair(selected.id(), tournament.getUuid());
            } else {
                if (!repository.getPlayersWithoutPair(tournament.getUuid()).contains(playerId)) {
                    throw new DomainValidationException("Этот игрок не участвует в текущем турнире");
                }
                removedIds = List.of(playerId);
            }
            repository.removePlayersWithoutPair(tournament.getUuid(), removedIds);
            currentTournamentManager.getCurrentTournamentRepository().clearPlayers(tournament.getUuid(), removedIds);
            return currentTournamentManager.getUserRepository().filterByIds(removedIds);
        });
    }

    @Override
    public List<UserDto> getTds(UUID tdId) {
        Tournament tournament = currentTournamentManager.getByTd(tdId);
        return currentTournamentManager.getUserRepository().filterByIds(tournament.getTds());
    }

    @Override
    public TournamentPlayers getAllPlayers(UUID tdId) {
        Tournament tournament = currentTournamentManager.getByTd(tdId);
        List<UserDto> playersWithoutPairs = currentTournamentManager.getUserRepository().filterByIds(currentTournamentManager.getTournamentRepository().getPlayersWithoutPair(tournament.getUuid()));
        List<PairDto> pairs = pairRepository.filterByIds(currentTournamentManager.getTournamentRepository().getAllPairs(tournament.getUuid()));
        return new TournamentPlayers(playersWithoutPairs, pairs);
    }

    @Override
    public void startTournament(UUID ownerId) {
        Tournament tournament = currentTournamentManager.getByTd(ownerId);
        List<UserDto> playersWithOutPair = new ArrayList<>(currentTournamentManager.getUserRepository().filterByIds(currentTournamentManager.getTournamentRepository().getPlayersWithoutPair(tournament.getUuid())));
        if (playersWithOutPair.size() % 2 != 0) {
            confirmationSender.oddNumberOfPlayers(playersWithOutPair.removeLast().id(), tournament.getName());
        }
        transactionalManager.executeTransactional(() -> {
            for (int i = 0; i < playersWithOutPair.size(); i += 2) {
                addPairToTournament(tournament.getUuid(), playersWithOutPair.get(i), playersWithOutPair.get(i + 1));
            }
            currentTournamentManager.getTournamentRepository().removePlayersWithOutPairs(tournament.getUuid());
        });

        List<PairDto> pairs = pairRepository.filterByIds(currentTournamentManager.getTournamentRepository().getAllPairs(tournament.getUuid()));
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
            currentTournamentManager.getTournamentRepository().save(TournamentMapper.toDto(tournament));
            for (PairDto pair : pairs) {
                activatePair(tournament.getUuid(), pair.firstPlayer(), pair.secondPlayer());
            }
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
                int boardId = boardRepository.save(BoardMapper.toDto(board));
                boardRepository.addBoardToTournament(boardId, tournament.getUuid());
            }
        });
        List<Board> allBoards = new ArrayList<>(alreadyAdded);
        allBoards.addAll(added);
        return allBoards.stream().map(Board::getNumber).sorted().toList();
    }

    @Override
    public void addBoardToTournament(int boardId, UUID tdId) {
        Tournament tournament = currentTournamentManager.getByTd(tdId);
        boardRepository.addBoardToTournament(boardId, tournament.getUuid());
    }

    private void addPairToTournament(UUID tournamentId, UserDto firstPlayer, UserDto secondPlayer) {
        PairDto pair = new PairDto(null, firstPlayer, secondPlayer);
        UUID pairId = pairRepository.addPair(pair);
        currentTournamentManager.getTournamentRepository().addPair(pairId, tournamentId);
        currentTournamentManager.getTournamentRepository().removePlayersWithoutPair(
                tournamentId, List.of(firstPlayer.id(), secondPlayer.id()));
        activatePair(tournamentId, firstPlayer, secondPlayer);
    }

    private void activatePair(UUID tournamentId, UserDto firstPlayer, UserDto secondPlayer) {
        currentTournamentManager.getCurrentTournamentRepository().switchPlayer(tournamentId, firstPlayer.id());
        currentTournamentManager.getCurrentTournamentRepository().switchPlayer(tournamentId, secondPlayer.id());
    }
}
