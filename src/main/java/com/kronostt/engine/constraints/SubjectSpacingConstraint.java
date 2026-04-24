package com.kronostt.engine.constraints;

import com.kronostt.engine.TimeTableState;
import com.kronostt.engine.model.Room;
import com.kronostt.engine.model.Session;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SubjectSpacingConstraint implements Constraint {

    private final int penaltyScore;

    @Override
    public int evaluate(Session session, Room room, int day, int start, TimeTableState state) {
        long batchId = session.getBatch().getId();
        long subjectId = session.getSubject().getId();

        for (int i = 0; i < state.getMaxSlots(); i++) {
            Session batchSession = state.getBatchSession(batchId, day, i);
            if (batchSession != null && batchSession.getSubject().getId() == subjectId) {
                return -penaltyScore;
            }
        }
        return 0;
    }
}
