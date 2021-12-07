package com.denchic45.kts.utils;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

public final class TimestampUtil {

    public static boolean isDateDiffsGreaterThanOrEqualTo(long d1, long d2, int diffDays, @NonNull TimeUnit timeUnit) {
        long diff = Math.abs(d1 - d2);
        return timeUnit.convert(diff, TimeUnit.MILLISECONDS) >= diffDays;
    }
}
