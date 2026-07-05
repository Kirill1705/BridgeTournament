package thor.bridge_tournament.infrastructure;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import thor.bridge_tournament.core.port.output.TransactionalManager;

import java.util.function.Supplier;

@AllArgsConstructor
@Component
public class TransactionalManagerImpl implements TransactionalManager {
    private final TransactionTemplate transactionTemplate;

    @Override
    public <T> T executeTransactional(Supplier<T> action) {
        return transactionTemplate.execute(_ -> action.get());
    }

    @Override
    public void executeTransactional(Runnable action) {
        transactionTemplate.execute(_ -> {
            action.run();
            return null;
        });
    }
}
