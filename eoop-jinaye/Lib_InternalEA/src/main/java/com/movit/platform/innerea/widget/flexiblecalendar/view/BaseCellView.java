package com.movit.platform.innerea.widget.flexiblecalendar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.TextView;

import com.movit.platform.framework.utils.LogUtils;
import com.movit.platform.innerea.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author p-v
 */
public class BaseCellView extends TextView {

    public static final int STATE_TODAY = R.attr.state_date_today;
    public static final int STATE_REGULAR = R.attr.state_date_regular;
    public static final int STATE_SELECTED = R.attr.state_date_selected;
    public static final int STATE_OUTSIDE_MONTH = R.attr.state_date_outside_month;

    private Set<Integer> stateSet;
    // private Paint eventPaint;

    private int eventCircleY;
    private int radius;
    private int padding;
    private int leftMostPosition = Integer.MIN_VALUE;
    private List<Paint> paintList;
    private Paint circlePaint;

    // 圆心x坐标
    private int mXCenter;
    // 圆心y坐标
    private int mYCenter;
    // 圆环半径
    private float mRingRadius;
    // 圆环宽度
    private float mStrokeWidth;

    public BaseCellView(Context context) {
        super(context);
    }

    public BaseCellView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BaseCellView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.BaseCellView);
        try {
            radius = (int) a.getDimension(
                    R.styleable.BaseCellView_event_radius, 6);
            padding = (int) a.getDimension(
                    R.styleable.BaseCellView_event_circle_padding, 2);
        } finally {
            a.recycle();
        }
        stateSet = new HashSet<Integer>(3);
        // eventPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // eventPaint.setStyle(Paint.Style.FILL);

        mStrokeWidth = 5;
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(getContext().getResources().getColor(R.color.blue_gps_circle));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(4);
    }

    public void addState(int state) {
        stateSet.add(state);
    }

    public void removeState(int state) {
        stateSet.remove(state);
    }

    public void clearAllStates() {
        stateSet.clear();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        if (stateSet == null)
            stateSet = new HashSet<>(3);
        if (!stateSet.isEmpty()) {
            final int[] drawableState = super.onCreateDrawableState(extraSpace
                    + stateSet.size());
            int[] states = new int[stateSet.size()];
            int i = 0;
            for (Integer s : stateSet) {
                states[i++] = s;
            }
            mergeDrawableStates(drawableState, states);
            return drawableState;
        } else {
            return super.onCreateDrawableState(extraSpace);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // initialize paint objects only if there is no state or just one state
        // i.e. the regular day state
        // 同下
        if ((stateSet == null || stateSet.isEmpty() || (stateSet.size() == 1))
                && paintList != null) {
            int num = paintList.size();

            Paint p = new Paint();
            p.setTextSize(getTextSize());

            Rect rect = new Rect();
            p.getTextBounds("31", 0, 1, rect); // measuring using fake text
            eventCircleY = (3 * getHeight() + rect.height()) / 4 + 4;
            mXCenter = getWidth() / 2;
            mYCenter = getHeight() / 2;
            mRingRadius = rect.width() / 2 + dip2px(getContext(), 10);

            // calculate left most position for the circle
            if (leftMostPosition == Integer.MIN_VALUE) {
                leftMostPosition = (getWidth() / 2) - (num / 2) * 2
                        * (padding + radius);
                if (num % 2 == 0) {
                    leftMostPosition = leftMostPosition + radius + padding;
                }
            }

        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setBackgroundResource(R.drawable.date_cell_text_bg_selector);
        setTextColor(getResources().getColor(R.color.title_color));
        // always draw circle
        if ((stateSet == null || stateSet.isEmpty() || stateSet.size() == 1)
                && paintList != null) {
            int num = paintList.size();
            //修改字体颜色
//            if (num > 0) {
//                setTextColor(getResources().getColor(R.color.white));
//            }
            for (int i = 0; i < num; i++) {
                RectF oval = new RectF();
                oval.left = (mXCenter - mRingRadius);
                oval.top = (mYCenter - mRingRadius);
                oval.right = (mXCenter + mRingRadius);
                oval.bottom = (mYCenter + mRingRadius);
                canvas.drawArc(oval, 90 - (i * 180) + 45, 180, true,
                        paintList.get(i));
            }
            if (stateSet != null && stateSet.contains(BaseCellView.STATE_SELECTED)) {
                canvas.drawCircle(mXCenter, mYCenter, mRingRadius + 2, circlePaint);
            }
        }
        super.onDraw(canvas);
    }

    private int calculateStartPoint(int offset) {
        return leftMostPosition + offset * (2 * (radius + padding));
    }

    public void setEvents(List<Integer> colorList) {
        if (colorList != null) {
            paintList = new ArrayList<>(colorList.size());
            for (Integer e : colorList) {
                // Paint eventPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                // eventPaint.setStyle(Paint.Style.FILL);
                // eventPaint.setColor(getContext().getResources().getColor(e));

                Paint mRingPaint = new Paint();
                mRingPaint.setAntiAlias(true);
                mRingPaint.setColor(getContext().getResources().getColor(e));
                mRingPaint.setStyle(Paint.Style.FILL);
                mRingPaint.setStrokeWidth(mStrokeWidth);
                paintList.add(mRingPaint);
            }
            invalidate();
            requestLayout();
        }
    }

    public List<Paint> getEvents() {
        return paintList;
    }

    public int dip2px(Context context, float dpValue) {
        if (context == null) {
            return 0;
        }
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
