package com.diploma.stegovector.objects.experiment;

import com.diploma.stegovector.objects.*;

import java.util.ArrayList;
import java.util.List;

public class AffineTransformation {
    private Image container;
    private double a1;
    private double a2;
    private double a3;
    private double a4;
    private double b1;
    private double b2;
    private double e1;
    private double e2;
    private double e3;
    private double e4;
    private double e5;
    private double e6;
    private double[][] transformationMatrix;
    private int precision;

    public Image getContainer() {
        return container;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public AffineTransformation(Image container, double a1, double a2, double a3, double a4, double b1, double b2, double e1, double e2, double e3, double e4, double e5, double e6) {
        this.container = container;
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
        this.a4 = a4;
        this.b1 = b1;
        this.b2 = b2;
        this.e1 = e1;
        this.e2 = e2;
        this.e3 = e3;
        this.e4 = e4;
        this.e5 = e5;
        this.e6 = e6;
        this.transformationMatrix = initTransformationMatrix(a1, a2, a3, a4, b1, b2, e1, e2, e3, e4);

    }

    private double[][] initTransformationMatrix(double a1, double a2, double a3, double a4, double b1, double b2, double e1, double e2, double e3, double e4) {
        return new double[][]{
                {a1 + e1, a2 + e2, b1},
                {a3 + e3, a4 + e4, b2},
                {0, 0, 1}
        };
    }

    private Point applyAffineMatrixToPoint(Point point, double e5, double e6) {
        double[] xy1 = new double[]{point.getX() + e5, point.getY() + e6, 1};
        double[] res = new double[3];
        for (int i = 0; i < this.transformationMatrix.length; i++) {
            for (int j = 0; j < this.transformationMatrix[i].length; j++) {
                res[i] += transformationMatrix[i][j] * xy1[j];
            }
        }
        Point changedPoint = new Point(res[0], res[1], this.precision);
        changedPoint.roundPoint(this.precision);
        return changedPoint;
    }

    public void applyAffineTransformation() {
//        System.out.println("Image before transformation: " + this.container);
        Point breakPoint = new Point(0, 0, this.precision);
        for (List<Curve> curveList : this.container.getCurves()) {
            for (Curve curve : curveList) {
                for (Segment segment : curve.getSegments()) {
                    List<Point> changedPoints = new ArrayList<>();
                    for (Point point : segment.getPoints()) {
                        changedPoints.add(applyAffineMatrixToPoint(point, this.e5, this.e6));
                    }
                    segment.setPoints(changedPoints);
                }
                if ((curve.getStartPoint().subtractPoint(new Point(b1, b2, this.precision)).isSameWith(breakPoint))) {
                    curve.setStartPoint(curve.getStartPoint().subtractPoint(new Point(b1, b2, this.precision)));
                }
                curve.setStartPoint(applyAffineMatrixToPoint(curve.getStartPoint(), this.e5, this.e6));
            }
        }
        System.out.println("Transformed image: " + this.container);
    }

}

