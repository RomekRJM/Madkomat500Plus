package com.example.madkomatapp.animatedimage;

import android.graphics.PointF;

import java.util.Arrays;

class PathTracer {
    private final PointF[] points;
    private float[] distances;
    private float totalDistance;

    PathTracer(PointF[] pts) {
        this.points = closePath(pts);
        this.distances = new float[points.length - 1];
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

            distances[i] = (float) Math.sqrt(
                    Math.pow((double) next.x - (double) current.x, 2.0)
                            + Math.pow((double) next.y - (double) current.y, 2.0)
            );

            totalDistance += distances[i];
        }
    }

    PointF getCoordinateAlongThePath(float percent) {
        validate(percent);

        float targetDistance = totalDistance * percent / 100.0f;
        float distance = 0.0f;
        int current;

        for (current = 0; current < distances.length - 1; ++current) {
            if (distance + distances[current] - targetDistance < 0.00001) {
                distance += distances[current];
            } else {
                break;
            }
        }

        int next = current + 1;

        float remainingPercentage = percent - distance * 100 / totalDistance;
        float coefficient = remainingPercentage / 100 * totalDistance / distances[current];

        float xDifference = points[next].x - points[current].x;
        float yDifference = points[next].y - points[current].y;
        float x = points[current].x + coefficient * xDifference;
        float y = points[current].y + coefficient * yDifference;

        return new PointF(x, y);
    }

    private void validate(float percent) {
        if (percent < 0.0 && percent > 100.0) {
            throw new IllegalArgumentException("percent should be in (0, 100) range, was " + percent);
        }
    }
}
