package com.lw.custom.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.lw.custom.R;

/**
 * starView
 */
public class StarView extends View {
    /**
     * 亮星填充颜色,默认黄色
     */
    @ColorInt
    private int starColor = Color.YELLOW;
    /**
     * 暗星绘制颜色,默认灰色
     */
    @ColorInt
    private int bgColor = Color.GRAY;
    /**
     * 设置暗星填充类型,默认填充
     */
    private Style starStyle = Style.STROKE;
    /**
     * 绘制的星星数量,默认为5
     */
    private int starCount = 5;
    /**
     * 亮星数量,小数部分>0.5代表半亮,<0.5部分忽略
     */
    private double rating = 0;
    /**
     * 星星高度大小，宽度等于高度
     */
    private float starSize = 150;
    /**
     * 画笔粗细,默认为1
     */
    private float paintSize = 1;
    /**
     * 各个星星之间的间距,默认为10
     */
    private float starMarge = 10;
    /**
     * 是否可以滑动改变亮星数量,默认关闭
     */
    private boolean isChange = false;
    /**
     * 半星开关,默认关闭
     */
    private boolean half = false;

    private Paint mPaint;

    public StarView(Context context) {
        super(context);
        mPaint = new Paint();
    }

    public StarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        init(context, attrs);
    }

    /**
     * 初始化
     */
    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.StarView);
        starColor = array.getColor(R.styleable.StarView_starColor, Color.YELLOW);
        bgColor = array.getColor(R.styleable.StarView_bgColor, Color.GRAY);
        int style = array.getInt(R.styleable.StarView_starStyle, 1);
        switch (style) {
            case 1:
                starStyle = Style.FILL;
                break;
            case 2:
                starStyle = Style.STROKE;
                break;
            case 3:
                starStyle = Style.FILL_AND_STROKE;
                break;
        }
        setStarCount(array.getInt(R.styleable.StarView_starCount, 5));
        setPaintSize(array.getDimension(R.styleable.StarView_paintSize, 1));
        setStarMarge(array.getDimension(R.styleable.StarView_starMarge, 10));
        setStarSize(array.getDimension(R.styleable.StarView_starSize, starSize));
        isChange = array.getBoolean(R.styleable.StarView_isChange, false);
        half = array.getBoolean(R.styleable.StarView_half, false);

        setRating(array.getFloat(R.styleable.StarView_rating, 0));
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //这个是获取宽跟高，给下面计算星星大小用
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY
                || MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {//宽高在布局中确定了的情况
            calculationSize(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        }
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {//重新测量宽度
            widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) ((starSize / cos(18) + starMarge + paintSize * 2) * starCount + getPaddingLeft() + getPaddingRight()), MeasureSpec.EXACTLY);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {//重新测量高度
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (starSize + paintSize * 2 + +getPaddingTop() + getPaddingBottom()), MeasureSpec.EXACTLY);
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 计算星星大小
     */
    private void calculationSize(int width, int height) {
        int showWidth = width - getPaddingLeft() - getPaddingRight();
        int showHeight = height - getPaddingTop() - getPaddingBottom();
        //计算星星大小
        if (showWidth > showHeight / cos(18) * starCount) {
            starSize = showHeight;
        } else {
            starSize = (showWidth / starCount - starMarge - paintSize * 2) * cos(18);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Path path;
        float dx = starSize / cos(18) + starMarge + paintSize * 2;
        float dy = 0;

        //长度
        float outR = starSize / (1 + cos(36));//中心距离外点距离
        float inR = outR * sin(18) / cos(36);//中心距离内点距离

        mPaint.setAntiAlias(true);//开启抗锯齿
        mPaint.setDither(true);//开启防抖动
        mPaint.setColor(starColor);//画笔颜色
        mPaint.setStrokeWidth(paintSize);//画笔大小(px)

        canvas.translate(starSize / (2 * cos(18)) + paintSize + starMarge / 2 + getPaddingLeft(), outR + paintSize + getPaddingTop());//移动坐标

        int pos = -1;
        if (half) {
            if (((int) (rating * 2)) % 2 != 0) {//检测是否需要绘制半星
                pos = (int) rating + 1;
            }
        }

        for (int i = 1; i <= starCount; ) {
            if (i <= rating) {//亮星绘制
                // 填充和描边五角星
                path = getCompletePath(outR, inR);
                mPaint.setColor(starColor);//画笔颜色
                mPaint.setStyle(Style.FILL_AND_STROKE);//填充和描边
                canvas.drawPath(path, mPaint);
            } else if (i == pos) {//半星绘制
                path = getLeftHalfPath(outR, inR);
                mPaint.setColor(starColor);//画笔颜色
                mPaint.setStyle(Style.FILL_AND_STROKE);
                canvas.drawPath(path, mPaint);

                path = getRightHalfPath(outR, inR);
                mPaint.setColor(bgColor);
                mPaint.setStyle(starStyle);
                canvas.drawPath(path, mPaint);
            } else {//暗星绘制
                path = getCompletePath(outR, inR);
                mPaint.setColor(bgColor);
                mPaint.setStyle(starStyle);
                canvas.drawPath(path, mPaint);
            }
            if (i++ < starCount) {//判断是否需要挪画布
                canvas.translate(dx, dy);
            }
        }
        canvas.save();
        canvas.restore();
    }

    /**
     * 滑动和点击选择星星
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isChange) {//是否可以点击或者滑动
            int x = (int) event.getX();
            if (x < 0) {
                x = 0;
            } else if (x > getMeasuredWidth()) {
                x = getMeasuredWidth();
            }
            if (half) {//绘制半星
                //半星宽度
                float halfStarWidth = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / (starCount * 2.0f);
                //半星数量
                int halfRatNum = (int) (x / halfStarWidth) + 1;
                if (Math.abs(halfRatNum - (int) (rating * 2)) >= 1) {
                    rating = halfRatNum / 2.0;
                    invalidate();//重新绘制
                }
            } else {//不绘制半星
                float starWidth = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / (starCount * 1.0f);
                //星星数量
                float ratNum = (int) (x / starWidth) + 1;
                if (Math.abs(ratNum - (int) rating) >= 1) {
                    rating = ratNum;
                    invalidate();//重新绘制
                }
            }
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    private Path getLeftHalfPath(float outR, float inR) {
        Path path = new Path();
        path.moveTo(0, inR);
        for (int i = 5; i > 0; ) {//顶部坐标开始逆时针转
            path.lineTo(-outR * sin(36 * i), outR * cos(36 * i));
            i--;
            path.lineTo(-inR * sin(36 * i), inR * cos(36 * i));
            i--;
        }
        path.close();
        return path;
    }

    private Path getRightHalfPath(float outR, float inR) {
        Path path = new Path();

        path.moveTo(0, inR);
        for (int i = 5; i < 10; ) {//顶部坐标开始逆时针转
            path.lineTo(-outR * sin(36 * i), outR * cos(36 * i));
            i++;
            path.lineTo(-inR * sin(36 * i), inR * cos(36 * i));
            i++;
        }
        path.close();
        return path;
    }

    private Path getCompletePath(float outR, float inR) {
        Path path = new Path();

        path.moveTo(0, inR);
        for (int i = 0; i < 10; ) {//顶部坐标开始逆时针转
            path.lineTo(-inR * sin(36 * i), inR * cos(36 * i));
            i++;
            path.lineTo(-outR * sin(36 * i), outR * cos(36 * i));
            i++;
        }
        path.close();
        return path;
    }

    private float cos(int num) {
        return (float) Math.cos(num * Math.PI / 180);
    }

    private float sin(int num) {
        return (float) Math.sin(num * Math.PI / 180);
    }

    /**
     * 设置亮星颜色
     *
     * @param starColor 颜色
     */
    public void setColor(@ColorInt int starColor) {
        this.starColor = starColor;
        invalidate();
    }

    /**
     * 设置亮星颜色
     */
    public void setColorResources(@ColorRes int colorRes) {
        this.starColor = getResources().getColor(colorRes);
        invalidate();
    }

    /**
     * 设置背景颜色
     *
     * @param bgColor 颜色
     */
    public void setBgColor(@ColorInt int bgColor) {
        this.bgColor = bgColor;
        invalidate();
    }

    /**
     * 设置背景颜色
     *
     * @param colorRes 颜色
     */
    public void setBgColorResources(@ColorRes int colorRes) {
        this.bgColor = getResources().getColor(colorRes);
        invalidate();
    }

    /**
     * 设置暗星填充类型
     *
     * @param starStyle #Paint.Style.FILL 填充 STROKE描边 FILL_AND_STROKE填充和描边
     */
    public void setStarStyle(Style starStyle) {
        this.starStyle = starStyle;
        invalidate();
    }

    /**
     * 设置亮星数量
     *
     * @param rating 亮星数量
     */
    public void setRating(double rating) {
        if (rating > starCount) {
            rating = starCount;
        } else if (rating < 0) {
            rating = 0;
        }
        if (this.rating != rating) {
            this.rating = rating;
            invalidate();
        }
    }

    /**
     * 设置画笔粗细
     *
     * @param paintSize 描边粗细
     */
    public void setPaintSize(float paintSize) {
        if (paintSize < 1) {
            paintSize = 1;
        }
        if (this.paintSize != paintSize) {
            this.paintSize = paintSize;
            invalidate();
        }
    }

    /**
     * 设置总数量
     */
    public void setStarCount(int starCount) {
        if (starCount < 1) {
            starCount = 1;
        }
        if (this.starCount != starCount) {
            this.starCount = starCount;
            invalidate();
        }
    }

    /**
     * 设置星星的大小
     */
    public void setStarSize(float starSize) {
        if (starSize < 0) {
            starSize = 0;
        }
        if (this.starSize != starSize) {
            this.starSize = starSize;
            invalidate();
        }
    }

    /**
     * 设置星星间距
     */
    public void setStarMarge(float starMarge) {
        if (starMarge < 0) {
            starMarge = 0;
        }
        if (this.starMarge != starMarge) {
            this.starMarge = starMarge;
            invalidate();
        }
    }

    /**
     * 滑动调整开关
     *
     * @param change 是否可滑动调整亮星数量
     */
    public void setChange(boolean change) {
        this.isChange = change;
    }

    /**
     * 亮半颗星功能
     *
     * @param half 是否支持亮半颗星
     */
    public void setHalf(boolean half) {
        this.half = half;
    }

    /**
     * 获取滑动调整开关状态
     */
    public boolean isChange() {
        return isChange;
    }

    /**
     * 获取亮半颗星功能状态
     */
    public boolean isHalf() {
        return half;
    }

    /**
     * 获取亮星数量,不包含半星
     */
    public int getIntRating() {
        if (rating > starCount) {
            return starCount;
        }
        return (int) rating;
    }

    /**
     * 获取亮星数量
     */
    public double getRating() {
        if (rating > starCount) {
            return starCount;
        }
        return ((int) rating * 2) / 2.0;
    }

    public int getStarColor() {
        return starColor;
    }

    public int getBgColor() {
        return bgColor;
    }

    public Style getStarStyle() {
        return starStyle;
    }

    public int getStarCount() {
        return starCount;
    }

    public float getStarSize() {
        return starSize;
    }

    public float getPaintSize() {
        return paintSize;
    }

    public float getStarMarge() {
        return starMarge;
    }
}