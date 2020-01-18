package com.example.madkomatapp.image;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.AppCompatImageView;


public class ImagePreview extends AppCompatImageView {
    private final String PROPERTY_ANGLE = "angle";
    private final String PROPERTY_ANGLE2 = "angle2";
    private final String PROPERTY_RECTANGLE_POSITION = "rectangle_position";
    private final String PROPERTY_RECTANGLE_HEIGHT_SCALE = "rectangle_height_scale";
    private final String PROPERTY_RECTANGLE_WIDTH_SCALE = "rectangle_width_scale";

    private final Paint circlePaint = new Paint();
    private final Paint rectanglePaint = new Paint();

    private final int sizeRect;
    private int circleRadius;
    private final int numberOfCircles;
    private final double spread;

    private int viewHeight;
    private int viewWidth;
    private int circleLeftTopX;
    private int circleLeftTopY;
    private int circleRightBotX;
    private int circleRightBotY;

    private int angle;
    private int angle2;
    private float rectangleHeightScale;
    private float rectangleWidthScale;
    private float rectangleOrbitRadius;

    private Bitmap bitmap;
    private PathTracer pathTracer;

    public ImagePreview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        circlePaint.setColor(0xffaa44ff);
        rectanglePaint.setColor(0x576888ff);

        sizeRect = 20;
        circleRadius = 40;
        numberOfCircles = 8;
        spread = 360.0 / numberOfCircles;

        pathTracer = new PathTracer(new PointF[]{
                new PointF(0.2f, 0.2f), new PointF(0.4f, 0.8f), new PointF(0.37f, 0.2f),
                new PointF(0.2f, 0.5f), new PointF(0.5f, 0.16f), new PointF(0.8f, 0.8f)
        });
    }

    protected void onDraw(Canvas canvas) {
        viewWidth = getWidth() / 2;
        viewHeight = getHeight() / 2;

        drawBitmap(canvas);
        drawMovingRectangle(canvas);
        drawLoadingIcon(canvas);
    }

    private void drawBitmap(Canvas canvas) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0f, 0f, null);
        }
    }

    private void drawLoadingIcon(Canvas canvas) {
        circleLeftTopX = circleRadius;
        circleLeftTopY = circleRadius;
        circleRightBotX = circleRadius + sizeRect;
        circleRightBotY = circleRadius + sizeRect;

        for (int i = 0; i < numberOfCircles; ++i) {
            double angleRad = Math.toRadians(i * spread + angle);
            int xShift = (int) Math.round(circleRadius * Math.cos(angleRad));
            int yShift = (int) Math.round(circleRadius * Math.sin(angleRad));

            canvas.drawRoundRect(circleLeftTopX + xShift, circleLeftTopY + yShift,
                    circleRightBotX + xShift, circleRightBotY + yShift,
                    90, 90, circlePaint);
        }
    }

    private void drawMovingRectangle(Canvas canvas) {
        int rectSize = Math.min(getWidth(), getHeight());
        int halfRectangleWidth = Math.round(rectSize / (rectangleWidthScale * 2));
        int halfRectangleHeight = Math.round(rectSize / (rectangleHeightScale * 2));

        //double angleRad = Math.toRadians(angle2);
        //int xShift = (int) Math.round(rectangleOrbitRadius * viewWidth / 6 * Math.cos(angleRad));
        //int yShift = (int) Math.round(rectangleOrbitRadius * viewHeight / 7 * Math.sin(angleRad));
        PointF coordinate = pathTracer.getCoordinateAlongThePath(rectangleOrbitRadius);
        int xShift = Math.round(getWidth() * coordinate.x);
        int yShift = Math.round(getHeight()  * coordinate.y);

        int left = xShift - halfRectangleWidth;
        int top = yShift - halfRectangleHeight;
        int right = xShift + halfRectangleWidth;
        int bottom = yShift + halfRectangleHeight;

        canvas.drawRect(0, 0, left, getHeight(), rectanglePaint);
        canvas.drawRect(0, 0, getWidth(), top, rectanglePaint);
        canvas.drawRect(right, 0, getWidth(), getHeight(), rectanglePaint);
        canvas.drawRect(0, bottom, getWidth(), getHeight(), rectanglePaint);
    }

    public void setBackgroundImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void startAnimators() {
        PropertyValuesHolder propertyAngle2 = PropertyValuesHolder.ofInt(PROPERTY_ANGLE2, 180, 1080);
        PropertyValuesHolder propertyRectanglePosition = PropertyValuesHolder.ofFloat(PROPERTY_RECTANGLE_POSITION, 0f, 100f);
        PropertyValuesHolder propertyRectangleHeight = PropertyValuesHolder.ofFloat(PROPERTY_RECTANGLE_HEIGHT_SCALE, 2.3f, 3.5f);
        PropertyValuesHolder propertyRectangleWidth = PropertyValuesHolder.ofFloat(PROPERTY_RECTANGLE_WIDTH_SCALE, 3.5f, 2.3f);

        ValueAnimator rectangleAnimator = new ValueAnimator();
        rectangleAnimator.setValues(propertyAngle2, propertyRectanglePosition,
                propertyRectangleHeight, propertyRectangleWidth);
        rectangleAnimator.setDuration(8000);
        rectangleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rectangleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        rectangleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                angle2 = (int) animation.getAnimatedValue(PROPERTY_ANGLE2);
                rectangleOrbitRadius = (float) animation.getAnimatedValue(PROPERTY_RECTANGLE_POSITION);
                rectangleHeightScale = (float) animation.getAnimatedValue(PROPERTY_RECTANGLE_HEIGHT_SCALE);
                rectangleWidthScale = (float) animation.getAnimatedValue(PROPERTY_RECTANGLE_WIDTH_SCALE);
                invalidate();
            }
        });
        rectangleAnimator.start();

        PropertyValuesHolder propertyAngle = PropertyValuesHolder.ofInt(PROPERTY_ANGLE, 0, 2160);

        ValueAnimator circleAnimator = new ValueAnimator();
        circleAnimator.setInterpolator(new LinearInterpolator());
        circleAnimator.setValues(propertyAngle);
        circleAnimator.setDuration(15000);
        circleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        circleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                angle = (int) animation.getAnimatedValue(PROPERTY_ANGLE);
                invalidate();
            }
        });
        circleAnimator.start();
    }
}
