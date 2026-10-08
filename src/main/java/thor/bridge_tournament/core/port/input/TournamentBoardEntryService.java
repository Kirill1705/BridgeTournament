package thor.bridge_tournament.core.port.input;

import thor.bridge_tournament.core.port.dto.board.PairBoardResult;
import thor.bridge_tournament.core.port.dto.board.RawBoardEntry;

import java.util.UUID;

public interface TournamentBoardEntryService {
    /**
     * @param userId внутренний идентификатор игрока, отправившего результат; не идентификатор судьи или партнёра
     */
    PairBoardResult addTournamentBoardEntryForPlayer(UUID userId, int boardNumber, RawBoardEntry entry);

    /**
     * Судья может ввести или перезаписать результат встречи в своём текущем турнире.
     * Проверяется, что игроки представляют противоположные пары встречи на указанной сдаче.
     * При перезаписи сохраняется идентификатор записи, автором становится судья.
     * @param tdId внутренний идентификатор судьи, отправившего результат
     * @param boardNumber номер сдачи в турнире
     * @param entry контракт, разыгрывающий, атака и результат
     * @param firstPairPlayerId идентификатор любого игрока первой пары
     * @param secondPairPlayerId идентификатор любого игрока пары оппонентов; порядок NS/EW не важен
     * @return результат и пересчитанный протокол сдачи в этом турнире
     */
    PairBoardResult addTournamentBoardEntryByTd(UUID tdId, int boardNumber, RawBoardEntry entry, UUID firstPairPlayerId, UUID secondPairPlayerId);
}
