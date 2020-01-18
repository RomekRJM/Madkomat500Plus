package com.example.madkomatapp.image;

import android.graphics.Point;

public class PathTracer {
    private final Point[] points;
    private double[] distances;
    private double totalDistance;

    public PathTracer(Point[] points) {
        this.points = points;
        this.totalDistance = 0;

        initDistances();
    }

    private void initDistances() {
        for (int i = 0; i < points.length - 1; ++i) {
            Point current = points[i];
            Point next = points[i + 1];

            distances[i] = Math.sqrt(
                    Math.pow((double) next.x - (double) current.x, 2.0)
                            + Math.pow((double) next.y - (double) current.y, 2.0)
            );

            totalDistance += distances[i];
        }
    }

    public Point getCoordinateAlongThePath(double percent) {
        return new Point(0, 0);
    }
}
