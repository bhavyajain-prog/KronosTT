package com.kronostt.engine.algorithm;

import com.kronostt.engine.model.ScheduledResult;

@FunctionalInterface
public interface Algo {
    ScheduledResult generateTimeTable();
}
