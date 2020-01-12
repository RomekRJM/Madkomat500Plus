package com.example.madkomatapp.image;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;


public class ImagePreview extends AppCompatImageView {
    private final String PROPERTY_RADIUS = "radius";
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

    private int radius;
    private int angle;
    private int angle2;
    private float rectangleHeightScale;
    private float rectangleWidthScale;
    private float rectangleOrbitRadius;

    private Bitmap bitmap;

    public ImagePreview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        circlePaint.setColor(0xffaa44ff);
        rectanglePaint.setColor(0x998888d8);

        sizeRect = 40;
        circleRadius = 140;
        numberOfCircles = 8;
        spread = 360.0 / numberOfCircles;

    }

    protected void onDraw(Canvas canvas) {
        viewWidth = getWidth() / 2;
        viewHeight = getHeight() / 2;

        drawBitmap(canvas);
        drawLoadingIcon(canvas);
        drawMovingRectangle(canvas);
    }

    private void drawBitmap(Canvas canvas) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0f, 0f, null);
        }
    }

    private void drawLoadingIcon(Canvas canvas) {
        circleLeftTopX = viewWidth - sizeRect;
        circleLeftTopY = viewHeight - sizeRect;
        circleRightBotX = viewWidth + sizeRect;
        circleRightBotY = viewHeight + sizeRect;

        for (int i = 0; i < numberOfCircles; ++i) {
            double angleRad = Math.toRadians(i * spread + angle);
            int xShift = (int) Math.round(circleRadius * Math.cos(angleRad));
            int yShift = (int) Math.round(circleRadius * Math.sin(angleRad));

            canvas.drawRoundRect(circleLeftTopX + xShift, circleLeftTopY + yShift,
                    circleRightBotX + xShift, circleRightBotY + yShift,
                    radius, radius, circlePaint);
        }
    }

    private void drawMovingRectangle(Canvas canvas) {
        int rectSize = Math.min(getWidth(), getHeight());
        int halfRectangleWidth = Math.round(rectSize / (rectangleWidthScale * 2));
        int halfRectangleHeight = Math.round(rectSize / (rectangleHeightScale * 2));

        double angleRad = Math.toRadians(angle2);
        int xShift = (int) Math.round(rectangleOrbitRadius * viewWidth / 6 * Math.cos(angleRad));
        int yShift = (int) Math.round(rectangleOrbitRadius * viewHeight / 7 * Math.sin(angleRad));

        canvas.drawRect(xShift + viewWidth - halfRectangleWidth, yShift + viewHeight - halfRectangleHeight,
                xShift + viewWidth + halfRectangleWidth, yShift + viewHeight + halfRectangleHeight,
                rectanglePaint);
    }

    public void setBackgroundImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void startAnimator() {
        PropertyValuesHolder propertyRadius = PropertyValuesHolder.ofInt(PROPERTY_RADIUS, 0, 150);
        PropertyValuesHolder propertyAngle = PropertyValuesHolder.ofInt(PROPERTY_ANGLE, 0, 720);
        PropertyValuesHolder propertyAngle2 = PropertyValuesHolder.ofInt(PROPERTY_ANGLE2, 180, 1080);
        PropertyValuesHolder propertyRectanglePosition = PropertyValuesHolder.ofFloat(PROPERTY_RECTANGLE_POSITION, 5f, 0f);
        PropertyValuesHolder propertyRectangleHeight = PropertyValuesHolder.ofFloat(PROPERTY_RECTANGLE_HEIGHT_SCALE, 2.3f, 3.5f);
        PropertyValuesHolder propertyRectangleWidth = PropertyValuesHolder.ofFloat(PROPERTY_RECTANGLE_WIDTH_SCALE, 3.5f, 2.3f);

        ValueAnimator animator = new ValueAnimator();
        animator.setValues(propertyRadius, propertyAngle, propertyAngle2,
                propertyRectanglePosition, propertyRectangleHeight, propertyRectangleWidth);
        animator.setDuration(8000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                radius = (int) animation.getAnimatedValue(PROPERTY_RADIUS);
                angle = (int) animation.getAnimatedValue(PROPERTY_ANGLE);
                angle2 = (int) animation.getAnimatedValue(PROPERTY_ANGLE2);
                rectangleOrbitRadius = (float) animation.getAnimatedValue(PROPERTY_RECTANGLE_POSITION);
                rectangleHeightScale = (float) animation.getAnimatedValue(PROPERTY_RECTANGLE_HEIGHT_SCALE);
                rectangleWidthScale = (float) animation.getAnimatedValue(PROPERTY_RECTANGLE_WIDTH_SCALE);

                invalidate();
            }
        });
        animator.start();
    }
}
