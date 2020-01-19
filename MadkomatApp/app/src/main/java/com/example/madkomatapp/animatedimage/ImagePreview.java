package com.example.madkomatapp.animatedimage;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

import com.example.madkomatapp.face.Face;
import com.example.madkomatapp.face.FaceBuilder;

import java.util.Random;


public class ImagePreview extends AppCompatImageView {
    private static final String PROPERTY_ANGLE = "angle";
    private static final String PROPERTY_RECTANGLE_POSITION = "rectangle_position";
    private static final String PROPERTY_RECTANGLE_HEIGHT_SCALE = "rectangle_height_scale";
    private static final String PROPERTY_RECTANGLE_WIDTH_SCALE = "rectangle_width_scale";
    private static final String PROPERTY_RECTANGLE_LEFT = "rectangle_left";
    private static final String PROPERTY_RECTANGLE_RIGHT = "rectangle_right";
    private static final String PROPERTY_RECTANGLE_TOP = "rectangle_top";
    private static final String PROPERTY_RECTANGLE_BOTTOM = "rectangle_bottom";
    private static final String PROPERTY_FRAME_COLOR = "frame_color";

    private static final String TEXT = "Wyszukiwanie bąbelka...";

    private final Paint circlePaint = new Paint();
    private final Paint rectanglePaint = new Paint();
    private final Paint framePaint = new Paint();

    private int frameColor;
    private final int gold = 0xffffd700;
    private final int paleGold = 0xffeee8aa;
    private float rectangleHeightScale;
    private float rectangleWidthScale;
    private float rectangleOrbitRadius;

    private int left;
    private int top;
    private int right;
    private int bottom;

    private Bitmap bitmap;
    private PathTracer pathTracer;

    private ValueAnimator rectangleAnimator;
    private ValueAnimator rectangleLockingAnimator;
    private ValueAnimator circleAnimator;
    private Animation animation;

    private enum Animation {
        WAITING, LOCKING, FOUND
    }

    public ImagePreview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        circlePaint.setColor(0xffaa44ff);
        rectanglePaint.setColor(0x576888ff);
        framePaint.setColor(gold);
        framePaint.setStrokeWidth(8.0f);
        framePaint.setTextSize(42.0f);
        framePaint.setTextAlign(Paint.Align.CENTER);
        framePaint.setFakeBoldText(true);

