package thor.bridge_tournament.core.port.output;

import java.util.function.Supplier;

public interface TransactionalManager {
    <T> T executeTransactional(Supplier<T> action);
    void executeTransactional(Runnable action);
}
