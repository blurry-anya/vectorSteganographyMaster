package com.diploma.stegovector.controllers;

import com.diploma.stegovector.Application;
import com.diploma.stegovector.methods.BitMethod;
import com.diploma.stegovector.methods.PatternMethod;
import com.diploma.stegovector.objects.Image;
import com.diploma.stegovector.objects.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;


public class ApplicationController implements Initializable {

    @FXML
    private WebView webView;

    @FXML
    private AnchorPane stageElement;

    @FXML
    private Label fileName, fileSize, totalNumOfPaths, amountOfExtractedBezier, amountOfAvailableBezier, recommendedInputSize;

    @FXML
    private ChoiceBox<Double> bitParamSteps, patternStartSteps, errors;

    @FXML
    private ChoiceBox<Integer> precisions, maxBitAmounts, patternLengths;

    @FXML
    private RadioButton bitMode, patternMode;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressBarLabel, progressBarActionName;

    @FXML
    private Label timeValue;

    @FXML
    private TextArea textArea;

    @FXML
    private Label encodedFlag1, encodedFlag2;

    private final ObservableList<Double> bitParametersValues = FXCollections.observableArrayList(0.001, 0.0005, 0.0006);
    private final ObservableList<Double> patternStartStepsValues = FXCollections.observableArrayList(0.001, 0.0005, 0.0006);
    private final ObservableList<Double> errorsValues = FXCollections.observableArrayList(0.00002, 0.00004, 0.00005);
    private final ObservableList<Integer> maxBitAmountsValues = FXCollections.observableArrayList(8, 16, 40, 64, 80);
    private final ObservableList<Integer> precisionsValues = FXCollections.observableArrayList(4, 5, 6);
    private final ObservableList<Integer> patternLengthsValues = FXCollections.observableArrayList(4, 8, 16, 40);

    private File svgImage, txtMessage;

    private Image loadedImage;

    private List<String> modifiedCurves;

    private double bitParameter, patternStartStep, error;
    private int maxBitAmount, precision, patternLength;

    private BigDecimal progress = new BigDecimal(String.format("%.2f", 0.0).replace(",", "."));

    private String finalParametersOfEncoding = "";

    private void setLoadedImage(Image image){
        this.loadedImage = image;
    }

    private Image getLoadedImage(){
        return loadedImage;
    }

    private void setModifiedCurves(List<String> modifiedCurves){
        this.modifiedCurves = modifiedCurves;
    }

    private List<String> getModifiedCurves(){
        return modifiedCurves;
    }

    private void clearModifiedCurves(){
        this.modifiedCurves = new ArrayList<>();
    }

    // image upload and download
    @FXML
    protected File onUploadBtnClick() throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
        progressBarActionName.setStyle("-fx-text-fill:  #463F3A");
        progressBarActionName.setText("Uploading image...");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open a SVG file");

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SVG files (*.svg)", "*.svg");
        fileChooser.getExtensionFilters().add(extFilter);


        Stage stage = (Stage) stageElement.getScene().getWindow();
        increaseProgress(0.1);
        svgImage = fileChooser.showOpenDialog(stage);
        increaseProgress(0.1);
        System.out.println("image: " + svgImage);

