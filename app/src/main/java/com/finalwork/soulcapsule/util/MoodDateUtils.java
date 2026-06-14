package com.finalwork.soulcapsule.util;

import com.finalwork.soulcapsule.dto.MoodResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 心情记录日期解析与分组工具。
 */
public final class MoodDateUtils {

    private static final DateTimeFormatter DAY_KEY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA);
    private static final DateTimeFormatter MONTH_KEY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM", Locale.CHINA);

    private MoodDateUtils() {
    }

    public static LocalDateTime parseCreateTime(String createTime) {
        if (createTime == null || createTime.isEmpty()) {
            return LocalDateTime.now();
        }
        String normalized = createTime.contains(".")
                ? createTime.substring(0, createTime.indexOf('.'))
                : createTime;
        if (normalized.length() > 19) {
            normalized = normalized.substring(0, 19);
        }
        return LocalDateTime.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    public static LocalDate toLocalDate(MoodResponse mood) {
        return parseCreateTime(mood.getCreateTime()).toLocalDate();
    }

    public static String dayKey(LocalDate date) {
        return date.format(DAY_KEY_FORMAT);
    }

    public static void applyPeriodOffset(Calendar cal, int period, int timeOffset) {
        if (period == MoodStatsHelper.PERIOD_WEEK) {
            cal.add(Calendar.WEEK_OF_YEAR, timeOffset);
        } else if (period == MoodStatsHelper.PERIOD_MONTH) {
            cal.add(Calendar.MONTH, timeOffset);
        } else {
            cal.add(Calendar.YEAR, timeOffset);
        }
    }

    public static LocalDate getWeekStartLocalDate(Calendar weekAnchor) {
        Calendar cal = (Calendar) weekAnchor.clone();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return LocalDate.of(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
        );
    }

    public static Map<String, List<MoodResponse>> groupByDay(List<MoodResponse> moods) {
        Map<String, List<MoodResponse>> map = new HashMap<>();
        if (moods == null) {
            return map;
        }
        for (MoodResponse mood : moods) {
            String key = toLocalDate(mood).format(DAY_KEY_FORMAT);
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(mood);
        }
        return map;
    }

    public static Map<String, List<MoodResponse>> groupByMonth(List<MoodResponse> moods) {
        Map<String, List<MoodResponse>> map = new HashMap<>();
        if (moods == null) {
            return map;
        }
        for (MoodResponse mood : moods) {
            LocalDate date = toLocalDate(mood);
            String key = date.format(MONTH_KEY_FORMAT);
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(mood);
        }
        return map;
    }

    public static int getDaysInMonth(Calendar monthAnchor) {
        return monthAnchor.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static int getMondayBasedColumnIndex(Calendar dayCal) {
        int dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK);
        return (dayOfWeek + 5) % 7;
    }
}
