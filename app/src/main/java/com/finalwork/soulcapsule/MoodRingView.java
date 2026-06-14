package com.finalwork.soulcapsule;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 多段同心圆环，用于心情日历中一天多种心情的展示。
 * 调用 {@link #setSegments(int[], float[])} 设置各段颜色与占比（占比之和应为 1.0）。
 */
public class MoodRingView extends View {

    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    private final List<Integer> segmentColors = new ArrayList<>();
    private final List<Float> segmentRatios = new ArrayList<>();

    private float strokeWidth = 4f;
    private int defaultColor = 0xFFE8E4D8;

    public MoodRingView(Context context) {
        super(context);
        init();
    }

    public MoodRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MoodRingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setColor(defaultColor);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeCap(Paint.Cap.BUTT);
    }

    public void setStrokeWidthDp(float dp) {
        strokeWidth = dp * getResources().getDisplayMetrics().density;
        invalidate();
    }

    /**
     * @param colors 每段颜色
     * @param ratios 每段占比，总和为 1.0；为空或全 0 时仅绘制默认灰环
     */
    public void setSegments(int[] colors, float[] ratios) {
        segmentColors.clear();
        segmentRatios.clear();
        if (colors != null && ratios != null && colors.length == ratios.length) {
            for (int i = 0; i < colors.length; i++) {
                if (ratios[i] > 0) {
                    segmentColors.add(colors[i]);
                    segmentRatios.add(ratios[i]);
                }
            }
        }
        invalidate();
    }

    public void setShowRing(boolean show) {
        setVisibility(show ? VISIBLE : INVISIBLE);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - strokeWidth / 2f;

        bgPaint.setStrokeWidth(strokeWidth);
        ringPaint.setStrokeWidth(strokeWidth);

        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius);

        if (segmentColors.isEmpty()) {
            canvas.drawArc(arcRect, 0, 360, false, bgPaint);
            return;
        }

        float startAngle = -90f;
        for (int i = 0; i < segmentColors.size(); i++) {
            float sweep = segmentRatios.get(i) * 360f;
            ringPaint.setColor(segmentColors.get(i));
            canvas.drawArc(arcRect, startAngle, sweep, false, ringPaint);
            startAngle += sweep;
        }
    }
}
