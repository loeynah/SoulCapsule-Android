package com.finalwork.soulcapsule.util;

import android.content.Context;

import com.finalwork.soulcapsule.R;
import com.finalwork.soulcapsule.dto.MoodResponse;

/**
 * 心情分数与颜色的全局映射规范。
 * 很好(5)=红 | 好(4)=橙 | 一般(3)=绿 | 不好(2)=蓝 | 很不好(1)=紫
 */
public final class MoodColorConstants {

    public static final int SCORE_VERY_GOOD = 5;
    public static final int SCORE_GOOD = 4;
    public static final int SCORE_NORMAL = 3;
    public static final int SCORE_BAD = 2;
    public static final int SCORE_VERY_BAD = 1;

    /** 很好 Score 5 = #FF5252 */
    public static final int COLOR_SCORE_VERY_GOOD = 0xFFFF5252;
    /** 好 Score 4 = #FFAB40 */
    public static final int COLOR_SCORE_GOOD = 0xFFFFAB40;
    /** 一般 Score 3 = #66BB6A */
    public static final int COLOR_SCORE_NORMAL = 0xFF66BB6A;
    /** 不好 Score 2 = #42A5F5 */
    public static final int COLOR_SCORE_BAD = 0xFF42A5F5;
    /** 很不好 Score 1 = #AB47BC */
    public static final int COLOR_SCORE_VERY_BAD = 0xFFAB47BC;

    public static final String EMOTION_VERY_GOOD = "很好";
    public static final String EMOTION_GOOD = "好";
    public static final String EMOTION_NORMAL = "一般";
    public static final String EMOTION_BAD = "不好";
    public static final String EMOTION_VERY_BAD = "很不好";

    private static final String[] EMOTION_LABELS = {
            EMOTION_VERY_GOOD, EMOTION_GOOD, EMOTION_NORMAL, EMOTION_BAD, EMOTION_VERY_BAD
    };

    private static final int[] COLOR_INTS = {
            COLOR_SCORE_VERY_GOOD,
            COLOR_SCORE_GOOD,
            COLOR_SCORE_NORMAL,
            COLOR_SCORE_BAD,
            COLOR_SCORE_VERY_BAD
    };

    private static final int[] COLOR_RES_IDS = {
            R.color.mood_very_good,
            R.color.mood_good,
            R.color.mood_normal,
            R.color.mood_bad,
            R.color.mood_very_bad
    };

    private MoodColorConstants() {
    }

    public static String[] getEmotionLabels() {
        return EMOTION_LABELS.clone();
    }

    public static int[] getColorInts() {
        return COLOR_INTS.clone();
    }

    public static int[] getColorResIds() {
        return COLOR_RES_IDS.clone();
    }

    public static int getColorIntForScore(int score) {
        switch (score) {
            case SCORE_VERY_GOOD:
                return COLOR_SCORE_VERY_GOOD;
            case SCORE_GOOD:
                return COLOR_SCORE_GOOD;
            case SCORE_NORMAL:
                return COLOR_SCORE_NORMAL;
            case SCORE_BAD:
                return COLOR_SCORE_BAD;
            case SCORE_VERY_BAD:
                return COLOR_SCORE_VERY_BAD;
            default:
                return COLOR_SCORE_NORMAL;
        }
    }

    public static int getColorResForScore(int score) {
        switch (score) {
            case SCORE_VERY_GOOD:
                return R.color.mood_very_good;
            case SCORE_GOOD:
                return R.color.mood_good;
            case SCORE_NORMAL:
                return R.color.mood_normal;
            case SCORE_BAD:
                return R.color.mood_bad;
            case SCORE_VERY_BAD:
                return R.color.mood_very_bad;
            default:
                return R.color.mood_normal;
        }
    }

    public static int getColorForScore(Context context, int score) {
        return context.getColor(getColorResForScore(score));
    }

    /**
     * 优先使用 score 字段，否则根据 emotions 文本推断分数。
     */
    public static int resolveScore(MoodResponse mood) {
        if (mood == null) {
            return SCORE_NORMAL;
        }
        Integer score = mood.getScore();
        if (score != null && score >= SCORE_VERY_BAD && score <= SCORE_VERY_GOOD) {
            return score;
        }
        String emotion = mood.getEmotions();
        if (emotion == null || emotion.isEmpty()) {
            return SCORE_NORMAL;
        }
        if (emotion.contains(EMOTION_VERY_GOOD)) {
            return SCORE_VERY_GOOD;
        }
        if (emotion.contains(EMOTION_VERY_BAD)) {
            return SCORE_VERY_BAD;
        }
        if (emotion.contains(EMOTION_GOOD)) {
            return SCORE_GOOD;
        }
        if (emotion.contains(EMOTION_BAD)) {
            return SCORE_BAD;
        }
        if (emotion.contains(EMOTION_NORMAL)) {
            return SCORE_NORMAL;
        }
        return SCORE_NORMAL;
    }

    /** 分布条索引 0~4 对应 很好~很不好，返回对应 score 5~1 */
    public static int scoreForDistributionIndex(int index) {
        return SCORE_VERY_GOOD - index;
    }
}
