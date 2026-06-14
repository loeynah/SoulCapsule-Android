package com.finalwork.soulcapsule;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 简易折线图 View，支持左侧心情色阶圆点、稀疏 X 轴标签与区域填充。
 */
public class MoodTrendView extends View {

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint scaleDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();

    private float[] dataPoints = new float[]{3f, 4f, 2.5f, 4.5f, 3.8f, 4.2f, 4.8f};
    private String[] labels = new String[]{"一", "二", "三", "四", "五", "六", "日"};
    private int[] sparseLabelDays;

    private int[] moodScaleColors;

    public MoodTrendView(Context context) {
        super(context);
        init();
    }

    public MoodTrendView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MoodTrendView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint.setColor(getContext().getColor(R.color.theme_orange));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3f * getResources().getDisplayMetrics().density);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        pointPaint.setColor(getContext().getColor(R.color.theme_orange));
        pointPaint.setStyle(Paint.Style.FILL);

        gridPaint.setColor(getContext().getColor(R.color.divider_gray));
        gridPaint.setStrokeWidth(1f);

        fillPaint.setStyle(Paint.Style.FILL);

        scaleDotPaint.setStyle(Paint.Style.FILL);

        moodScaleColors = new int[]{
                getContext().getColor(R.color.mood_very_good),
                getContext().getColor(R.color.mood_good),
                getContext().getColor(R.color.mood_normal),
                getContext().getColor(R.color.mood_bad),
                getContext().getColor(R.color.mood_very_bad)
        };
    }

    public void setMoodScaleColors(int[] colors) {
        if (colors != null && colors.length > 0) {
            moodScaleColors = colors;
            invalidate();
        }
    }

    public void setData(float[] points, String[] axisLabels) {
        sparseLabelDays = null;
        if (points != null && points.length > 0) {
            dataPoints = points;
        }
        if (axisLabels != null && axisLabels.length > 0) {
            labels = axisLabels;
        }
        invalidate();
    }

    public void setDataWithSparseDays(float[] points, int[] dayLabels) {
        if (points != null && points.length > 0) {
            dataPoints = points;
        }
        sparseLabelDays = dayLabels;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0 || dataPoints.length == 0) {
            return;
        }

        float density = getResources().getDisplayMetrics().density;
        float scaleColumnWidth = 28f * density;
        float paddingLeft = scaleColumnWidth + 12f * density;
        float paddingRight = 16f * density;
        float paddingTop = 16f * density;
        float paddingBottom = 28f * density;

        float chartW = w - paddingLeft - paddingRight;
        float chartH = h - paddingTop - paddingBottom;

        drawMoodScale(canvas, scaleColumnWidth, paddingTop, chartH, density);

        canvas.drawLine(paddingLeft, paddingTop + chartH, w - paddingRight, paddingTop + chartH, gridPaint);

        float min = 1f;
        float max = 5f;
        int count = dataPoints.length;
        float stepX = count > 1 ? chartW / (count - 1) : 0;

        linePath.reset();
        fillPath.reset();
        for (int i = 0; i < count; i++) {
            float x = paddingLeft + stepX * i;
            float normalized = (dataPoints[i] - min) / (max - min);
            float y = paddingTop + chartH * (1f - normalized);

            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, paddingTop + chartH);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }
        if (count > 0) {
            float lastX = paddingLeft + stepX * (count - 1);
            fillPath.lineTo(lastX, paddingTop + chartH);
            fillPath.close();
        }

        int fillTop = getContext().getColor(R.color.theme_orange);
        int fillBottom = getContext().getColor(R.color.bg_warm_white);
        int fillTopColor = (fillTop & 0x00FFFFFF) | 0x33000000;
        fillPaint.setShader(new LinearGradient(
                0, paddingTop, 0, paddingTop + chartH,
                fillTopColor,
                fillBottom,
                Shader.TileMode.CLAMP
        ));
        canvas.drawPath(fillPath, fillPaint);
        fillPaint.setShader(null);

        canvas.drawPath(linePath, linePaint);

        float pointRadius = 4f * density;
        for (int i = 0; i < count; i++) {
            float x = paddingLeft + stepX * i;
            float normalized = (dataPoints[i] - min) / (max - min);
            float y = paddingTop + chartH * (1f - normalized);
            canvas.drawCircle(x, y, pointRadius, pointPaint);
        }

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(getContext().getColor(R.color.text_secondary));
        textPaint.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                11f,
                getResources().getDisplayMetrics()));
        textPaint.setTextAlign(Paint.Align.CENTER);

        if (sparseLabelDays != null) {
            for (int day : sparseLabelDays) {
                int index = day - 1;
                if (index >= 0 && index < count) {
                    float x = paddingLeft + stepX * index;
                    canvas.drawText(String.valueOf(day), x, h - 8f * density, textPaint);
                }
            }
        } else {
            int labelCount = Math.min(labels.length, count);
            for (int i = 0; i < labelCount; i++) {
                float x = paddingLeft + stepX * i;
                canvas.drawText(labels[i], x, h - 8f * density, textPaint);
            }
        }
    }

    private void drawMoodScale(Canvas canvas, float columnWidth, float paddingTop, float chartH, float density) {
        if (moodScaleColors == null || moodScaleColors.length == 0) {
            return;
        }
        int dotCount = moodScaleColors.length;
        float dotRadius = 5f * density;
        float cx = columnWidth / 2f;
        float step = chartH / (dotCount - 1);

        for (int i = 0; i < dotCount; i++) {
            scaleDotPaint.setColor(moodScaleColors[i]);
            float cy = paddingTop + step * i;
            canvas.drawCircle(cx, cy, dotRadius, scaleDotPaint);
        }
    }
}
