package com.diploma.stegovector.objects.experiment;

public class ParametersSet {
    private int maxBitAmount;
    private int precision;
    private double error;

    public ParametersSet(int maxBitAmount, int precision, double error) {
        this.maxBitAmount = maxBitAmount;
        this.precision = precision;
        this.error = error;
    }

    public int getMaxBitAmount() {
        return maxBitAmount;
    }

    public int getPrecision() {
        return precision;
    }

    public double getError() {
        return error;
    }
}
