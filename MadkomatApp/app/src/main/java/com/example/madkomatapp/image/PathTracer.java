package com.example.madkomatapp.image;

import android.graphics.PointF;

import java.util.Arrays;

class PathTracer {
    private final PointF[] points;
    private double[] distances;
    private double totalDistance;

    PathTracer(PointF[] pts) {
        this.points = closePath(pts);
        this.distances = new double[points.length - 1];
        this.totalDistance = 0;

        initDistances();
    }

    private PointF[] closePath(PointF[] points) {
        if (points[0].equals(points[points.length - 1])) {
            return points;
        }

        PointF[] closed = Arrays.copyOf(points, points.length + 1);
        closed[points.length] = points[0];
        return closed;
    }

    private void initDistances() {
        for (int i = 0; i < points.length - 1; ++i) {
            PointF current = points[i];
            PointF next = points[i + 1];

            distances[i] = Math.sqrt(
                    Math.pow(next.x - current.x, 2.0)
                            + Math.pow(next.y - current.y, 2.0)
            );

            totalDistance += distances[i];
        }
    }

    PointF getCoordinateAlongThePath(double percent) {
        validate(percent);

        double targetDistance = totalDistance * percent / 100.0f;
        double distance = 0.0;
        int current;

        for (current = 0; current < distances.length - 1; ++current) {
            if (distance + distances[current] - targetDistance < 0.00001) {
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
        double x = points[current].x + coefficient * xDifference;
        double y = points[current].y + coefficient * yDifference;

        return new PointF((float)x, (float)y);
    }

    private void validate(double percent) {
        if (percent < 0.0 && percent > 100.0) {
            throw new IllegalArgumentException("percent should be in (0, 100) range, was " + percent);
        }
    }
}
