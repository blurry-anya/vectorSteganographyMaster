package com.diploma.stegovector.objects;

import java.util.List;

public class EncodedCurve {
    private String literal;
    private Point startPoint;
    private List<Curve> curveSegments;

    public EncodedCurve(String literal, Point startPoint, List<Curve> curveSegments) {
        this.literal = literal;
        this.startPoint = startPoint;
        this.curveSegments = curveSegments;
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

    public List<Curve> getCurveSegments() {
        return curveSegments;
    }

    public void setCurveSegments(List<Curve> curveSegments) {
        this.curveSegments = curveSegments;
    }




    @Override
    public String toString() {
        String segments = "";
        for (int i = 0; i < this.curveSegments.size(); i++) {
            segments += this.curveSegments.get(i).toString() + ' ';
        }

        return this.literal + ' ' + this.startPoint.toString() + ' ' + segments;
    }

    /*@Override
    public String toString() {
        return "EncodedCurve{" +
                "literal='" + literal + '\'' +
                ", \nstartPoint=" + startPoint +
                ", \ncurveSegments=" + curveSegments +
                '}';
    }*/


}
