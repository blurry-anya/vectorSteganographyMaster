package com.diploma.stegovector.objects;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Point {
    private double x;
    private double y;
    private int precision;

    /** so, a deal is that I totally forgot about doing calculations with a precision,
     * deshalb I had to add a precision variable much later **/
//    private int precision = 5;

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getPrecision(){
        return precision;
    }

    public Point(double x, double y, int precision) {
        this.x = x;
        this.y = y;
        this.precision = precision;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    /**
     * functions for encoding and decoding
     **/

    public double getDistanceWithAnother(Point point2) {
        return Math.sqrt(
                ((Math.pow(this.x - point2.x, 2)) + (Math.pow(this.y - point2.y, 2)))
        );
    }

    public Point addPoint(Point point2) {
        Point result = new Point(this.x + point2.x, this.y + point2.y, this.precision);
        result.roundPoint(precision);
        return result;
//        return new Point(this.x + point2.x, this.y + point2.y);
    }

    public Point subtractPoint(Point point2) {
        Point result = new Point((this.x - point2.x), (this.y - point2.y), this.precision);
        result.roundPoint(precision);
        return result;
//        return new Point(round((this.x - point2.x), 6), round((this.y - point2.y), 6));
    }

    public Point divideOn(double number) {
        return new Point(this.x / number, this.y / number, this.precision);

//        return new Point(round((this.x / number), 6), round((this.y / number), 6));
    }

    public Point multiplyOn(double number) {
        return new Point(this.x * number, this.y * number, this.precision);
//        return new Point(round((this.x * number), 6), round((this.y * number), 6));
    }

    public Point getCurvePoint(double parameter, Point point2) {//get point as expression (1-t)*P1+t*P2 (de Casteljau), i.e. to get point of curve
        return this.multiplyOn(1 - parameter).addPoint(point2.multiplyOn(parameter));
    }

    public Point getAdditionalPoint(double parameter, Point point2) {//get point as expression (point1 - t*point2)/(1 - t)
        Point result = this.subtractPoint(point2.multiplyOn(parameter)).divideOn(1 - parameter);
        result.roundPoint(precision);
        return result;
//        return (this.subtractPoint(point2.multiplyOn(parameter)).divideOn(1 - parameter)).roundPoint(6);
    }

    public Point getInterimPointOnAdditionalLines(double parameter, Point point2) { //for splitting curve (calc points for Casteljau matrix)
        // P = P + (P1 - P2) * t
        return this.addPoint(point2.subtractPoint(this).multiplyOn(parameter));
    }

    private double[] getDifferenceWith(Point point2) {
        return new double[]{Math.abs(this.x - point2.x), Math.abs(this.y - point2.y)};
    }

    public boolean isDifferenceSmaller(Point point2, double error) {
        double[] compareValues = this.getDifferenceWith(point2);
        return compareValues[0] <= error && compareValues[1] <= error;
    }

    public boolean isSameWith(Point point2) {
        return this.x == point2.x && this.y == point2.y;
    }

    public void roundPoint(int precision) {
        this.setX(round(this.x, precision));
        this.setY(round(this.y, precision));
    }

    @Override
    public String toString() {
        return Double.toString(this.getX()) + ',' + Double.toString(this.getY());
    }


    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}