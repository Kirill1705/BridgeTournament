package thor.bridge_tournament.core.service;

import lombok.AllArgsConstructor;
import thor.bridge_tournament.core.domain.BoardEntryManager;
import thor.bridge_tournament.core.domain.CurrentTournamentManager;
import thor.bridge_tournament.core.domain.tournament.Tournament;
import thor.bridge_tournament.core.domain.tournament.TournamentNode;
import thor.bridge_tournament.core.exception.BoardNotFoundException;
import thor.bridge_tournament.core.port.dto.board.BoardDto;
import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.input.TournamentBoardEntryService;
import thor.bridge_tournament.core.port.output.TransactionalManager;
import thor.bridge_tournament.core.port.output.repository.BoardRepository;
import thor.bridge_tournament.core.port.output.repository.TournamentNodeRepository;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class TournamentBoardEntryServiceImpl implements TournamentBoardEntryService {
    private final CurrentTournamentManager currentTournamentManager;
    private final TournamentNodeRepository tournamentNodeRepository;
    private final TransactionalManager transactionalManager;
    private final BoardEntryManager boardEntryManager;

    @Override
    public PairBoardResult addTournamentBoardEntry(String userName, int boardNumber, RawBoardEntry entry) {
        UUID playerId = currentTournamentManager.getUserIdOrRegister(userName);
        Tournament tournament = currentTournamentManager.getByPlayerId(userName);
        Optional<BoardDto> boardDto = boardEntryManager.getBoardRepository().findByTournament(tournament.getUuid(), boardNumber);
        if (boardDto.isEmpty()) {
            throw new BoardNotFoundException(boardNumber);
        }
        Optional<TournamentNode> node = tournamentNodeRepository.findNode(playerId, tournament.getUuid(), boardNumber);
        return transactionalManager.executeTransactional(() -> {
            PairBoardResult result = boardEntryManager.addBoardEntry(userName, boardDto.get().id(), entry, tournament.getCountType().createCalculator(), node.get().ns(), node.get().ew(), tournament.getCountType().getFormatStandardName());
            tournamentNodeRepository.addEntry(result.entryId(), node.get().id());
            return result;
        });
    }
}
