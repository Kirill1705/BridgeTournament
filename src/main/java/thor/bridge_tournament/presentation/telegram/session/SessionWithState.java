package thor.bridge_tournament.presentation.telegram.session;

import thor.bridge_tournament.presentation.telegram.session.state.SessionState;

public interface SessionWithState<T> extends UserSession {
    void updateState(SessionState<T> state);

    T getData();
}
