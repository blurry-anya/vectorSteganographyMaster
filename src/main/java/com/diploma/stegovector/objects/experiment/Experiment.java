package com.diploma.stegovector.objects.experiment;

import com.diploma.stegovector.methods.BitMethod;
import com.diploma.stegovector.methods.PatternMethod;
import com.diploma.stegovector.objects.Curve;
import com.diploma.stegovector.objects.Image;
import com.diploma.stegovector.objects.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Experiment {
    private String transformationType;
    private String method;
    private Image container;
    private double bitParameter;
    private int maxBitAmount;
    private int precision;
    private double error;
    private double startStep;
    private int patternLength;
    private String experimentMessage;
    private List<Long> timeMarks = new ArrayList<Long>();

    private List<Double> percentage;

    public List<Long> getTimeMarks() {
        return timeMarks;
    }


    public Experiment(String method, String transformationType, Image container, double bitParameter, int maxBitAmount, int precision, double error, double startStep, int patternLength, String experimentMessage) {
        this.transformationType = transformationType;
        this.method = method;
        this.container = container;
        this.bitParameter = bitParameter;
        this.maxBitAmount = maxBitAmount;
        this.precision = precision;
        this.error = error;
        this.startStep = startStep;
        this.patternLength = patternLength;
        this.experimentMessage = experimentMessage;
    }

    public List<Double> getPercentage() {
        return percentage;
    }


    public void executeExperiment() {
        System.out.println("\t|--------------------------------------------------------------|");
        System.out.println("\t|---------------------Starting experiment...-------------------|");
        System.out.println("\t|--------------------------------------------------------------|");
        System.out.println("this.bitParameter " + this.bitParameter);
        System.out.println("this.maxBitAmount " + this.maxBitAmount);
        System.out.println("this.precision " + this.precision);

        System.out.println("Image to be experimented on:");
        System.out.println(this.container);
        List<Image> images = new ArrayList<>();
        List<String> encoded = new ArrayList<>();

        Message originalMessage = new Message(this.experimentMessage, this.maxBitAmount);
        List<List<Double>> finalParameters = new ArrayList<>();
        long startTimeEncode = System.nanoTime();
        switch (method) {
            case "bit":
                BitMethod bitEncode = new BitMethod(this.bitParameter, this.precision, this.maxBitAmount, this.container);
                encoded = bitEncode.encode(originalMessage);
                break;
            case "pattern":
                PatternMethod patternEncode = new PatternMethod(this.startStep, this.patternLength, this.precision, this.maxBitAmount, this.container);
                encoded = patternEncode.encode(originalMessage);
                finalParameters = patternEncode.getFinalParameters();
                break;
        }
        long endTimeEncode = System.nanoTime();

        this.timeMarks.add(((endTimeEncode - startTimeEncode) / 1000000));


        String[] encodedArray = new String[encoded.size()];
        for (int i = 0; i < encoded.size(); i++) {
            encodedArray[i] = encoded.get(i);
        }

        Image encodedImage = new Image();
        List<List<Curve>> parsedBack = new ArrayList<>();
        parsedBack = encodedImage.parsePaths(encodedArray);

        encodedImage.setCurves(parsedBack);
        images.add(encodedImage);


        List<Message> allDecodedMessages = new ArrayList<>();

        double a1 = Math.cos(1), a2 = -Math.sin(1), a3 = Math.sin(1);

        List<Long> decodeTime = new ArrayList<>();
        switch (transformationType) {
            case "translation":
                System.out.println("\t\tTRANSLATION");
                for (int i = -250, index = 1; i <= 250; i += 50) {
                    if (i != 0) {
                        AffineTransformation translation = new AffineTransformation(images.get(index - 1), 1, 0, 0, 1, i, i, 0, 0, 0, 0, 0, 0);
                        translation.setPrecision(this.precision);
                        translation.applyAffineTransformation();
                        Image newImage = translation.getContainer().createClone();
                        images.add(newImage);
                        Message decodedMessage = new Message();

                        long startTimeDecode = System.nanoTime();
                        switch (method) {
                            case "bit":
                                BitMethod bitDecode = new BitMethod(this.bitParameter, this.precision, this.maxBitAmount, translation.getContainer());
                                decodedMessage = bitDecode.decode(this.error);
                                break;
                            case "pattern":
                                PatternMethod patternDecode = new PatternMethod(this.startStep, this.patternLength, this.precision, this.maxBitAmount, translation.getContainer());
                                patternDecode.setFinalParameters(finalParameters);
                                decodedMessage = patternDecode.decode(this.error);
                                break;
                        }
                        long endTimeDecode = System.nanoTime();

                        decodeTime.add((endTimeDecode-startTimeDecode)/1000000);
                        allDecodedMessages.add(decodedMessage);
                        index++;
                    }
                }
                break;

            case "rotation":
                System.out.println("\t\tROTATION");
                System.out.println("a: " + a1 + " a2:" + a2 + " a3: " + a3);
                for (int i = 1, index = 1; i <= 10; i++) {
                    AffineTransformation rotation = new AffineTransformation(images.get(index - 1), a1, a2, a3, a1, 0, 0, 0, 0, 0, 0, 0, 0);
                    rotation.setPrecision(this.precision);
                    rotation.applyAffineTransformation();
                    Image newImage = rotation.getContainer().createClone();
                    images.add(newImage);
                    Message decodedMessage = new Message();

                    long startTimeDecode = System.nanoTime();
                    switch (method) {
                        case "bit":
                            BitMethod bitDecode = new BitMethod(this.bitParameter, this.precision, this.maxBitAmount, rotation.getContainer());
                            decodedMessage = bitDecode.decode(this.error);
                            break;
                        case "pattern":
                            PatternMethod patternDecode = new PatternMethod(this.startStep, this.patternLength, this.precision, this.maxBitAmount, rotation.getContainer());
                            patternDecode.setFinalParameters(finalParameters);
                            decodedMessage = patternDecode.decode(this.error);
                            break;
                    }
                    long endTimeDecode = System.nanoTime();

                    decodeTime.add((endTimeDecode-startTimeDecode)/1000000);
                    allDecodedMessages.add(decodedMessage);
                    index++;
                    System.out.println();
                }

                break;
            case "shearX":
                System.out.println("\t\tSHEAR X");
                for (int i = 1, index = 1; i <= 10; i++) {
                    System.out.println("\t\t Number of loop: " + i);

                    AffineTransformation shearX = new AffineTransformation(images.get(index - 1), 1, 0.01, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);
                    shearX.setPrecision(this.precision);
                    shearX.applyAffineTransformation();
                    Image newImage = shearX.getContainer().createClone();

                    images.add(newImage);
                    Message decodedMessage = new Message();

                    long startTimeDecode = System.nanoTime();
                    switch (method) {
                        case "bit":
                            BitMethod bitDecode = new BitMethod(this.bitParameter, this.precision, this.maxBitAmount, shearX.getContainer());
                            decodedMessage = bitDecode.decode(this.error);
                            break;
                        case "pattern":
                            PatternMethod patternDecode = new PatternMethod(this.startStep, this.patternLength, this.precision, this.maxBitAmount, shearX.getContainer());
                            patternDecode.setFinalParameters(finalParameters);
                            decodedMessage = patternDecode.decode(this.error);
                            break;
                    }
                    long endTimeDecode = System.nanoTime();

                    decodeTime.add((endTimeDecode-startTimeDecode)/1000000);
                    allDecodedMessages.add(decodedMessage);
                    index++;
                }
                break;
            case "shearY":
                System.out.println("\t\tSHEAR Y");
                for (int i = 1, index = 1; i <= 10; i++) {

                    AffineTransformation shearY = new AffineTransformation(images.get(index - 1), 1, 0, 0.01, 1, 0, 0, 0, 0, 0, 0, 0, 0);
                    shearY.setPrecision(this.precision);
                    shearY.applyAffineTransformation();
                    Image newImage = shearY.getContainer().createClone();

                    images.add(newImage);
                    Message decodedMessage = new Message();

                    long startTimeDecode = System.nanoTime();
                    switch (method) {
                        case "bit":
                            BitMethod bitDecode = new BitMethod(this.bitParameter, this.precision, this.maxBitAmount, shearY.getContainer());
                            decodedMessage = bitDecode.decode(this.error);
                            break;
                        case "pattern":
                            PatternMethod patternDecode = new PatternMethod(this.startStep, this.patternLength, this.precision, this.maxBitAmount, shearY.getContainer());
                            patternDecode.setFinalParameters(finalParameters);
                            decodedMessage = patternDecode.decode(this.error);
                            break;
                    }
                    long endTimeDecode = System.nanoTime();

                    decodeTime.add((endTimeDecode-startTimeDecode)/1000000);
                    allDecodedMessages.add(decodedMessage);
                    index++;
                }
                break;
            case "proportionalScaleForCompression":
                System.out.println("\t\t PROPORTIONAL SCALE FOR COMPRESSION");
                for (int i = 1, index = 1; i <= 10; i++) {
                    AffineTransformation scaleForCompr = new AffineTransformation(images.get(index - 1), 0.99, 0, 0, 0.99, 0, 0, 0, 0, 0, 0, 0, 0);
                    scaleForCompr.setPrecision(this.precision);
                    scaleForCompr.applyAffineTransformation();
                    Image newImage = scaleForCompr.getContainer().createClone();

                    images.add(newImage);
                    Message decodedMessage = new Message();

                    long startTimeDecode = System.nanoTime();
                    switch (method) {
                        case "bit":
                            BitMethod bitDecode = new BitMethod(this.bitParameter, this.precision, this.maxBitAmount, scaleForCompr.getContainer());
                            decodedMessage = bitDecode.decode(this.error);
                            break;
                        case "pattern":
                            PatternMethod patternDecode = new PatternMethod(this.startStep, this.patternLength, this.precision, this.maxBitAmount, scaleForCompr.getContainer());
                            patternDecode.setFinalParameters(finalParameters);
                            decodedMessage = patternDecode.decode(this.error);
                            break;
                    }
                    long endTimeDecode = System.nanoTime();

                    decodeTime.add((endTimeDecode-startTimeDecode)/1000000);
                    allDecodedMessages.add(decodedMessage);
                    index++;
                }
                break;
            case "proportionalScaleForExtension":
                System.out.println("\t\t PROPORTIONAL SCALE FOR EXTENSION");
                for (int i = 1, index = 1; i <= 10; i++) {
                    AffineTransformation scaleForExt = new AffineTransformation(images.get(index - 1), 1.1, 0, 0, 1.1, 0, 0, 0, 0, 0, 0, 0, 0);
                    scaleForExt.setPrecision(this.precision);
                    scaleForExt.applyAffineTransformation();
                    Image newImage = scaleForExt.getContainer().createClone();

                    images.add(newImage);
                    Message decodedMessage = new Message();

                    long startTimeDecode = System.nanoTime();
                    switch (method) {
                        case "bit":
                            BitMethod bitDecode = new BitMethod(this.bitParameter, this.precision, this.maxBitAmount, scaleForExt.getContainer());
                            decodedMessage = bitDecode.decode(error);
                            break;
                        case "pattern":
                            PatternMethod patternDecode = new PatternMethod(this.startStep, this.patternLength, this.precision, this.maxBitAmount, scaleForExt.getContainer());
                            patternDecode.setFinalParameters(finalParameters);
                            decodedMessage = patternDecode.decode(this.error);
                            break;
                    }
                    long endTimeDecode = System.nanoTime();

                    decodeTime.add((endTimeDecode-startTimeDecode)/1000000);
                    allDecodedMessages.add(decodedMessage);
                    index++;
                }
                break;
            case "almostAffineTransformation":
                System.out.println("\t\t ALMOST AFFINE TRANSFORMATION");
                for (int i = 1, index = 1; i <= 10; i++) {
                    AffineTransformation almostAffine = new AffineTransformation(images.get(index - 1), a1, a2, a3, -a1, 0, 0, 0, 0, 0, 0, 0, 0);
                    almostAffine.setPrecision(this.precision);
                    almostAffine.applyAffineTransformation();
                    Image newImage = almostAffine.getContainer().createClone();
                    images.add(newImage);
                    Message decodedMessage = new Message();

                    long startTimeDecode = System.nanoTime();
                    switch (method) {
                        case "bit":
                            BitMethod bitDecode = new BitMethod(this.bitParameter, this.precision, this.maxBitAmount, almostAffine.getContainer());
                            decodedMessage = bitDecode.decode(this.error);
                            break;
                        case "pattern":
                            PatternMethod patternDecode = new PatternMethod(this.startStep, this.patternLength, this.precision, this.maxBitAmount, almostAffine.getContainer());
                            patternDecode.setFinalParameters(finalParameters);
                            decodedMessage = patternDecode.decode(this.error);
                            break;
                    }
                    long endTimeDecode = System.nanoTime();

                    decodeTime.add((endTimeDecode-startTimeDecode)/1000000);
                    allDecodedMessages.add(decodedMessage);
                    index++;
                    System.out.println();

                }
                break;
        }

        int originalMessageBitsSize = originalMessage.getText().length() * 8;
        double realMessageSize = this.container.getAmountOfValidSegments() * maxBitAmount;

        List<Double> percentOfIncorrectBits = new ArrayList<>();
        System.out.println("All decoded messages: ");
        for (Message message : allDecodedMessages) {
            System.out.println(message.getBinaryMessage());
            double wrongBits = getAmountOfIncorrectBits(String.join("", message.getBinaryMessage()), String.join("", originalMessage.getBinaryMessage()));
            // originalMessageBitsSize - 100
            // wrongBits - x
            // originalMessageBitsSize * x = wrongBits * 100
            // x = (wrongBits * 100) / originalMessageBitsSize
            percentOfIncorrectBits.add(((wrongBits * 100) / realMessageSize));
        }
        this.percentage = percentOfIncorrectBits;
        System.out.println("Current percents: ");
        System.out.println(percentOfIncorrectBits);

        long decodeTimeSum = 0;
        for (int i = 0; i < decodeTime.size(); i++) {
            decodeTimeSum += decodeTime.get(i);
        }
        this.timeMarks.add(decodeTimeSum/10);
    }


    public double getAmountOfIncorrectBits(String decodedMessage, String originalMessage) {
        String[] decodedSplit = decodedMessage.split("");
        String[] originalSplit = originalMessage.split("");
        int wrongBits = 0;
        for (int i = 0; i < decodedSplit.length; i++) {
            if (!Objects.equals(originalSplit[i], decodedSplit[i])) {
                wrongBits++;
            }
        }
        return wrongBits;
    }


}
