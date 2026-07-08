package thor.bridge_tournament.core.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import thor.bridge_tournament.core.domain.BoardEntryManager;
import thor.bridge_tournament.core.domain.CurrentTournamentManager;
import thor.bridge_tournament.core.domain.board.Board;
import thor.bridge_tournament.core.domain.board.BoardEntry;
import thor.bridge_tournament.core.domain.movement.Movement;
import thor.bridge_tournament.core.domain.movement.MovementValidator;
import thor.bridge_tournament.core.domain.tournament.*;
import thor.bridge_tournament.core.exception.BoardNotFoundException;
import thor.bridge_tournament.core.mapping.BoardEntryMapper;
import thor.bridge_tournament.core.mapping.BoardMapper;
import thor.bridge_tournament.core.mapping.MovementMapper;
import thor.bridge_tournament.core.port.dto.UserDto;
import thor.bridge_tournament.core.port.dto.board.BoardDto;
import thor.bridge_tournament.core.port.dto.board.BoardEntryDto;
import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.dto.movement.MovementDto;
import thor.bridge_tournament.core.port.dto.movement.PairMovementEntryDto;
import thor.bridge_tournament.core.port.dto.movement.PairMovementNextRoundInfo;
import thor.bridge_tournament.core.port.dto.tournament.PairDto;
import thor.bridge_tournament.core.port.input.MovementService;
import thor.bridge_tournament.core.port.output.TransactionalManager;
import thor.bridge_tournament.core.port.output.repository.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class MovementServiceImpl implements MovementService {
    private final CurrentTournamentManager currentTournamentManager;
    private final TournamentNodeRepository tournamentGamesRepository;
    private final TransactionalManager transactionalManager;
    private final MovementRepository movementRepository;


    @Override
    public List<PairMovementEntryDto> getMovementCard(String userName) {
        Tournament tournament = currentTournamentManager.getByPlayerId(userName);
        UUID playerId = currentTournamentManager.getUserIdOrRegister(userName);
        List<TournamentNode> nodes = tournamentGamesRepository.getAllMovementsForPlayer(playerId, tournament.getUuid());
        return nodes.stream()
                .map(node -> new PairMovementEntryDto(
                        node.round(),
                        getOpponents(node, playerId),
                        node.table(),
                        node.boards()
                ))
                .toList();
    }

    @Override
    public PairMovementNextRoundInfo getMovementNextRound(String userName) {
        Tournament tournament = currentTournamentManager.getByPlayerId(userName);
        UUID playerId = currentTournamentManager.getUserIdOrRegister(userName);
        Optional<TournamentNode> node = tournamentGamesRepository.findNextNodeForPlayer(playerId, tournament.getUuid());
        PairDto opponents = getOpponents(node.get(), playerId);
        return new PairMovementNextRoundInfo(new PairMovementEntryDto(
                node.get().round(),
                opponents,
                node.get().table(),
                node.get().boards()
        ), tournamentGamesRepository.getDealsNotPlayed(node.get().id()));
    }

    @Override
    public void addMovement(MovementDto movementDto) {
        Movement movement = MovementMapper.fromDto(movementDto);
        MovementValidator validator = new MovementValidator();
        validator.validate(movement.getBody());
        Optional<UUID> movementOpt = movementRepository.find(movement.getPairsCount(), movementDto.roundsCount(), movementDto.type());
        transactionalManager.executeTransactional(() -> {
            if (movementOpt.isPresent()) {
                log.warn("Replacing movement");
                movementRepository.delete(movementOpt.get());
            }
            movementRepository.save(MovementMapper.toDto(movement));
        });
    }

    private PairDto getOpponents(TournamentNode node, UUID playerId) {
        if (node.ns().firstPlayer().id().equals(playerId) || node.ns().secondPlayer().id().equals(playerId)) {
            return node.ew();
        }
        return node.ns();
    }

}