        pathTracer = new PathTracer(new PointF[]{
                new PointF(0.2f, 0.2f), new PointF(0.4f, 0.8f), new PointF(0.37f, 0.2f),
                new PointF(0.2f, 0.5f), new PointF(0.5f, 0.16f), new PointF(0.8f, 0.8f)
        });
    }

    protected void onDraw(Canvas canvas) {
        drawBitmap(canvas);

        switch(animation) {
            case WAITING:
                drawMovingRectangle(canvas);
                drawText(canvas);
                break;
            case LOCKING:
                drawAnimatedRectangle(canvas);
                break;
        }
    }

    private void drawBitmap(Canvas canvas) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0f, 0f, null);
        }
    }

    private void drawMovingRectangle(Canvas canvas) {
        int rectSize = Math.min(getWidth(), getHeight());
        int halfRectangleWidth = Math.round(rectSize / (rectangleWidthScale * 2));
        int halfRectangleHeight = Math.round(rectSize / (rectangleHeightScale * 2));

        PointF coordinate = pathTracer.getCoordinateAlongThePath(rectangleOrbitRadius);
        int xShift = Math.round(getWidth() * coordinate.x);
        int yShift = Math.round(getHeight() * coordinate.y);

        left = xShift - halfRectangleWidth;
        top = yShift - halfRectangleHeight;
        right = xShift + halfRectangleWidth;
        bottom = yShift + halfRectangleHeight;

        drawAnimatedRectangle(canvas);
    }

    private void drawAnimatedRectangle(Canvas canvas) {
        canvas.drawRect(0, 0, left, getHeight(), rectanglePaint);
        canvas.drawRect(0, 0, getWidth(), top, rectanglePaint);
        canvas.drawRect(right, 0, getWidth(), getHeight(), rectanglePaint);
        canvas.drawRect(0, bottom, getWidth(), getHeight(), rectanglePaint);

        framePaint.setColor(frameColor);
        canvas.drawLines(new float[]{
                left, top, right, top,
                right, top, right, bottom,
                right, bottom, left, bottom,
                left, bottom, left, top
        }, framePaint);
    }

    private void drawText(Canvas canvas) {
        canvas.drawText(TEXT.toCharArray(),0, TEXT.length(), (float)getWidth()/2,
                48.0f, framePaint);
    }

    public void setBackgroundImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void startFaceFoundAnimation(Face face) {
        rectangleAnimator.end();

        int targetLeft = (int)Math.round(getWidth() * face.getLeft());
        int targetRight = targetLeft + (int)Math.round(getWidth() * face.getWidth());
        int targetTop = (int)Math.round(getHeight() * face.getTop());
        int targetBottom = targetTop + (int)Math.round(getHeight() * face.getHeight());

        PropertyValuesHolder propertyRectangleLeft = PropertyValuesHolder.ofInt(PROPERTY_RECTANGLE_LEFT, left, targetLeft);
        PropertyValuesHolder propertyRectangleRight = PropertyValuesHolder.ofInt(PROPERTY_RECTANGLE_RIGHT, right, targetRight);
        PropertyValuesHolder propertyRectangleTop = PropertyValuesHolder.ofInt(PROPERTY_RECTANGLE_TOP, top, targetTop);
        PropertyValuesHolder propertyRectangleBottom = PropertyValuesHolder.ofInt(PROPERTY_RECTANGLE_BOTTOM, bottom, targetBottom);

        rectangleLockingAnimator = new ValueAnimator();
        rectangleLockingAnimator.setValues(propertyRectangleLeft, propertyRectangleRight,
                propertyRectangleTop, propertyRectangleBottom);
        rectangleLockingAnimator.setDuration(2500);
        rectangleLockingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                left = (int) animation.getAnimatedValue(PROPERTY_RECTANGLE_LEFT);
                right = (int) animation.getAnimatedValue(PROPERTY_RECTANGLE_RIGHT);
                top = (int) animation.getAnimatedValue(PROPERTY_RECTANGLE_TOP);
                bottom = (int) animation.getAnimatedValue(PROPERTY_RECTANGLE_BOTTOM);
                invalidate();
            }
        });
        rectangleLockingAnimator.start();

        animation = Animation.LOCKING;
    }

    public void startAnimators() {
        PropertyValuesHolder propertyRectanglePosition = PropertyValuesHolder.ofFloat(PROPERTY_RECTANGLE_POSITION, 0f, 100f);
        PropertyValuesHolder propertyRectangleHeight = PropertyValuesHolder.ofFloat(PROPERTY_RECTANGLE_HEIGHT_SCALE, 2.3f, 3.5f);
        PropertyValuesHolder propertyRectangleWidth = PropertyValuesHolder.ofFloat(PROPERTY_RECTANGLE_WIDTH_SCALE, 3.5f, 2.3f);

        rectangleAnimator = new ValueAnimator();
        rectangleAnimator.setValues(propertyRectanglePosition, propertyRectangleHeight,
                propertyRectangleWidth);
        rectangleAnimator.setDuration(8000);
        rectangleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rectangleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        rectangleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rectangleOrbitRadius = (float) animation.getAnimatedValue(PROPERTY_RECTANGLE_POSITION);
                rectangleHeightScale = (float) animation.getAnimatedValue(PROPERTY_RECTANGLE_HEIGHT_SCALE);
                rectangleWidthScale = (float) animation.getAnimatedValue(PROPERTY_RECTANGLE_WIDTH_SCALE);
                invalidate();
            }
        });
        rectangleAnimator.start();

        PropertyValuesHolder propertyAngle = PropertyValuesHolder.ofInt(PROPERTY_ANGLE, 0, 2160);
        PropertyValuesHolder propertyFrameColor = PropertyValuesHolder.ofInt(PROPERTY_FRAME_COLOR, gold, paleGold);

        circleAnimator = new ValueAnimator();
        circleAnimator.setInterpolator(new LinearInterpolator());
        circleAnimator.setValues(propertyAngle, propertyFrameColor);
        circleAnimator.setDuration(15000);
        circleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        circleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                frameColor = (int) animation.getAnimatedValue(PROPERTY_FRAME_COLOR);
                invalidate();
            }
        });
        circleAnimator.start();

        animation = Animation.WAITING;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startFaceFoundAnimation(new FaceBuilder()
                        .setTop(0.3001307249069214)
                        .setLeft(0.36556869745254517)
                        .setHeight(0.17334245145320892)
                        .setWidth(0.22835981845855713)
                        .createFace()
                );
            }
        }, new Random().nextInt(3000) + 5000);
    }
}
