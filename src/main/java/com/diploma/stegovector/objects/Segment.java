package com.diploma.stegovector.objects;

import java.util.ArrayList;
import java.util.List;

public class Segment {
    private String literal;
    private List<Point> points;
    private boolean valid;


    public Segment() {

    }

    public Segment(String literal, List<Point> points, boolean valid) {
        this.literal = literal;
        this.points = points;
        this.valid = valid;
    }


    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }


    public boolean isValid() {
        boolean isValid = true;
        if (!this.literal.matches("[Cc]")) {
            isValid = false;
        } else {
            for (int j = 1; j < this.getPoints().size(); j++) {
                if (this.getPoints().get(j).getDistanceWithAnother(this.getPoints().get(j - 1)) <= 1) {
                    isValid = false;
                    break;
                }
            }
        }

        return isValid;
    }

    private Point[][] initCasteljauMatrix() {
        return new Point[][]{
                {new Point(0, 0, this.points.get(1).getPrecision()), new Point(0, 0, this.points.get(1).getPrecision()), new Point(0, 0, this.points.get(1).getPrecision()), new Point(0, 0, this.points.get(1).getPrecision())},
                {new Point(0, 0, this.points.get(1).getPrecision()), new Point(0, 0, this.points.get(1).getPrecision()), new Point(0, 0, this.points.get(1).getPrecision())},
                {new Point(0, 0, this.points.get(1).getPrecision()), new Point(0, 0, this.points.get(1).getPrecision())},
                {new Point(0, 0, this.points.get(1).getPrecision())},
        };
    }

    public List<Segment> splitSegment(double parameter) {
        Point[][] matrix = initCasteljauMatrix();
        List<Point> segment1 = new ArrayList<>();
        List<Point> segment2 = new ArrayList<>();

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (i == 0) {
                    matrix[i][j] = this.points.get(j);
                } else {
                    matrix[i][j] = matrix[i - 1][j].getInterimPointOnAdditionalLines(parameter, matrix[i - 1][j + 1]);
                }
            }
        }


        for (int i = 0; i < matrix.length; i++) {
            segment1.add(matrix[i][0]);
            segment2.add(matrix[i][matrix[i].length - 1]);
        }

        Segment first = new Segment(this.literal, segment1, false);
        Segment second = new Segment(this.literal, segment2, false);
        second.reversePointOrder();

        return List.of(first, second);

    }

    private void reversePointOrder() {
        List<Point> reversed = new ArrayList<>();
        for (int i = this.getPoints().size() - 1; i >= 0; i--) {
            reversed.add(this.getPoints().get(i));
        }
        this.setPoints(reversed);
    }

    public Segment mergeSegments(double parameter, Segment segment2) {
        Point p2 = segment2.points.get(2).getAdditionalPoint(parameter, segment2.points.get(3));
        Point p11 = segment2.points.get(1).getAdditionalPoint(parameter, segment2.points.get(2));
        Point p1 = p11.getAdditionalPoint(parameter, p2);

        return new Segment(this.literal, List.of(this.points.get(0), p1, p2, segment2.points.get(3)), false);
    }

    public String pointsToString() {
        String points = "";
        if (this.getPoints() != null) {
            for (Point point : this.getPoints()) {
                points += point.toString() + ' ';
            }
        }
        return points;
    }

    @Override
    public String toString() {
        String points = "";
        if (this.getPoints() != null) {
            for (Point point : this.getPoints()) {
                points += point.toString() + ' ';
            }
        }
        return this.literal + ' ' + points;
    }

}
