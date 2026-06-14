package com.finalwork.soulcapsule;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.finalwork.soulcapsule.util.MoodColorConstants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 多色比例心情圆环：接收 Score → 占比 Map，用 drawArc 从 -90° 起首尾相接绘制。
 */
public class MoodRatioRingView extends View {

    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    private final Map<Integer, Float> scoreRatios = new LinkedHashMap<>();

    private float strokeWidth = 4f;

    public MoodRatioRingView(Context context) {
        super(context);
        init();
    }

    public MoodRatioRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MoodRatioRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeCap(Paint.Cap.BUTT);

        emptyPaint.setStyle(Paint.Style.STROKE);
        emptyPaint.setColor(0xFFBDBDBD);
        emptyPaint.setPathEffect(new DashPathEffect(new float[]{8f, 8f}, 0f));
    }

    public void setStrokeWidthDp(float dp) {
        strokeWidth = dp * getResources().getDisplayMetrics().density;
        invalidate();
    }

    /**
     * @param ratios Score(1~5) → 占比，总和应为 1.0；null 或空表示无记录
     */
    public void setScoreRatios(@Nullable Map<Integer, Float> ratios) {
        scoreRatios.clear();
        if (ratios != null) {
            for (Map.Entry<Integer, Float> entry : ratios.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0f) {
                    scoreRatios.put(entry.getKey(), entry.getValue());
                }
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - strokeWidth / 2f;

        ringPaint.setStrokeWidth(strokeWidth);
        emptyPaint.setStrokeWidth(strokeWidth);
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius);

        if (scoreRatios.isEmpty()) {
            canvas.drawArc(arcRect, 0, 360, false, emptyPaint);
            return;
        }

        float startAngle = -90f;
        for (int score = MoodColorConstants.SCORE_VERY_GOOD;
             score >= MoodColorConstants.SCORE_VERY_BAD;
             score--) {
            Float ratio = scoreRatios.get(score);
            if (ratio == null || ratio <= 0f) {
                continue;
            }
            float sweepAngle = ratio * 360f;
            ringPaint.setColor(MoodColorConstants.getColorIntForScore(score));
            canvas.drawArc(arcRect, startAngle, sweepAngle, false, ringPaint);
            startAngle += sweepAngle;
        }
    }
}
