package thor.bridge_tournament.presentation.telegram.exception;

import jdk.jshell.spi.ExecutionControl;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;
import thor.bridge_tournament.core.exception.*;

@Component
public class ExceptionHandler {
    public String handle(Exception exception) {
        return switch (exception) {
            case NotImplementedException e -> "Эта функция пока что не реализована";
            case DomainValidationException e -> "Неправильные входные данные: \"" + e.getLocalizedMessage() + "\"";
            case BoardNotFoundException e -> String.format("Сдача с номером %d не найдена", e.getBoardNumber());
            case MovementNotFoundException e -> "К сожалению движение для турнира с такими параметрами пока не добавлено, задайте другие параметры турнира";
            case TournamentNotFoundException e -> "Вы не участвуете сейчас ни в одном турнире";
            case DealsUniqueMovementException e -> String.format("Пара %d играет дважды сдачу %d", e.getPair(), e.getBoard());
            case PairTableUniqueException e -> String.format("Папа %d в течении раунда %d сидит сразу за двумя столами", e.getPair(), e.getRound());
            case PairUniqueMovementException e -> String.format("Пары %d и %d играют дважды друг с другом", e.getFirst(), e.getSecond());
            case TheSameBoardInRoundException e -> String.format("Сдача %d играется на нескольких столах в течении раунда", e.getBoard());
            case TheSameTableInRoundException e -> String.format("Стол %d участвует дважды в раунде %d", e.getTable(), e.getRound());
            default -> "Возникла ошибка, попробуйте снова с другими данными";
        };
    }
}
