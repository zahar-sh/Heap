package com.example.heap.controller;

public class Geometry {

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void clamp(Point point, int minX, int maxX, int minY, int maxY) {
        point.setX(Geometry.clamp(point.getX(), minX, maxX));
        point.setY(Geometry.clamp(point.getY(), minY, maxY));
    }

    public static double radiansOf(int x1, int y1, int x2, int y2) {
        x2 -= x1;
        y2 -= y1;
        double v = Math.acos(y2 / Math.sqrt(x2 * x2 + y2 * y2));
        return x2 > 0 ? -v : v;
    }

    public static boolean containsPoint(int minX, int minY, int maxX, int maxY, int x, int y) {
        return x > minX && x < maxX && y > minY && y < maxY;
    }
}
