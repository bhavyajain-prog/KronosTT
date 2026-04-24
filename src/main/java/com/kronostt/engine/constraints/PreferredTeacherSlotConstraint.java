package com.kronostt.engine.constraints;

import com.kronostt.engine.TimeTableState;
import com.kronostt.engine.model.Room;
import com.kronostt.engine.model.Session;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PreferredTeacherSlotConstraint implements Constraint {
    private final long classTeacherId;
    private final long targetBatchId;
    private final int rewardScore;
    private final int preferredSlot;

    @Override
    public int evaluate(Session session, Room room, int day, int start, TimeTableState state) {
        if (session.getTeacher().getId() == classTeacherId &&
                session.getBatch().getId() == targetBatchId) {
            if (start == preferredSlot) return rewardScore;
            else return -(rewardScore / 2);
        }
        return 0;
    }
}
