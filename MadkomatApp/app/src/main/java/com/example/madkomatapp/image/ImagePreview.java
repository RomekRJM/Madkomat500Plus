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
    private final Paint backgroundPaint = new Paint();

    private int radius;
    private int angle;
    private Bitmap bitmap;

    public ImagePreview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        backgroundPaint.setColor(0xffaa44ff);
    }

    protected void onDraw(Canvas canvas) {
        int viewWidth = getWidth() / 2;
        int viewHeight = getHeight() / 2;
        int sizeRect = 40;
        int circleRadius = 140;
        int numberOfCircles = 8;
        double spread = 360.0 / numberOfCircles;

        int leftTopX = viewWidth - sizeRect;
        int leftTopY = viewHeight - sizeRect;

        int rightBotX = viewWidth + sizeRect;
        int rightBotY = viewHeight + sizeRect;

        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0f, 0f, null);
        }

        for (int i = 0; i < numberOfCircles; ++i) {
            double angleRad = Math.toRadians(i * spread + angle);
            int xShift = (int) Math.round(circleRadius * Math.cos(angleRad));
            int yShift = (int) Math.round(circleRadius * Math.sin(angleRad));

            canvas.drawRoundRect(leftTopX + xShift, leftTopY + yShift,
                    rightBotX + xShift, rightBotY + yShift,
                    radius, radius, backgroundPaint);
        }
    }

    public void setBackgroundImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void startAnimator() {
        PropertyValuesHolder propertyRadius = PropertyValuesHolder.ofInt(PROPERTY_RADIUS, 0, 150);
        PropertyValuesHolder propertyAngle = PropertyValuesHolder.ofInt(PROPERTY_ANGLE, 0, 720);

        ValueAnimator animator = new ValueAnimator();
        animator.setValues(propertyRadius, propertyAngle);
        animator.setDuration(8000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                radius = (int) animation.getAnimatedValue(PROPERTY_RADIUS);
                angle = (int) animation.getAnimatedValue(PROPERTY_ANGLE);
                invalidate();
            }
        });
        animator.start();
    }
}
