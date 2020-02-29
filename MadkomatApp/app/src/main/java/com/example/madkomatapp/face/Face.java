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

    private static final int ADULT_THRESHOLD = 18;
    private static final double MIN_SMILING_CONFIDENCE = 60.0;

    Face(double left, double top, double width, double height, boolean smiling,
         double smilingConfidence, int ageRangeLow, int ageRangeHigh) {
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

    private boolean isSmiling() {
        return smiling;
    }

    private double getSmilingConfidence() {
        return smilingConfidence;
    }

    private int getAgeRangeLow() {
        return ageRangeLow;
    }

    private int getAgeRangeHigh() {
        return ageRangeHigh;
    }

    public boolean isSmilingKid() {
        return isSmiling() && getSmilingConfidence() >= MIN_SMILING_CONFIDENCE && isKid();
    }

    private boolean isKid() {
        return getAgeRangeLow() <= ADULT_THRESHOLD && getAgeRangeHigh() <= ADULT_THRESHOLD;
    }

    public String toString() {
        return (isKid() ? "Dziecko" : "Dorosły") + " wiek: "
                + getAgeRangeLow() + "-" + getAgeRangeHigh() + " lat.";
    }
}
