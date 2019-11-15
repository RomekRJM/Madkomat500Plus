package com.example.madkomatapp.face;

public class FaceBuilder {
    private double left;
    private double top;
    private double width;
    private double height;
    private boolean smiling;
    private double smilingConfidence;
    private int ageRangeLow;
    private int ageRangeHigh;

    public FaceBuilder setLeft(double left) {
        this.left = left;
        return this;
    }

    public FaceBuilder setTop(double top) {
        this.top = top;
        return this;
    }

    public FaceBuilder setWidth(double width) {
        this.width = width;
        return this;
    }

    public FaceBuilder setHeight(double height) {
        this.height = height;
        return this;
    }

    public FaceBuilder setSmiling(boolean smiling) {
        this.smiling = smiling;
        return this;
    }

    public FaceBuilder setSmilingConfidence(double smilingConfidence) {
        this.smilingConfidence = smilingConfidence;
        return this;
    }

    public FaceBuilder setAgeRangeLow(int ageRangeLow) {
        this.ageRangeLow = ageRangeLow;
        return this;
    }

    public FaceBuilder setAgeRangeHigh(int ageRangeHigh) {
        this.ageRangeHigh = ageRangeHigh;
        return this;
    }

    public Face createFace() {
        return new Face(left, top, width, height, smiling, smilingConfidence, ageRangeLow, ageRangeHigh);
    }
}