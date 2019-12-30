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
    private final Paint backgroundPaint = new Paint();

    private int radius;
    private Bitmap bitmap;

    public ImagePreview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        backgroundPaint.setColor(0xffaa44ff);
    }

    protected void onDraw(Canvas canvas) {
        int viewWidth = getWidth() / 2;
        int viewHeight = getHeight() / 2;

        int leftTopX = viewWidth - 150;
        int leftTopY = viewHeight - 150;

        int rightBotX = viewWidth + 150;
        int rightBotY = viewHeight + 150;

        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0f, 0f, null);
        }

        canvas.drawRoundRect(leftTopX, leftTopY, rightBotX, rightBotY, radius, radius, backgroundPaint);
    }

    public void setBackgroundImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void startAnimator() {
        PropertyValuesHolder propertyRadius = PropertyValuesHolder.ofInt(PROPERTY_RADIUS, 0, 150);

        ValueAnimator animator = new ValueAnimator();
        animator.setValues(propertyRadius);
        animator.setDuration(2000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                radius = (int) animation.getAnimatedValue(PROPERTY_RADIUS);
                invalidate();
            }
        });
        animator.start();
    }
}
