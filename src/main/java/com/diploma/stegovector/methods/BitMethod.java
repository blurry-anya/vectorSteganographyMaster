package com.diploma.stegovector.methods;

import com.diploma.stegovector.interfaces.Stegotransformation;
import com.diploma.stegovector.objects.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BitMethod implements Stegotransformation {
    private double parameter;
    private int precision;
    private int maxBitAmount;
    private Image image;

    public BitMethod(Image image) {
        this.image = image;
    }


    public BitMethod(double parameter, int precision, int maxBitAmount, Image image) {
        this.parameter = parameter;
        this.precision = precision;
        this.maxBitAmount = maxBitAmount;
        this.image = image;
    }

    public double getParameter() {
        return parameter;
    }

    public void setParameter(double parameter) {
        this.parameter = parameter;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getMaxBitAmount() {
        return maxBitAmount;
    }

    public void setMaxBitAmount(int maxBitAmount) {
        this.maxBitAmount = maxBitAmount;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }


    @Override
    public List<String> encode(Message message) {
        System.out.println("\t|--------------------------------------------------------------|");
        System.out.println("\t|------------Starting encoding with bit method...--------------|");
        System.out.println("\t|--------------------------------------------------------------|");

        message.setBinaryMessage(this.maxBitAmount);
        List<String> input = message.getBinaryMessage();

        System.out.println("Blocks of input: " + input.size());
        System.out.println("Amount of curves: " + this.image.getAmountOfCurves());


        System.out.println("Amount of segments: " + this.image.getAmountOfSegments());
        System.out.println("Amount of valid segments: " + this.image.getAmountOfValidSegments());


        int indexOfBlock = 0;

        List<List<Boolean>> resultMarks = new ArrayList<>();
        List<EncodedCurve> encodedCurves = new ArrayList<>();
        for (List<Curve> listOfCurves : this.image.getCurves()) {
            for (Curve curve : listOfCurves) {
                System.out.println("\tEncoding of the curve: " + curve);
                List<Boolean> wasEncodedMarks = new ArrayList<Boolean>();
                List<List<Segment>> result = new ArrayList<>();
                for (Segment segment : curve.getSegments()) {
                    Boolean flag = false;
                    List<Segment> newSegments = new ArrayList<>();
                    System.out.println("Current segment: " + segment + " -->  valid: " + segment.getValid());
                    List<Segment> encodedSegments = new ArrayList<>();
                    if (segment.getValid() && indexOfBlock < input.size()) {
                        int indexOfSegment = 0;
                        String[] currBlock = input.get(indexOfBlock).split("");
                        List<List<Segment>> splittedSegments = new ArrayList<>(); // store pairs of split segments
                        double k = this.parameter;
                        for (int i = 0; i < currBlock.length; i++) {
                            System.out.println("k: " + k);

                            if (Objects.equals(currBlock[i], "1")) {
                                System.out.println("got '1'");
                                if (indexOfSegment == 0) {
//                                System.out.println("First segment");
                                    splittedSegments.add(segment.splitSegment(k));
                                } else {
//                                System.out.println("Next element for splitting: " + splittedSegments.get(indexOfSegment-1).get(1));
                                    splittedSegments.add(splittedSegments.get(indexOfSegment - 1).get(1).splitSegment(k)); // take second part of split pair, because it was not yet used for splitting
                                }
                                splittedSegments.get(indexOfSegment).get(0).getPoints().remove(0); // removing point needed for canonical view
                                encodedSegments.add(splittedSegments.get(indexOfSegment).get(0)); // adding first part of split pair
                                indexOfSegment++;
                            } else {
                                System.out.println("got '0'");
                            }
                            k = BigDecimal.valueOf(k + this.parameter).setScale(6, RoundingMode.HALF_UP).doubleValue();
                        }
                        splittedSegments.get(indexOfSegment - 1).get(1).getPoints().remove(0);
                        encodedSegments.add(splittedSegments.get(indexOfSegment - 1).get(1)); // adding last segment, that does not need to be split
                        System.out.println("encoded curves: " + encodedCurves);
                        newSegments.addAll(encodedSegments);
                        indexOfBlock++;
                        flag = true;
                    } else {
                        if (segment.getLiteral().matches("[Cc]")) {
                            segment.getPoints().remove(0);
                        }
                        newSegments.add(segment);
                    }
                    wasEncodedMarks.add(flag);
                    result.add(newSegments);
                }
                resultMarks.add(wasEncodedMarks);

                encodedCurves.add(new EncodedCurve(curve.getLiteral(), curve.getStartPoint(), transformSetSegmentToEncodedCurve(result)));
            }
        }

        /*System.out.println("All segments: ");
        for (List<SegmentModel> list : result) {
            System.out.println(list);
        }*/
        System.out.println("All marks: ");
        for (List<Boolean> list : resultMarks) {
            System.out.println(list);
        }
        System.out.println("All curves: ");
        for (EncodedCurve curve : encodedCurves) {
            System.out.println(curve);
        }

        List<String> parsed = getReadyForParsingIntoSvg(encodedCurves);

        return parsed;
    }


    private List<Curve> transformSetSegmentToEncodedCurve(List<List<Segment>> list) {
        List<Curve> result = new ArrayList<>();
        for (List<Segment> segments : list) {
            result.add(new Curve("m", new Point(0, 0, this.precision), segments));
        }
        return result;
    }

    public String parseParametersToSvgElement() {
        return "k" + this.parameter + ":b" + this.maxBitAmount + ":p" + this.precision + "/";
    }

    public void getParametersFromSvgElement(String link){
        List<String> splitBySlash = List.of(link.split("/"));

        List<String> params = List.of(splitBySlash.get(0).split(":"));

        for (String param:params) {
            String [] split = {param.substring(0,1), param.substring(1, param.length())};
            switch (split[0]){
                case "k":
                    this.parameter = BigDecimal.valueOf(Double.parseDouble(split[1])).setScale(5, RoundingMode.HALF_UP).doubleValue();
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
        System.out.println("parameter: " + parameter);
        System.out.println("maxBitAmount: " + maxBitAmount);
        System.out.println("precision: " + precision);

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
                    strSegment += encodedCurve.getCurveSegments().get(i);
                }
            }
            strings.add(strSegment);
        }
        return strings;
    }

    @Override
    public Message decode(double error) {
        System.out.println("\t|--------------------------------------------------------------|");
        System.out.println("\t|------------Starting decoding with bit method...--------------|");
        System.out.println("\t|--------------------------------------------------------------|");

        List<String> decodedBinaryBlocks = new ArrayList<>();

        for (int i = 0; i < this.image.getCurves().size(); i++) {
            System.out.println("Decoding curves: " + this.image.getCurves().get(i));
            int indexOfFlag = 0;
            for (Curve curve : this.image.getCurves().get(i)) {
                System.out.println("Curve to be decoded: " + curve);
//                System.out.println("Encoding mark: " + encodeFlags.get(i).get(indexOfFlag));
//                if (encodeFlags.get(i).get(indexOfFlag) && curve.getSegments().size() > 1) {
                if (curve.getSegments().size() > 1) {
                    StringBuilder output = new StringBuilder();
                    double lastEncodedValue = BigDecimal.valueOf((this.maxBitAmount + 1) * this.parameter).setScale(this.precision, RoundingMode.HALF_UP).doubleValue();
                    System.out.println("Last parameter: " + lastEncodedValue);
                    List<Segment> copy = curve.getSegments().subList(0, curve.getSegments().size());
                    System.out.println("Obtained copy: " + copy);
                    for (double t = lastEncodedValue - parameter; t >= 0; t -= parameter) {

                        int indexOfSegment = copy.size() - 1;
                        System.out.println("indexOfSegment: " + indexOfSegment);
//                        System.out.println("copy.get(indexOfSegment): " + copy.get(indexOfSegment));
//                        System.out.println("copy.get(indexOfSegment) matches: " + copy.get(indexOfSegment));
                        if (copy.get(indexOfSegment).getLiteral().matches("[Cc]")) {

                            t = BigDecimal.valueOf(t).setScale(this.precision, RoundingMode.HALF_UP).doubleValue();
                            System.out.println("Parameter is " + t);
                            System.out.println("Index of segments is " + indexOfSegment);


                            if (indexOfSegment != 0 && t != 0) {
                                List<Segment> lastTwoSegments = List.of(copy.get(indexOfSegment - 1), copy.get(indexOfSegment));
                                System.out.println("Last two segments: " + lastTwoSegments);

                                Point p02_2 = lastTwoSegments.get(1).getPoints().get(0).getAdditionalPoint(t, lastTwoSegments.get(1).getPoints().get(1));
                                Point p11 = lastTwoSegments.get(1).getPoints().get(1).getAdditionalPoint(t, lastTwoSegments.get(1).getPoints().get(2));
                                Point p02_3 = lastTwoSegments.get(0).getPoints().get(1).getCurvePoint(t, p11);

                                System.out.println(p02_2.isDifferenceSmaller(p02_3, error));
                                System.out.println(lastTwoSegments.get(1).getPoints().get(2).isDifferenceSmaller(p02_2, error));
                                System.out.println(lastTwoSegments.get(1).getPoints().get(2).isDifferenceSmaller(p02_3, error));

                                if (
                                        p02_2.isDifferenceSmaller(p02_3, error) ||
                                                lastTwoSegments.get(1).getPoints().get(2).isDifferenceSmaller(p02_2, error) ||
                                                lastTwoSegments.get(1).getPoints().get(2).isDifferenceSmaller(p02_3, error)
                                ) {
                                    System.out.println("Got '1'");
                                    output.append("1");
                                    Segment merged = lastTwoSegments.get(0).mergeSegments(t, lastTwoSegments.get(1));
                                    System.out.println("merged segment: " + merged);
                                    copy.remove(indexOfSegment);
                                    copy.set(indexOfSegment - 1, merged);

                                    System.out.println("Copy looks like: " + copy);
                                    indexOfSegment--;
                                } else {
                                    System.out.println("Got '0', because condition of points is not met");
                                    output.append("0");
                                }
                                System.out.println();
                            } else {
                                System.out.println("Output length: " + output.length());
                                if (output.length() < this.maxBitAmount) {
                                    output.append("0");
                                }
                            }
                        } else {
                            copy.remove(copy.size() - 1);
                        }
                    }
//                    System.out.println("Output: " + output.reverse());

                    if (output.length() < this.maxBitAmount) {
                        while (output.length() < this.maxBitAmount) {
                            output.append("0");
                        }
                    }
                    output.reverse();
                    decodedBinaryBlocks.add(output.toString());
                }
                indexOfFlag++;
            }
        }

        System.out.println("Decoded binary blocks: ");
        System.out.println(decodedBinaryBlocks);
        Message decodedMessage = new Message();
        decodedMessage.setText(decodedBinaryBlocks);
        decodedMessage.setBinaryMessage(decodedBinaryBlocks);
        return decodedMessage;
    }
}