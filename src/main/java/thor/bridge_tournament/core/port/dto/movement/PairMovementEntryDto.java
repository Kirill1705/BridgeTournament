package thor.bridge_tournament.core.port.dto.movement;

import thor.bridge_tournament.core.port.dto.tournament.PairDto;

import java.util.List;

public record PairMovementEntryDto(int round, PairDto opponents, int table, List<Integer> boards) { }
