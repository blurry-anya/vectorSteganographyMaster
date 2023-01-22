package com.diploma.stegovector.methods;

import com.diploma.stegovector.interfaces.Stegotransformation;
import com.diploma.stegovector.objects.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class PatternMethod implements Stegotransformation {
    private double startStep;
    private int patternLength;
    private int precision;
    private int maxBitAmount;
    private PatternTable patternTable;
    private Image image;
    private List<List<Double>> finalParameters;


    public PatternMethod(Image image) {
        this.image = image;
    }

    public PatternMethod(double startStep, int patternLength, int precision, int maxBitAmount, Image image) {
        this.startStep = startStep;
        this.patternLength = patternLength;
        this.precision = precision;
        this.maxBitAmount = maxBitAmount;
        this.image = image;
        setPatternTable(patternLength, maxBitAmount);
    }

    public void setPatternTable(int patternLength, int maxBitAmount) {
        this.patternTable = new PatternTable(patternLength, maxBitAmount);
    }

    public List<List<Double>> getFinalParameters(){
        return finalParameters;
    }

    public void setFinalParameters(List<List<Double>> parameters){
        this.finalParameters = parameters;
    }

    public void setFinalParameters(String link){
        this.finalParameters = getFinalParametersFromSvgElement(link);
        setPatternTable(patternLength, maxBitAmount);
    }

    public double getStartStep() {
        return startStep;
    }

    public int getPatternLength() {
        return patternLength;
    }

    public int getPrecision() {
        return precision;
    }

    public int getMaxBitAmount() {
        return maxBitAmount;
    }

    @Override
    public List<String> encode(Message message) {
        System.out.println("\t|--------------------------------------------------------------|");
        System.out.println("\t|------------Starting encoding with pattern method...--------------|");
        System.out.println("\t|--------------------------------------------------------------|");

        message.setBinaryMessage(this.maxBitAmount);
        List<String> input = message.getBinaryMessage();

        int indexOfBlock = 0;
        List<EncodedCurve> encodedCurves = new ArrayList<>();
        List<List<Double>> finalParameters = new ArrayList<>();
        for (List<Curve> listOfCurves : this.image.getCurves()) {
            for (Curve curve : listOfCurves) {
                System.out.println("\tEncoding of the curve: " + curve);

                List<Double> finalParametersOfSegment = new ArrayList<Double>();
                List<List<Segment>> resultSegments = new ArrayList<>();

                for (Segment segment : curve.getSegments()) {
                    System.out.println("Segment is " + segment);
                    double finalParameter = 0;
                    List<Segment> encodedSegments = new ArrayList<>();
                    if (segment.getValid() && indexOfBlock < input.size()) {
                        System.out.println("Input block is " + input.get(indexOfBlock));
                        List<String> inputValue = trimString(input.get(indexOfBlock), patternLength);
                        int indexOfSegment = 0;
                        List<List<Segment>> splitSegments = new ArrayList<>();
                        double k = startStep;
                        for (int i = 0; i < inputValue.size(); i++) {
                            System.out.println("Going to hide this pattern: " + inputValue.get(i));
                            PatternTableRecord currentRecord = patternTable.find(inputValue.get(i));
                            if (currentRecord != null) { // if exist
                                System.out.println("Found record: " + currentRecord);
                                System.out.println("Parameter: " + (k + currentRecord.step));
                                if (indexOfSegment == 0) {
                                    splitSegments.add(segment.splitSegment(BigDecimal.valueOf(k + currentRecord.step).setScale(6, RoundingMode.HALF_UP).doubleValue()));
                                    System.out.println("first split, segments are " + splitSegments);
                                } else {
                                    System.out.println("next split, working with previous: " + splitSegments.get(indexOfSegment - 1).get(1));
                                    splitSegments.add(splitSegments.get(indexOfSegment - 1).get(1).splitSegment(BigDecimal.valueOf(k + currentRecord.step).setScale(6, RoundingMode.HALF_UP).doubleValue()));
                                }
                                splitSegments.get(indexOfSegment).get(0).getPoints().remove(0);
                                encodedSegments.add(splitSegments.get(indexOfSegment).get(0));
                                indexOfSegment++;
                                k = BigDecimal.valueOf(k + currentRecord.step).setScale(6, RoundingMode.HALF_UP).doubleValue();
                            }
                        }
                        finalParameter = k;
                        splitSegments.get(indexOfSegment - 1).get(1).getPoints().remove(0);
                        encodedSegments.add(splitSegments.get(indexOfSegment - 1).get(1));
                        System.out.println("Got encoded segment:");
                        for (Segment seg : encodedSegments) {
                            System.out.println(seg);
                        }
                        System.out.println();
                        indexOfBlock++;
                    } else {
                        if (segment.getLiteral().matches("[Cc]")) {
                            segment.getPoints().remove(0);
                        }
                        encodedSegments.add(segment);
                    }
                    finalParametersOfSegment.add(finalParameter);
                    resultSegments.add(encodedSegments);

                }
                finalParameters.add(finalParametersOfSegment);
                encodedCurves.add(new EncodedCurve(curve.getLiteral(), curve.getStartPoint(), transformSetSegmentToEncodedCurve(resultSegments)));

            }

        }

        System.out.println("All final parameters: ");
        for (List<Double> list : finalParameters) {
            System.out.println(list);
        }
        System.out.println("All curves: ");
        for (EncodedCurve curve : encodedCurves) {
            System.out.println(curve);
        }

        this.finalParameters = finalParameters;
        System.out.println("FIN PARAMS AFTER ENCODE : " + this.finalParameters);

        List<String> parsed = getReadyForParsingIntoSvg(encodedCurves);
        System.out.println("Encoded");
        for (int i = 0; i < parsed.size(); i++) {
            System.out.println(parsed.get(i));
        }

        return getReadyForParsingIntoSvg(encodedCurves);
    }

    private List<Curve> transformSetSegmentToEncodedCurve(List<List<Segment>> list) {
        List<Curve> result = new ArrayList<>();
        for (List<Segment> segments : list) {
            result.add(new Curve("m", new Point(0, 0, this.precision), segments));
        }
        return result;
    }

    private List<String> getReadyForParsingIntoSvg(List<EncodedCurve> encodedCurves) {
        List<String> strings = new ArrayList<>();
        for (EncodedCurve encodedCurve : encodedCurves) {
            String strSegment = "";
            for (int i = 0; i < encodedCurve.getCurveSegments().size(); i++) {
                if (i == 0) {
                    strSegment += encodedCurve.getLiteral() + " " + encodedCurve.getStartPoint().toString() + " " +
                            encodedCurve.getCurveSegments().get(i).getSegments().toString();
                    strSegment = strSegment.replaceAll("[\\[\\]]", "");
                    strSegment = strSegment.replaceAll("(\s,\sC)", "\sC");
                } else {
                    strSegment += encodedCurve.getCurveSegments().get(i).toString();
                }
            }
            strings.add(strSegment);
        }
        return strings;
    }


    @Override
    public Message decode(double error) {
        System.out.println("\t|--------------------------------------------------------------|");
        System.out.println("\t|------------Starting decoding with pattern method...--------------|");
        System.out.println("\t|--------------------------------------------------------------|");

//        System.out.println("Table of patterns: ");
//        for (PatternTableRecord record : this.patternTable.getRecords()) {
//            System.out.println(record);
//        }

        System.out.println("Pattern length: " + patternLength);
        System.out.println("Max bit amount: " + maxBitAmount);
        System.out.println("Precision: " + precision);
        System.out.println("startStep: " + startStep);

        List<String> decodedBinaryBlocks = new ArrayList<>();
        System.out.println("Stego keys list: " + this.finalParameters);
        for (int i = 0; i < this.image.getCurves().size(); i++) {
//            System.out.println("Decoding curves: " + this.image.getCurves().get(i));
//            System.out.println("i: " + i);
//            System.out.println("this.image.getCurves().get(i): " + this.image.getCurves().get(i));
//            System.out.println("Size: " + this.image.getCurves().get(i).size());
            List<Curve> listOfCurves = this.image.getCurves().get(i);
            for (int j = 0; j < listOfCurves.size(); j++) {
//                System.out.println("Curve to be decoded: " + listOfCurves.get(j));
//                System.out.println("j: " + j);
//                System.out.println("Curve[j] segments: " + listOfCurves.get(j).getSegments());
//                System.out.println("Curve[j] segments size: " + listOfCurves.get(j).getSegments().size());
                if (listOfCurves.get(j).getSegments().size() > maxBitAmount / patternLength) {
                    StringBuilder output = new StringBuilder();
                    List<Segment> merged = listOfCurves.get(j).getSegments().subList(0, listOfCurves.get(j).getSegments().size());
//                    System.out.println("Value from steganokey: " + finalParameters.get(i).get(j));
                    double t = finalParameters.get(i).get(j), parameter = 0;
                    boolean flag = true;
                    int indexOfSegment = listOfCurves.get(j).getSegments().size() - 1;

                    if (t != 0.0) {
                        do {
//                            System.out.println("Index: " + indexOfSegment);

                            t = BigDecimal.valueOf(t).setScale(this.precision, RoundingMode.HALF_UP).doubleValue();
//                            System.out.println("Parameter t is " + t);
                            List<Segment> lastTwoSegments = new ArrayList<>();

                            if (indexOfSegment == 1) {
//                                System.out.println("First segments (indexOfSegment == 1)");
                                for (int k = 0; k < this.patternTable.getRecords().size(); k++) {
                                    double nextParameter = BigDecimal.valueOf(parameter - this.patternTable.getRecords().get(k).step).setScale(this.precision, RoundingMode.HALF_UP).doubleValue();
                                    if (nextParameter == startStep) {
                                        output.insert(0, this.patternTable.findStep(this.patternTable.getRecords().get(k).step).pattern);
                                        flag = false;
                                    }
                                }
                            } else {
//                                System.out.println("Center elements");
                                if (indexOfSegment == listOfCurves.get(j).getSegments().size() - 1) {
//                                    System.out.println("oh okay, that's last element");
                                    Segment mergedLastTwo = merged.get(indexOfSegment - 1).mergeSegments(t, merged.get(indexOfSegment));
                                    merged.remove(indexOfSegment);
                                    merged.set(indexOfSegment - 1, mergedLastTwo);
//                                    System.out.println("List after first merge: " + merged);
                                    indexOfSegment--;
                                }
//                                System.out.println("Merged before adding to list: " + merged);

                                lastTwoSegments.addAll(List.of(merged.get(merged.size() - 2), merged.get(merged.size() - 1)));

//                                System.out.println("List of segments: " + lastTwoSegments);
                                for (int k = 0; k < this.patternTable.getRecords().size(); k++) {
                                    parameter = BigDecimal.valueOf(t - this.patternTable.getRecords().get(k).step).setScale(this.precision, RoundingMode.HALF_UP).doubleValue();

//                                    System.out.println("k: " + k + "; parameter: " + parameter + "; t: " + t);
//                                    System.out.println("Pattern record: " + this.patternTable.getRecords().get(k));

                                    Point p02_2 = lastTwoSegments.get(1).getPoints().get(0).getAdditionalPoint(parameter, lastTwoSegments.get(1).getPoints().get(1));
                                    Point p11 = lastTwoSegments.get(1).getPoints().get(1).getAdditionalPoint(parameter, lastTwoSegments.get(1).getPoints().get(2));
                                    Point p02_3 = lastTwoSegments.get(0).getPoints().get(1).getCurvePoint(parameter, p11);
                                    p02_2.roundPoint(this.precision);
                                    p11.roundPoint(this.precision);
                                    p02_3.roundPoint(this.precision);

                                    if (
                                            p02_2.isDifferenceSmaller(p02_3, error) ||
                                                    lastTwoSegments.get(1).getPoints().get(2).isDifferenceSmaller(p02_2, error) ||
                                                    lastTwoSegments.get(1).getPoints().get(2).isDifferenceSmaller(p02_3, error)
                                    ) {
                                        output.insert(0, this.patternTable.findStep(this.patternTable.getRecords().get(k).step).pattern);
                                        lastTwoSegments.clear();
                                        Segment mergedSegments = merged.get(merged.size() - 2).mergeSegments(parameter, merged.get(merged.size() - 1));
                                        merged.remove(merged.size() - 2);
                                        merged.set(merged.size() - 1, mergedSegments);
//                                        System.out.println("Merged segment: " + mergedSegments);
                                        t = parameter;
                                        indexOfSegment = merged.size();
                                        break;
                                    }
                                    if (k == this.patternTable.getRecords().size() - 1) {
                                        System.out.println("\t\t-----ERROR: COULD NOT FIND A PARAMETER-------");
                                        flag = false;
                                    }
                                }
                            }
//                            System.out.println("output: " + output.toString());

                        } while (flag);
                        if(output.length() < this.maxBitAmount){
                            while (output.length() < this.maxBitAmount) {
                                output.append("0");
                            }
                        }
                        decodedBinaryBlocks.add(output.toString());
                    } else {
//                        System.out.println("Parameter t is 0.0, which means that curve was not encoded.");
                    }

                } else {
                    System.out.println("This curve was not encoded");
                }
            }
        }
        System.out.println("Decoded binary blocks: ");
        System.out.println(decodedBinaryBlocks);
        Message decodedMessage = new Message();
        decodedMessage.setText(decodedBinaryBlocks);
        decodedMessage.setBinaryMessage(decodedBinaryBlocks);
        System.out.println();
        return decodedMessage;
    }


    private List<List<Integer>> makeStegokey(List<List<Double>> finalParameters) {
        List<List<String>> finalParametersInBinary = new ArrayList<>();
        for (List<Double> list : finalParameters) {
            List<String> tmp = new ArrayList<>();
            for (double value : list) {
                tmp.add(decimalToBinary(value, 24).replace(".", ""));
            }
            finalParametersInBinary.add(tmp);
        }
        System.out.println("Final parameters in binary: " + finalParametersInBinary);

        List<List<Integer>> finalParametersNumbers = new ArrayList<>();
        for (List<String> list : finalParametersInBinary) {
            List<Integer> tmp = new ArrayList<>();
            for (String value : list) {
                tmp.add(getDecimal(value));
            }
            finalParametersNumbers.add(tmp);
        }
        System.out.println("Final parameters in numbers: " + finalParametersNumbers);

        return finalParametersNumbers;
    }

    public String parseStegokeyToSvgElement(List<List<Double>> finalParametersInDouble){
        List<List<Integer>> finalParametersInNumber = makeStegokey(finalParametersInDouble);
        String result = "";
        for (List<Integer> list:finalParametersInNumber) {
            result += "v";
            for (int i = 0; i < list.size(); i++) {
                result += list.get(i);
                if (i != list.size() - 1){
                    result += ":";
                }
            }
        }
        return parseParametersToSvgElement().concat(result);
    }

    private List<List<Double>> getFinalParametersFromSvgElement(String link){
        List<List<Double>> parameters = new ArrayList<>();
        List<String> splitBySlash = List.of(link.split("/"));

        List<String> params = List.of(splitBySlash.get(0).split(":"));
        int patternLength = 0, maxBitAmount = 0;
        double startStep = 0;
        for (String param:params) {
            String [] split = {param.substring(0,1), param.substring(1, param.length())};
            switch (split[0]){
                case "l":
                    this.patternLength = Integer.parseInt(split[1]);
                    break;
                case "s":
                    this.startStep = BigDecimal.valueOf(Double.parseDouble(split[1])).setScale(5, RoundingMode.HALF_UP).doubleValue();
                    break;
                case "b":
                    this.maxBitAmount = Integer.parseInt(split[1]);
                    break;
                case "p":
                    this.precision = Integer.parseInt(split[1]);
                    break;
            }
        }
        System.out.println("Got params: ");
        System.out.println("patternLength: " + patternLength);
        System.out.println("startStep: " + startStep);
        System.out.println("maxBitAmount: " + maxBitAmount);
        System.out.println("precision: " + precision);

        List<String> finalsList = List.of(splitBySlash.get(1).split("v"));

        System.out.println(finalsList);
        List<List<Integer>> finalParametersInInteger = new ArrayList<>();
        for (String finals:finalsList) {
            if (!Objects.equals(finals, "")){
                List<String> splitEl = List.of(finals.split(":"));
                List<Integer> integers = new ArrayList<>();
                for (String split:splitEl) {
                    integers.add(Integer.valueOf(split));
                }
                finalParametersInInteger.add(integers);
            }
        }

        parameters = getStegokey(finalParametersInInteger);
        System.out.println("Got parameters: ");
        System.out.println(parameters);

        return parameters;
    }

    private String parseParametersToSvgElement(){
        return "l" + this.patternLength + ":s" + this.startStep + ":b" + this.maxBitAmount + ":p" + this.precision + "/";
    }

    private int getDecimal(String binary) {
        List<String> binaryList = List.of(binary.split(""));
        List<Integer> castBinary = new ArrayList<>();
        for (int i = 0; i < binaryList.size(); i++) {
            castBinary.add(Integer.parseInt(binaryList.get(i)));
        }
        int decimal = 0;
        for (int i = 0, exponent = castBinary.size() - 1; i < castBinary.size(); i++) {
            decimal += castBinary.get(i) * Math.pow(2, exponent);
            exponent--;
        }
        return decimal;
    }


    private List<List<Double>> getStegokey(List<List<Integer>> finalParametersInInteger) {

        List<List<String>> finalParametersInBinary = new ArrayList<>();

        for (List<Integer> list : finalParametersInInteger) {
            List<String> tmp = new ArrayList<>();
            for (Integer value : list) {
                tmp.add("0." + getNbitString(Integer.toBinaryString(value), 24));
            }
            finalParametersInBinary.add(tmp);
        }

        System.out.println("From int to binary: " + finalParametersInBinary);

        List<List<Double>> result = new ArrayList<>();

        for (List<String> list : finalParametersInBinary) {
            List<Double> tmp = new ArrayList<>();
            for (String value : list) {
                tmp.add(BigDecimal.valueOf(binaryToDecimal(value, 24)).setScale(5, RoundingMode.HALF_UP).doubleValue());

            }
            result.add(tmp);
        }

        System.out.println("Extracted params: " + result);
        return result;
    }


    static double binaryToDecimal(String binary, int len) {

        // Fetch the radix point
        int point = binary.indexOf('.');

        // Update point if not found
        if (point == -1)
            point = len;

        double intDecimal = 0,
                fracDecimal = 0,
                twos = 1;

        // Convert integral part of binary to decimal
        // equivalent
        for (int i = point - 1; i >= 0; i--) {
            intDecimal += (binary.charAt(i) - '0') * twos;
            twos *= 2;
        }

        // Convert fractional part of binary to
        // decimal equivalent
        twos = 2;
        for (int i = point + 1; i < len; i++) {
            fracDecimal += (binary.charAt(i) - '0') / twos;
            twos *= 2.0;
        }

        // Add both integral and fractional part
        return intDecimal + fracDecimal;
    }


    private String decimalToBinary(double num, int k_prec) {
        String binary = "";

        // Fetch the integral part of decimal number
        int Integral = (int) num;

        // Fetch the fractional part decimal number
        double fractional = num - Integral;

        // Conversion of integral part to
        // binary equivalent
        while (Integral > 0) {
            int rem = Integral % 2;

            // Append 0 in binary
            binary += ((char) (rem + '0'));

            Integral /= 2;
        }

        // Reverse string to get original binary
        // equivalent
        binary = reverse(binary);

        // Append point before conversion of
        // fractional part
        binary += ('.');

        // Conversion of fractional part to
        // binary equivalent
        while (k_prec-- > 0) {
            // Find next bit in fraction
            fractional *= 2;
            int fract_bit = (int) fractional;

            if (fract_bit == 1) {
                fractional -= fract_bit;
                binary += (char) (1 + '0');
            } else {
                binary += (char) (0 + '0');
            }
        }

        return binary;
    }

    private String reverse(String input) {
        char[] temparray = input.toCharArray();
        int left, right = 0;
        right = temparray.length - 1;

        for (left = 0; left < right; left++, right--) {
            // Swap values of left and right
            char temp = temparray[left];
            temparray[left] = temparray[right];
            temparray[right] = temp;
        }
        return String.valueOf(temparray);
    }


    private class PatternTable {
        private List<PatternTableRecord> records;

        public PatternTable(int patternLength, int maxBitAmount) {

            double maxStep = (double) patternLength / maxBitAmount;
            double factDiff = maxStep / Math.pow(2, patternLength);
            double definedDiff = BigDecimal.valueOf(factDiff / 2).setScale(4, RoundingMode.HALF_UP).doubleValue();

/*            System.out.println("pattern length: " + patternLength);
            System.out.println("maxBitAmount: " + maxBitAmount);
            System.out.println("Math.pow(2, patternLength): " + Math.pow(2, patternLength));
            System.out.println("maxStep: " + maxStep);

            System.out.println("fact difference is " + factDiff);
            System.out.println("fact difference is " + definedDiff);*/

//            double [] stepsValue = new double[(int)Math.pow(2, patternLength)];
//            for (int i = stepsValue.length -1; i >= 0; i--) {
//                if (i == stepsValue.length - 1){
//                    stepsValue[i] = definedDiff;
//                } else {
//                    stepsValue[i] = BigDecimal.valueOf(stepsValue[i+1] + definedDiff).setScale(4, RoundingMode.HALF_UP).doubleValue();
//                }
//            }


            double[] stepsValue = {0.0095, 0.009, 0.0085, 0.008, 0.0075, 0.007, 0.0065, 0.006, 0.0055, 0.005, 0.0045, 0.004, 0.0035, 0.003, 0.0025, 0.002};
//            double[] stepsValue = {0.0075, 0.0095, 0.0009, 0.0085, 0.008, 0.0002, 0.007, 0.0065, 0.006, 0.0055, 0.005, 0.0045, 0.004, 0.0035, 0.003, 0.0025};

            List<PatternTableRecord> table = new ArrayList<>();
            for (int i = 1; i <= Math.pow(2, patternLength); i++) {
                table.add(new PatternTableRecord(i, getNbitString(Integer.toBinaryString(i - 1), 4), stepsValue[i - 1]));
            }

            this.records = table;

        }

        public List<PatternTableRecord> getRecords() {
            return records;
        }

        public PatternTableRecord find(String toFind) {
            PatternTableRecord res = null;
            for (PatternTableRecord record : this.getRecords()) {
                if (record.pattern.matches(toFind)) {
                    res = record;
                    break;
                }
            }
            return res;
        }

        public PatternTableRecord findStep(Double toFind) {
            PatternTableRecord res = null;
            for (PatternTableRecord record : this.getRecords()) {
                if (record.step == toFind) {
                    res = record;
                    break;
                }
            }
            return res;
        }
    }


    private class PatternTableRecord {
        private int index;
        private String pattern;
        private double step;

        public PatternTableRecord(int index, String pattern, double step) {
            this.index = index;
            this.pattern = pattern;
            this.step = step;
        }

        @Override
        public String toString() {
            return "PatternTableRecord{" +
                    "index: " + index +
                    ", pattern: '" + pattern + '\'' +
                    ", step: " + step +
                    '}';
        }
    }

    private String getNbitString(String string, int n) {
        String result = "";
        for (int i = 0; i < n - string.length(); i++) {
            result += "0";
        }
        result += string;
        return result;
    }

    private List<String> trimString(String string, int trimSize) {
        List<String> splitList = List.of(string.split(""));
        List<String> trimmed = new ArrayList<>();
        for (int i = 0; i < splitList.size(); i += trimSize) {
            trimmed.add(String.join("", splitList.subList(i, i + trimSize)));
        }
        return trimmed;
    }
}
