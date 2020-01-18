package com.example.madkomatapp.image;

import android.graphics.Point;

import java.util.Arrays;

public class PathTracer {
    private final Point[] points;
    private double[] distances;
    private double totalDistance;

    public PathTracer(Point[] pts) {
        this.points = closePath(pts);
        this.distances = new double[points.length - 1];
        this.totalDistance = 0;

        initDistances();
    }

    private Point[] closePath(Point[] points) {
        if (points[0].equals(points[points.length - 1])) {
            return points;
        }

        Point[] closed = Arrays.copyOf(points, points.length + 1);
        closed[points.length] = points[0];
        return closed;
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
        validate(percent);

        double targetDistance = totalDistance * percent / 100.0;
        double distance = 0.0;
        int current;

        for (current = 0; current < points.length - 1; ++current) {
            if (distance + distances[current] - targetDistance < 0.001) {
                distance += distances[current];
            } else {
                break;
            }
        }

        int next = current + 1;

        double remainingPercentage = percent - distance * 100 / totalDistance;
        double coefficient = remainingPercentage / 100 * totalDistance / distances[current];

        double xDifference = (double) points[next].x - (double) points[current].x;
        double yDifference = (double) points[next].y - (double) points[current].y;
        int x = (int) Math.round(points[current].x + coefficient * xDifference);
        int y = (int) Math.round(points[current].y + coefficient * yDifference);

        return new Point(x, y);
    }

    private void validate(double percent) {
        if (percent < 0.0 && percent > 100.0) {
            throw new IllegalArgumentException("percent should be in (0, 100) range, was " + percent);
        }
    }
}
