package com.example.madkomatapp.face;

public class Face {

    private final double left;
    private final double top;
    private final double width;
    private final double height;
    private final boolean smiling;
    private final double smilingConfidence;
    private final int ageRangeLow;
    private final int ageRangeHigh;

    private static final int ADULT_THRESHOLD = 16;
    private static final double MIN_SMILING_CONFIDENCE = 60.0;

    public Face(double left, double top, double width, double height, boolean smiling, double smilingConfidence, int ageRangeLow, int ageRangeHigh) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.smiling = smiling;
        this.smilingConfidence = smilingConfidence;
        this.ageRangeLow = ageRangeLow;
        this.ageRangeHigh = ageRangeHigh;
    }

    public double getLeft() {
        return left;
    }

    public double getTop() {
        return top;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public boolean isSmiling() {
        return smiling;
    }

    public double getSmilingConfidence() {
        return smilingConfidence;
    }

    public int getAgeRangeLow() {
        return ageRangeLow;
    }

    public int getAgeRangeHigh() {
        return ageRangeHigh;
    }

    public boolean isSmilingKid() {
        return isSmiling() && getSmilingConfidence() >= MIN_SMILING_CONFIDENCE && isKid();
    }

    public boolean isKid() {
        return getAgeRangeLow() <= ADULT_THRESHOLD && getAgeRangeHigh() <= ADULT_THRESHOLD;
    }
}
