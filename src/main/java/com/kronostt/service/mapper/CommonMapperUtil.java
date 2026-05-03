package com.kronostt.service.mapper;

import com.kronostt.engine.model.enums.WeekDay;
import org.springframework.stereotype.Component;

@Component
public class CommonMapperUtil {

    // MapStruct will automatically use this when mapping int -> WeekDay
    public WeekDay safeGetWeekDay(int dayOfWeek) {
        if (dayOfWeek < 0 || dayOfWeek > 5) {
            throw new IllegalArgumentException(
                    "Invalid dayOfWeek: " + dayOfWeek + ". Must be 0-5 (MONDAY-SATURDAY)"
            );
        }
        return WeekDay.values()[dayOfWeek];
    }

    // MapStruct will automatically use this when mapping WeekDay -> int
    public int weekDayToInt(WeekDay weekDay) {
        if (weekDay == null) {
            return 0; // Or whatever fallback you prefer
        }
        return weekDay.ordinal();
    }
}