package com.diploma.stegovector.objects;

import java.util.List;

public class Curve {
    private String literal;
    private Point startPoint;
    private List<Segment> segments;


    public Curve(String literal, Point startPoint, List<Segment> segments) {
        this.literal = literal;
        this.startPoint = startPoint;
        this.segments = segments;
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }

    //    @Override
//    public String toString() {
//        return "Curve{\n" +
//                "\tliteral: '" + literal + "'\n" +
//                "\tstartPoint: " + startPoint + "\n" +
//                "\tsegments:\n" + segments + " \n" +
//                "}\n";
//    }
    @Override
    public String toString() {
        String segments = "";
        if (this.getSegments() != null) {
            for (int i = 0; i < this.segments.size(); i++) {
                if (i==0){
                    segments += this.segments.get(i).toString();
                } else {
                    segments += this.segments.get(i).pointsToString();
                }

            }
        }
        return this.literal + ' ' + this.startPoint.toString() + ' ' + segments;
    }
}
