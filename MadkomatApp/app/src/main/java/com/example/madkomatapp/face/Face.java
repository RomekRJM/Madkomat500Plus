package com.example.madkomatapp.face;

public class Face {
    double left;
    double top;
    double width;
    double height;
    boolean smiling;
    double smilingConfidence;
    int ageRangeLow;
    int ageRangeHigh;

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
}
