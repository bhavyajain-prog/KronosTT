package com.kronostt.engine.algorithm;

import com.kronostt.engine.model.ScheduledResult;
import com.kronostt.engine.model.ScheduledSession;
import com.kronostt.engine.model.Session;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlacementAlgo implements Algo {
    protected final List<Session> sessions;

    public AbstractPlacementAlgo(List<Session> sessions) {
        this.sessions = sessions;
    }

    @Override
    public final ScheduledResult generateTimeTable() {
        sortSessionsByDifficulty();

        List<ScheduledSession> scheduledSessions = new ArrayList<>();
        List<Session> unscheduledSessions = new ArrayList<>();

        for (Session session : sessions) {
            boolean isPlaced = attemptPlacing(session, scheduledSessions);
            if (!isPlaced) {
                unscheduledSessions.add(session);
            }
        }

        return ScheduledResult.builder()
                .sessions(scheduledSessions)
                .unscheduledSessions(unscheduledSessions)
                .build();
    }

    protected abstract void sortSessionsByDifficulty();
    protected abstract boolean attemptPlacing(Session session, List<ScheduledSession> scheduledSessions);
}
