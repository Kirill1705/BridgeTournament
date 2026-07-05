package thor.bridge_tournament.core.service;

import lombok.AllArgsConstructor;
import thor.bridge_tournament.core.domain.board.Board;
import thor.bridge_tournament.core.mapping.BoardMapper;
import thor.bridge_tournament.core.port.input.BoardService;
import thor.bridge_tournament.core.port.output.repository.BoardRepository;

@AllArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;

    @Override
    public int addBoard(int number) {
        Board board = new Board(number);
        return boardRepository.save(BoardMapper.toDto(board));
    }
}
