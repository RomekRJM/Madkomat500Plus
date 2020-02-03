package com.example.madkomatapp.animatedimage;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

import com.example.madkomatapp.face.Face;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


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
    private static final String PROPERTY_FOREGROUND_OPACITY = "foreground_opacity";
    private static final long LOCKING_ANIMATION_LENGTH = 2500;
    private static final long PAUSE_BEFORE_FINISH = 750;
    private static final String TEXT = "Wyszukiwanie bąbelka...";

    private final Paint circlePaint = new Paint();
    private final Paint rectanglePaint = new Paint();
    private final Paint framePaint = new Paint();
    private final Paint redFacePaint = new Paint();
    private final Paint greenFacePaint = new Paint();
    private final Paint redFaceFramePaint = new Paint();
    private final Paint greenFaceFramePaint = new Paint();
    private final Paint redFaceTextPaint = new Paint();
    private final Paint greenFaceTextPaint = new Paint();
    private final Paint foregroundPaint = new Paint();

    private int frameColor;
    private final int redFaceColor = 0x77ff3700;
    private final int greenFaceColor = 0x7711a700;
    private final int redFaceFrameColor = 0xffff1700;
    private final int greenFaceFrameColor = 0xff11a700;
    private final int gold = 0xffffd700;
    private final int paleGold = 0xffeee8aa;
    private float rectangleHeightScale;
    private float rectangleWidthScale;
    private float rectanglePosition;

    private int left;
    private int top;
    private int right;
    private int bottom;
    private int foregroundOpacity;
    private AtomicInteger lastFaceFrameToDraw;

    private Bitmap background;
    private Bitmap foreground;
    private PathTracer pathTracer;
    private ValueAnimator rectangleAnimator;
    private ValueAnimator rectangleLockingAnimator;
    private ValueAnimator frameColorAnimator;
    private ValueAnimator foregroundAnimator;
    private Animation animation;

    private List<Face> faces;

    private AnimationListener animationListener;

    private enum Animation {
        WAITING, LOCKING, LOCKING_FINISHED, BLENDING_IN_RESULTS, FINISHED
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

        redFacePaint.setColor(redFaceColor);

        greenFacePaint.setColor(greenFaceColor);

        redFaceFramePaint.setColor(redFaceFrameColor);
        redFaceFramePaint.setStyle(Paint.Style.STROKE);
        redFaceFramePaint.setStrokeWidth(5.0f);

        redFaceTextPaint.setColor(redFaceFrameColor);
        redFaceTextPaint.setTextSize(42.0f);
        redFaceTextPaint.setTextAlign(Paint.Align.CENTER);

        greenFaceFramePaint.setColor(greenFaceFrameColor);
        greenFaceFramePaint.setStyle(Paint.Style.STROKE);
        greenFaceFramePaint.setStrokeWidth(5.0f);

        greenFaceTextPaint.setColor(greenFaceFrameColor);
        greenFaceTextPaint.setTextSize(42.0f);
        greenFaceTextPaint.setTextAlign(Paint.Align.CENTER);

        foregroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        lastFaceFrameToDraw = new AtomicInteger(0);

        pathTracer = new PathTracer(new PointF[]{
                new PointF(0.2f, 0.2f), new PointF(0.4f, 0.8f), new PointF(0.37f, 0.2f),
                new PointF(0.2f, 0.5f), new PointF(0.5f, 0.16f), new PointF(0.8f, 0.8f)
        });
    }

    protected void onDraw(Canvas canvas) {
        drawBitmap(canvas);

        switch (animation) {
            case WAITING:
                drawMovingRectangle(canvas);
                drawText(canvas);
                break;
            case LOCKING:
                drawAnimatedRectangle(canvas);
                break;
            case BLENDING_IN_RESULTS:
            case LOCKING_FINISHED:
                drawFaceFrames(canvas);
                break;
            case FINISHED:
                break;
        }
    }

    private void drawBitmap(Canvas canvas) {
        final Rect destination = new Rect(0, 0, getWidth(), getHeight());

        if (background != null) {
            canvas.drawBitmap(background, null, destination, null);
        }

        if (foreground != null) {
            foregroundPaint.setAlpha(foregroundOpacity);
            canvas.drawBitmap(foreground, null, destination, foregroundPaint);
        }
    }

    private void drawMovingRectangle(Canvas canvas) {
        int rectSize = Math.min(getWidth(), getHeight());
        int halfRectangleWidth = Math.round(rectSize / (rectangleWidthScale * 2));
        int halfRectangleHeight = Math.round(rectSize / (rectangleHeightScale * 2));

        PointF coordinate = pathTracer.getCoordinateAlongThePath(rectanglePosition);
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

    private void drawFaceFrames(Canvas canvas) {
        if (faces == null) {
            return;
        }

        int faceFrameIndex = 0;

        while (faceFrameIndex < lastFaceFrameToDraw.get()) {
            Face face = faces.get(faceFrameIndex);
            float left = (float) face.getLeft() * getWidth();
            float top = (float) face.getTop() * getHeight();

            canvas.drawRoundRect(
                    left, top,
                    left + (float) face.getWidth() * getWidth(),
                    top + (float) face.getHeight() * getHeight(),
                    25, 25,
                    face.isSmilingKid() ? greenFacePaint : redFacePaint);

            canvas.drawRoundRect(
                    left, top,
                    left + (float) face.getWidth() * getWidth(),
                    top + (float) face.getHeight() * getHeight(),
                    25, 25,
                    face.isSmilingKid() ? greenFaceFramePaint : redFaceFramePaint);
            ++faceFrameIndex;

            canvas.drawText(face.toString(),
                    left + (float) face.getWidth() * getWidth() / 2,
                    top + (float) face.getHeight() * getHeight() + 42,
                    face.isSmilingKid() ? greenFaceTextPaint : redFaceTextPaint);
        }
    }

    private void drawText(Canvas canvas) {
        canvas.drawText(TEXT.toCharArray(), 0, TEXT.length(), (float) getWidth() / 2,
                48.0f, framePaint);
    }

    public void setAnimationListener(AnimationListener animationListener) {
        this.animationListener = animationListener;
    }

    public void setBackgroundImage(Bitmap bitmap) {
        this.background = bitmap;
    }

    public void setForegroundImage(Bitmap bitmap) {
        this.foreground = bitmap;
    }

    public void startFaceFoundAnimation(final List<Face> faces) {
        this.faces = faces;
        lastFaceFrameToDraw.set(0);

        int cntr = 0;

        for (final Face face : faces) {
            new Handler().postDelayed(() -> startConsecutiveFaceFoundAnimation(face),
                    (cntr++) * LOCKING_ANIMATION_LENGTH);
        }

        new Handler().postDelayed(this::finishLockingAnimation,
                cntr * LOCKING_ANIMATION_LENGTH + PAUSE_BEFORE_FINISH);
    }

    public void startConsecutiveFaceFoundAnimation(Face face) {
        if (rectangleLockingAnimator != null) {
            rectangleLockingAnimator.end();
            rectangleLockingAnimator = null;
        }

        if (rectangleAnimator != null) {
            rectangleAnimator.end();
            rectangleAnimator = null;
        }

        int targetLeft = (int) Math.round(getWidth() * face.getLeft());
        int targetRight = targetLeft + (int) Math.round(getWidth() * face.getWidth());
        int targetTop = (int) Math.round(getHeight() * face.getTop());
        int targetBottom = targetTop + (int) Math.round(getHeight() * face.getHeight());

        PropertyValuesHolder propertyRectangleLeft = PropertyValuesHolder.ofInt(PROPERTY_RECTANGLE_LEFT, left, targetLeft);
        PropertyValuesHolder propertyRectangleRight = PropertyValuesHolder.ofInt(PROPERTY_RECTANGLE_RIGHT, right, targetRight);
        PropertyValuesHolder propertyRectangleTop = PropertyValuesHolder.ofInt(PROPERTY_RECTANGLE_TOP, top, targetTop);
        PropertyValuesHolder propertyRectangleBottom = PropertyValuesHolder.ofInt(PROPERTY_RECTANGLE_BOTTOM, bottom, targetBottom);

        rectangleLockingAnimator = new ValueAnimator();
        rectangleLockingAnimator.setValues(propertyRectangleLeft, propertyRectangleRight,
                propertyRectangleTop, propertyRectangleBottom);
        rectangleLockingAnimator.setDuration(LOCKING_ANIMATION_LENGTH);
        rectangleLockingAnimator.addUpdateListener(animation -> {
            left = (int) animation.getAnimatedValue(PROPERTY_RECTANGLE_LEFT);
            right = (int) animation.getAnimatedValue(PROPERTY_RECTANGLE_RIGHT);
            top = (int) animation.getAnimatedValue(PROPERTY_RECTANGLE_TOP);
            bottom = (int) animation.getAnimatedValue(PROPERTY_RECTANGLE_BOTTOM);
            invalidate();
        });
        rectangleLockingAnimator.start();
        rectangleLockingAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                lastFaceFrameToDraw.incrementAndGet();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        animation = Animation.LOCKING;
    }

    public void startAnimators() {
        restart();

        PropertyValuesHolder propertyRectanglePosition = PropertyValuesHolder.ofFloat(PROPERTY_RECTANGLE_POSITION, 0f, 100f);
        PropertyValuesHolder propertyRectangleHeight = PropertyValuesHolder.ofFloat(PROPERTY_RECTANGLE_HEIGHT_SCALE, 2.3f, 3.5f);
        PropertyValuesHolder propertyRectangleWidth = PropertyValuesHolder.ofFloat(PROPERTY_RECTANGLE_WIDTH_SCALE, 3.5f, 2.3f);

        rectangleAnimator = new ValueAnimator();
        rectangleAnimator.setValues(propertyRectanglePosition, propertyRectangleHeight,
                propertyRectangleWidth);
        rectangleAnimator.setDuration(8000);
        rectangleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rectangleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        rectangleAnimator.addUpdateListener(animation -> {
            rectanglePosition = (float) animation.getAnimatedValue(PROPERTY_RECTANGLE_POSITION);
            rectangleHeightScale = (float) animation.getAnimatedValue(PROPERTY_RECTANGLE_HEIGHT_SCALE);
            rectangleWidthScale = (float) animation.getAnimatedValue(PROPERTY_RECTANGLE_WIDTH_SCALE);
            invalidate();
        });
        rectangleAnimator.start();

        PropertyValuesHolder propertyAngle = PropertyValuesHolder.ofInt(PROPERTY_ANGLE, 0, 2160);
        PropertyValuesHolder propertyFrameColor = PropertyValuesHolder.ofInt(PROPERTY_FRAME_COLOR, gold, paleGold);

        frameColorAnimator = new ValueAnimator();
        frameColorAnimator.setInterpolator(new LinearInterpolator());
        frameColorAnimator.setValues(propertyAngle, propertyFrameColor);
        frameColorAnimator.setDuration(15000);
        frameColorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        frameColorAnimator.addUpdateListener(animation -> {
            frameColor = (int) animation.getAnimatedValue(PROPERTY_FRAME_COLOR);
            invalidate();
        });
        frameColorAnimator.start();

        animation = Animation.WAITING;
    }

    public void startForegroundAnimation() {
        PropertyValuesHolder propertyForegroundOpacity = PropertyValuesHolder.ofInt(PROPERTY_FOREGROUND_OPACITY, 0, 255);

        foregroundAnimator = new ValueAnimator();
        foregroundAnimator.setValues(propertyForegroundOpacity);
        foregroundAnimator.setDuration(3000);
        foregroundAnimator.addUpdateListener(animation -> {
            foregroundOpacity = (int) animation.getAnimatedValue(PROPERTY_FOREGROUND_OPACITY);
            invalidate();
        });
        foregroundAnimator.start();
    }

    private void finishLockingAnimation() {
        frameColorAnimator.end();
        frameColorAnimator = null;

        if (rectangleLockingAnimator != null) {
            rectangleLockingAnimator.end();
            rectangleLockingAnimator = null;
        }
        lastFaceFrameToDraw.decrementAndGet();

        if (animationListener != null) {
            animationListener.animationFinished();
        }

        animation = Animation.LOCKING_FINISHED;
        invalidate();
    }

    private void restart() {
        faces = null;
        animation = Animation.WAITING;
    }
}
