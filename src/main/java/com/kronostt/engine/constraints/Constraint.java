package com.kronostt.engine.constraints;

import com.kronostt.engine.TimeTableState;
import com.kronostt.engine.model.Room;
import com.kronostt.engine.model.Session;

public interface Constraint {
    int evaluate(Session session, Room room, int day, int start, TimeTableState state);
}