        if (svgImage != null) {
            WebEngine webEngine = webView.getEngine();
            URL url = svgImage.toURI().toURL();
            webEngine.load(url.toString());
            increaseProgress(0.2);

            getPrecision();

            loadedImage = new Image();
            loadedImage.setPrecision(precision);
            increaseProgress(0.1);
            loadedImage.getVectorImageContent(svgImage);
            increaseProgress(0.4);
            fileName.setText(svgImage.getName());
            totalNumOfPaths.setText(String.valueOf(loadedImage.getAmountOfCurves()));
            amountOfExtractedBezier.setText(String.valueOf(loadedImage.getAmountOfSegments()));
            amountOfAvailableBezier.setText(String.valueOf(loadedImage.getAmountOfValidSegments()));
            getMaxBitAmount();
            recommendedInputSize.setText(String.valueOf(loadedImage.getAmountOfValidSegments() * maxBitAmount));

            long bytes = svgImage.length();
//            long kilobytes = svgImage.length() / 1024;

            fileSize.setText(String.format("%,d bytes", bytes));
//            fileSize.setText(String.format("%,d KB", kilobytes));

            if (!Objects.equals(loadedImage.getAdditionalData(), "")) {
                precisions.setDisable(true);
                maxBitAmounts.setDisable(true);
                patternLengths.setDisable(true);
                patternStartSteps.setDisable(true);
                bitParamSteps.setDisable(true);
                encodedFlag1.setVisible(true);
                encodedFlag2.setVisible(true);

            } else {
                precisions.setDisable(false);
                maxBitAmounts.setDisable(false);
                patternLengths.setDisable(false);
                patternStartSteps.setDisable(false);
                bitParamSteps.setDisable(false);
                encodedFlag1.setVisible(false);
                encodedFlag2.setVisible(false);
            }

            increaseProgress(0.1);
            progressBarActionName.setText("Image is uploaded.");
            progress = new BigDecimal(String.format("%.2f", 0.0).replace(",", "."));
        } else {
            System.out.println("ERROR: load a valid file!");
            progressBarActionName.setText("Upload file first!");
            progressBarActionName.setStyle("-fx-text-fill: #8f1c1c");
            increaseProgress(-1);
        }
        return svgImage;
    }

    public void onDownloadBtnClick(MouseEvent mouseEvent) {
        System.out.println("Clicked on image download btn");
        progressBarActionName.setStyle("-fx-text-fill:  #463F3A");
        progressBarActionName.setText("Downloading image...");
        List<String> toDownload = getModifiedCurves();
        if (toDownload != null) {
            Stage stage = (Stage) stageElement.getScene().getWindow();
            loadedImage.setModifiedContent(svgImage, toDownload, finalParametersOfEncoding, stage);
            progressBarActionName.setText("Image is downloaded.");
            progress = new BigDecimal(String.format("%.2f", 0.0).replace(",", "."));
        } else {
            progressBarActionName.setText("Image was not encoded yet.");
            progressBarActionName.setStyle("-fx-text-fill: #8f1c1c");
            progressBarLabel.setText("");
        }
        clearModifiedCurves();
    }


    // message upload and download
    public void onUploadTextBtnClick(MouseEvent mouseEvent) throws IOException {
        System.out.println("Clicked on text upload btn");
        progressBarActionName.setStyle("-fx-text-fill:  #463F3A");
        progressBarActionName.setText("Uploading text...");
        increaseProgress(0.1);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open a TXT file with your message");

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);


        Stage stage = (Stage) stageElement.getScene().getWindow();

        txtMessage = fileChooser.showOpenDialog(stage);
        increaseProgress(0.1);
        if (txtMessage != null) {

            BufferedReader br = new BufferedReader(new FileReader(txtMessage));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                increaseProgress(0.2);
                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                String everything = sb.toString();
                increaseProgress(0.2);

                textArea.setText(everything);
            } finally {
                br.close();
            }

            increaseProgress(0.4);
            progressBarActionName.setText("Message is uploaded.");
            progress = new BigDecimal(String.format("%.2f", 0.0).replace(",", "."));

        } else {
            System.out.println("ERROR: load a valid file!");
            progressBarActionName.setText("Upload file first!");
            progressBarActionName.setStyle("-fx-text-fill: #8f1c1c");
            increaseProgress(-1);
        }
    }

    public void onDownloadTextBtnClick(MouseEvent mouseEvent) throws FileNotFoundException {
        System.out.println("Clicked on text download btn");
        progressBarActionName.setStyle("-fx-text-fill:  #463F3A");
        progressBarActionName.setText("Downloading text...");
        if (!Objects.equals(textArea.getText(), "")) {
            increaseProgress(0.1);
            FileChooser fileChooser = new FileChooser();
            //Set extension filter for text files
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);
            Stage stage = (Stage) stageElement.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            increaseProgress(0.3);

            PrintWriter writer = new PrintWriter(file);
            writer.println(textArea.getText());
            increaseProgress(0.6);
            writer.close();

            progressBarActionName.setText("Text is downloaded.");
            progress = new BigDecimal(String.format("%.2f", 0.0).replace(",", "."));
        } else {
            progressBarActionName.setText("There is nothing to download");
            progressBarActionName.setStyle("-fx-text-fill: #8f1c1c");
            progressBarLabel.setText("");
        }
    }

    // stego transformations
    public void onEncodeBtnClick(MouseEvent mouseEvent) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        System.out.println("Clicked on encode btn");

        if (getLoadedImage() != null){
            Image image = getLoadedImage().createClone();
//            image.setPrecisionToEveryPoint(this.precision);

            progressBarActionName.setText("Encoding image...");
            progressBarActionName.setStyle("-fx-text-fill:  #463F3A");


            String textFromTextArea = textArea.getText();

            Message message = new Message(textFromTextArea);

            if (!Objects.equals(message.getText(), "")) {
                getMaxBitAmount();
                getPrecision();

                image.setPrecision(this.precision);
                long startTime = System.nanoTime();

                if (bitMode.isSelected()) {

                    getBitParameterStep();

                    System.out.println("Parameters of bit encoding: ");
                    System.out.println("\tbit parameter: " + bitParameter);
                    System.out.println("\tprecision: " + precision);
                    System.out.println("\tmaxBitAmount: " + maxBitAmount);

                    BitMethod bitEncode = new BitMethod(bitParameter, precision, maxBitAmount, image);

                    finalParametersOfEncoding = bitEncode.parseParametersToSvgElement();

                    setModifiedCurves(bitEncode.encode(message));
                } else if (patternMode.isSelected()) {
                    getPatternLength();
                    getPatternStartStep();

                    PatternMethod patternEncode = new PatternMethod(patternStartStep, patternLength, precision, maxBitAmount, image);

                    modifiedCurves = patternEncode.encode(message);

                    finalParametersOfEncoding = patternEncode.parseStegokeyToSvgElement(patternEncode.getFinalParameters());

                    System.out.println("Parameters of pattern encoding: ");
                    System.out.println("\tpattern length: " + patternLength);
                    System.out.println("\tstart step: " + patternStartStep);
                    System.out.println("\tprecision: " + precision);
                    System.out.println("\tmaxBitAmount: " + maxBitAmount);
                }


                System.out.println("Encoded image: " + modifiedCurves);

                long endTime = System.nanoTime();

                long duration = (endTime - startTime) / 1000000;

                timeValue.setText(String.valueOf(duration) + " ms");

                progressBarActionName.setText("Image is encoded.");
                increaseProgress(1);
                progress = new BigDecimal(String.format("%.2f", 0.0).replace(",", "."));
            } else {
                progressBarActionName.setText("There is no message to encode!");
                progressBarActionName.setStyle("-fx-text-fill: #8f1c1c");
                increaseProgress(-1);
            }
        } else {
            progressBarActionName.setText("Download image first!");
            progressBarActionName.setStyle("-fx-text-fill: #8f1c1c");
            increaseProgress(-1);
        }
        textArea.setText("");
    }

    public void onDecodeBtnClick(MouseEvent mouseEvent) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        textArea.setText("");
        System.out.println("Clicked on decode btn");
        if (loadedImage != null) {
            progressBarActionName.setStyle("-fx-text-fill:  #463F3A");
            progressBarActionName.setText("Decoding image...");
            long startTime = System.nanoTime();

            loadedImage = new Image();
            loadedImage.setPrecision(this.precision);
            loadedImage.getVectorImageContent(svgImage);

            getMaxBitAmount();
            getPrecision();
            getError();

            if (bitMode.isSelected()) {
                getBitParameterStep();

                BitMethod bitDecode = new BitMethod(loadedImage);
                bitDecode.getParametersFromSvgElement(loadedImage.getAdditionalData());

                bitParamSteps.setValue(bitDecode.getParameter());
                precisions.setValue(bitDecode.getPrecision());
                maxBitAmounts.setValue(bitDecode.getMaxBitAmount());

                Message decodedMessage = bitDecode.decode(error);

                System.out.println("\t\tDecoded message:");
                System.out.println(decodedMessage.getText());

                textArea.setText(decodedMessage.getText());

            } else if (patternMode.isSelected()) {
                getPatternLength();
                getPatternStartStep();

                PatternMethod patternDecode = new PatternMethod(loadedImage);
                // extracted parameters from hidden link and set them
                patternDecode.setFinalParameters(loadedImage.getAdditionalData());
                // set values of extracted parameters
                patternStartSteps.setValue(patternDecode.getStartStep());
                patternLengths.setValue(patternDecode.getPatternLength());
                patternStartSteps.setValue(patternDecode.getStartStep());
                precisions.setValue(patternDecode.getPrecision());
                maxBitAmounts.setValue(patternDecode.getMaxBitAmount());

                Message decodedMessage = patternDecode.decode(error);

                System.out.println("\t\tDecoded message:");
                System.out.println(decodedMessage.getText());
                textArea.setText(decodedMessage.getText());
            }

            long endTime = System.nanoTime();

            long duration = (endTime - startTime) / 1000000;

            timeValue.setText(String.valueOf(duration) + " ms");

            progressBarActionName.setText("Image is decoded.");
            progress = new BigDecimal(String.format("%.2f", 0.0).replace(",", "."));
        } else {
            progressBarActionName.setText("Download image first!");
            progressBarActionName.setStyle("-fx-text-fill: #8f1c1c");
            increaseProgress(-1);
        }

    }

    public void increaseProgress(double value) {
//        System.out.println("Value in increaseProgress is "+progress.doubleValue());
        if (value == -1.0) {
            System.out.println("INCREASE VALUE IS -1");
            progress = new BigDecimal(String.format("%.2f", 0.0).replace(",", "."));
            progressBar.setProgress(-0.1);
            progressBarLabel.setText("");
        } else {
            if (progress.doubleValue() < 1 && progress.doubleValue() >= 0) {
                progress = new BigDecimal(String.format("%.2f", progress.doubleValue() + value).replace(",", "."));
                progressBar.setProgress(progress.doubleValue());
                progressBarLabel.setText(Integer.toString((int) Math.round(progress.doubleValue() * 100)) + "%");
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        bitParamSteps.setValue(0.0005);

        patternLengths.setValue(4);
        patternStartSteps.setValue(0.0005);

        precisions.setValue(5);
        errors.setValue(0.00004);
        maxBitAmounts.setValue(8);


        bitParamSteps.setItems(bitParametersValues);
        patternStartSteps.setItems(patternStartStepsValues);
        patternLengths.setItems(patternLengthsValues);
        precisions.setItems(precisionsValues);
        errors.setItems(errorsValues);
        maxBitAmounts.setItems(maxBitAmountsValues);


        bitParamSteps.setOnAction(this::getBitParameterStep);
        patternStartSteps.setOnAction(this::getPatternStartStep);
        patternLengths.setOnAction(this::getPatternLength);
        precisions.setOnAction(this::getPrecision);
        errors.setOnAction(this::getError);
        maxBitAmounts.setOnAction(this::getMaxBitAmount);

    }

    private void getBitParameterStep(ActionEvent actionEvent) {
        bitParameter = bitParamSteps.getValue();
        System.out.println("Value of encode bit parameter is: " + bitParameter);
    }

    private void getMaxBitAmount(ActionEvent actionEvent) {
        maxBitAmount = maxBitAmounts.getValue();
        if (loadedImage != null) {
            recommendedInputSize.setText(String.valueOf(loadedImage.getAmountOfValidSegments() * maxBitAmount));
        }
        System.out.println("Value of encode bit max amount is: " + maxBitAmount);
    }

    private void getPrecision(ActionEvent actionEvent) {
        precision = precisions.getValue();
        System.out.println("Value of encode bit precision is: " + precision);
    }

    private void getPatternStartStep(ActionEvent actionEvent) {
        patternStartStep = patternStartSteps.getValue();
        System.out.println("Value of encode bit parameter is: " + bitParameter);
    }

    private void getError(ActionEvent actionEvent) {
        error = errors.getValue();
        System.out.println("Value of encode bit max amount is: " + maxBitAmount);
    }

    private void getPatternLength(ActionEvent actionEvent) {
        patternLength = patternLengths.getValue();
        System.out.println("Value of encode bit precision is: " + precision);
    }


    private void getBitParameterStep() {
        bitParameter = bitParamSteps.getValue();
        System.out.println("Value of encode bit parameter is: " + bitParameter);
    }

    private void getMaxBitAmount() {
        maxBitAmount = maxBitAmounts.getValue();
        System.out.println("Value of encode bit max amount is: " + maxBitAmount);
    }

    private void getPrecision() {
        precision = precisions.getValue();
        System.out.println("Value of encode bit precision is: " + precision);
    }

    private void getPatternStartStep() {
        patternStartStep = patternStartSteps.getValue();
        System.out.println("Value of encode bit parameter is: " + bitParameter);
    }

    private void getError() {
        error = errors.getValue();
        System.out.println("Value of encode bit max amount is: " + maxBitAmount);
    }

    private void getPatternLength() {
        patternLength = patternLengths.getValue();
        System.out.println("Value of encode bit precision is: " + precision);
    }

    public void onExperimentsBtnClick(MouseEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(Objects.requireNonNull(Application.class.getResource("experimentsLayout.fxml")));

        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setTitle("Experiments");
        stage.setMinWidth(800);
        stage.setMinHeight(800);
        stage.setMaxWidth(1500);
        stage.setMaxHeight(1000);
        stage.setScene(scene);

        stage.initModality(Modality.NONE);
        stage.show();

    }
}
