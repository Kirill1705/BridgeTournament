package thor.bridge_tournament.core.service;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import thor.bridge_tournament.core.domain.BoardEntryManager;
import thor.bridge_tournament.core.domain.tournament.CountType;
import thor.bridge_tournament.core.domain.tournament.ImpTranslationScale;
import thor.bridge_tournament.core.domain.tournament.MedianImpsCalculator;
import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;
import thor.bridge_tournament.core.port.input.BoardEntryService;

import java.util.UUID;

@AllArgsConstructor
public class BoardEntryServiceImpl implements BoardEntryService {
    private final BoardEntryManager boardEntryManager;

    @Override
    public PairBoardResult addBoardEntryImps(UUID userId, int boardId, RawBoardEntry entry, Double throwAwayPercent) {
        if (throwAwayPercent == null) {
            throwAwayPercent = 0d;
        }
        return boardEntryManager.addBoardEntry(userId, boardId, entry, new MedianImpsCalculator(throwAwayPercent, ImpTranslationScale.createDefault()), null, null, CountType.MEDIAN_IMPS.getFormatStandardName());
    }

    @Override
    public PairBoardResult addBoardEntryMp(UUID userId, int boardId, RawBoardEntry entry) {
        return boardEntryManager.addBoardEntry(userId, boardId, entry, CountType.MP.createCalculator(),
                null, null, CountType.MP.getFormatStandardName());
    }
}
