package com.diploma.stegovector.controllers;

import com.diploma.stegovector.objects.Image;
import com.diploma.stegovector.objects.experiment.Experiment;
import com.diploma.stegovector.objects.experiment.ParametersSet;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ExperimentsController implements Initializable {

    @FXML
    GridPane gridRotation, gridTranslation, gridShearX, gridShearY, gridScaleCompression, gridScaleExtension, gridAlmostAffine, gridSpeedValues;

    @FXML
    Label statusLabelRotation, statusLabelTranslation, statusLabelShearX, statusLabelShearY, statusLabelScalex, statusLabelScaleX, statusLabelAlmostAffine;

    @FXML
    Label timeTableTranslation, timeTableRotation, timeTableShearX, timeTableShearY, timeTableScalex, timeTableScaleX, timeTableAlmostAffine;

    @FXML
    Label speedTableUpdateLabel, uploadImageLabel;

    @FXML
    RadioButton bitMethodRotation, patternMethodRotation,
            bitMethodTranslation, patternMethodTranslation,
            bitMethodShearX, patternMethodShearX,
            bitMethodShearY, patternMethodShearY,
            bitMethodScalex, patternMethodScalex,
            bitMethodScaleX, patternMethodScaleX,
            bitMethodAlmostAffine, patternMethodAlmostAffine;

    @FXML
    private AnchorPane stageElement;

    String experimentalMessage = "Steganography is the technique of hiding secret data within an ordinary, non-secret, file or message in order to avoid detection. Steganography is the technique of hiding secret data within an ordinary, non-secret, file or message in order to avoid detection. Steganography is the hiding technique...";

    Image expImage;

    List<String> parametersSet = List.of("set 1", "set 2", "set 3", "set 4", "set 5", "set 6", "set 7", "set 8", "set 9", "set 10", "set 11", "set 12");
    List<String> loopsNames = List.of("loop 1", "loop 2", "loop 3", "loop 4", "loop 5", "loop 6", "loop 7", "loop 8", "loop 9", "loop 10");

//    String imagePath = "./src/main/resources/com/diploma/stegovector/svgImages/exp_lamp.svg";

//    File experimentalFile = new File(imagePath);

    File experimentalFile;

    List<ParametersSet> parametersSets = List.of(
            new ParametersSet(40, 5, 0.00002),
            new ParametersSet(40, 5, 0.00004),
            new ParametersSet(40, 6, 0.00002),
            new ParametersSet(40, 6, 0.00004),

            new ParametersSet(64, 5, 0.00002),
            new ParametersSet(64, 5, 0.00004),
            new ParametersSet(64, 6, 0.00002),
            new ParametersSet(64, 6, 0.00004),

            new ParametersSet(80, 5, 0.00002),
            new ParametersSet(80, 5, 0.00004),
            new ParametersSet(80, 6, 0.00002),
            new ParametersSet(80, 6, 0.00004)
    );


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        init();
    }

    public void init() {
        preloadGrid(gridTranslation);
        preloadGrid(gridRotation);
        preloadGrid(gridShearX);
        preloadGrid(gridShearY);
        preloadGrid(gridScaleCompression);
        preloadGrid(gridScaleExtension);
        preloadGrid(gridAlmostAffine);
        preloadSpeedGrid(gridSpeedValues);

        timeTableTranslation.setVisible(false);
        timeTableRotation.setVisible(false);
        timeTableShearX.setVisible(false);
        timeTableShearY.setVisible(false);
        timeTableScalex.setVisible(false);
        timeTableScaleX.setVisible(false);
        timeTableAlmostAffine.setVisible(false);
    }

    public void preloadGrid(GridPane gridPane) {
        List<String> preloadData = List.of("The", "values", "will", "appear", "here");

        gridPane.add(new Label(preloadData.get(0)), 3, 6);
        gridPane.add(new Label(preloadData.get(1)), 4, 6);
        gridPane.add(new Label(preloadData.get(2)), 5, 6);
        gridPane.add(new Label(preloadData.get(3)), 6, 6);
        gridPane.add(new Label(preloadData.get(4)), 7, 6);
    }

    public void reinitAlles() {
        clearGrid(gridTranslation);
        clearGrid(gridRotation);
        clearGrid(gridShearX);
        clearGrid(gridShearY);
        clearGrid(gridScaleCompression);
        clearGrid(gridScaleExtension);
        clearGrid(gridAlmostAffine);
        clearGrid(gridSpeedValues);

        init();
    }

    public void uploadExpImage() {
        reinitAlles();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open a SVG file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SVG files (*.svg)", "*.svg");
        fileChooser.getExtensionFilters().add(extFilter);
        Stage stage = (Stage) stageElement.getScene().getWindow();
        experimentalFile = fileChooser.showOpenDialog(stage);
        if (experimentalFile != null) {
            uploadImageLabel.setText("Uploaded: " + experimentalFile.getName());
        } else {
            uploadImageLabel.setText("You need to choose a file.");
        }
    }

    public void runTranslationExperiments(MouseEvent event) {
        if (experimentalFile != null) {
            clearGrid(gridTranslation);
            initGrid(gridTranslation);

            clearGrid(gridSpeedValues);
            initSpeedGrid(gridSpeedValues);

            expImage = new Image();
            expImage.setPrecision(6);
            try {
                expImage.getVectorImageContent(experimentalFile);
            } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }

            List<Image> copiesForExperiment = List.of(expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone());
            List<List<Double>> allPercentage = new ArrayList<>();
            List<List<Long>> time = new ArrayList<>();

            if (bitMethodTranslation.isSelected()) {
                for (int i = 0; i < parametersSets.size(); i++) {
                    Experiment rotation = new Experiment("bit", "translation", copiesForExperiment.get(i), 0.0005, parametersSets.get(i).getMaxBitAmount(), parametersSets.get(i).getPrecision(), parametersSets.get(i).getError(), 0.0005, 4, experimentalMessage);
                    rotation.executeExperiment();
                    List<Double> percentageOfLoss = rotation.getPercentage();
                    allPercentage.add(percentageOfLoss);
                    time.add(rotation.getTimeMarks());
                }
                statusLabelTranslation.setText("Executed with the bit mode.");
                speedTableUpdateLabel.setText("Values were obtained by translation, bit mode.");
            } else {
                for (int i = 0; i < parametersSets.size(); i++) {
                    Experiment rotation = new Experiment("pattern", "translation", copiesForExperiment.get(i), 0.0005, parametersSets.get(i).getMaxBitAmount(), parametersSets.get(i).getPrecision(), parametersSets.get(i).getError(), 0.0005, 4, experimentalMessage);
                    rotation.executeExperiment();
                    List<Double> percentageOfLoss = rotation.getPercentage();
                    allPercentage.add(percentageOfLoss);
                    time.add(rotation.getTimeMarks());
                }
                statusLabelTranslation.setText("Executed with the pattern mode.");
                speedTableUpdateLabel.setText("Values were obtained by translation, pattern mode.");
            }
            timeTableTranslation.setVisible(true);

            for (int i = 1; i <= allPercentage.size(); i++) {
                for (int j = 1; j <= allPercentage.get(i - 1).size(); j++) {
                    gridTranslation.add(new Label(String.format("%.2f", allPercentage.get(i - 1).get(j - 1))), j, i);
                }
            }

            for (int i = 1; i <= 12; i++) {
                gridSpeedValues.add(new Label(time.get(i - 1).get(0) + " ms"), 4, i);
                gridSpeedValues.add(new Label(time.get(i - 1).get(1) + " ms"), 5, i);
            }
            long[] averageTime = getAverageTime(time);
            gridSpeedValues.add(new Label(averageTime[0] + " ms"), 4, 13);
            gridSpeedValues.add(new Label(averageTime[1] + " ms"), 5, 13);
        } else {
            statusLabelTranslation.setText("Load a file first!");
        }
    }


    public void runRotationExperiment() {
        if (experimentalFile != null) {
            clearGrid(gridRotation);
            initGrid(gridRotation);

            clearGrid(gridSpeedValues);
            initSpeedGrid(gridSpeedValues);

            expImage = new Image();
            expImage.setPrecision(6);

            try {
                expImage.getVectorImageContent(experimentalFile);
            } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }

            List<Image> copiesForExperiment = List.of(expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone());
            List<List<Double>> allPercentage = new ArrayList<>();
            List<List<Long>> time = new ArrayList<>();


            if (bitMethodRotation.isSelected()) {
                for (int i = 0; i < parametersSets.size(); i++) {
                    Experiment rotation = new Experiment("bit", "rotation", copiesForExperiment.get(i), 0.0005, parametersSets.get(i).getMaxBitAmount(), parametersSets.get(i).getPrecision(), parametersSets.get(i).getError(), 0.0005, 4, experimentalMessage);
                    rotation.executeExperiment();
                    List<Double> percentageOfLoss = rotation.getPercentage();
                    allPercentage.add(percentageOfLoss);
                    time.add(rotation.getTimeMarks());
                }
                statusLabelRotation.setText("Executed with the bit mode.");
                speedTableUpdateLabel.setText("Values were obtained by rotation, bit mode.");
            } else {
                for (int i = 0; i < parametersSets.size(); i++) {
                    Experiment rotation = new Experiment("pattern", "rotation", copiesForExperiment.get(i), 0.0005, parametersSets.get(i).getMaxBitAmount(), parametersSets.get(i).getPrecision(), parametersSets.get(i).getError(), 0.0005, 4, experimentalMessage);
                    rotation.executeExperiment();
                    List<Double> percentageOfLoss = rotation.getPercentage();
                    allPercentage.add(percentageOfLoss);
                    time.add(rotation.getTimeMarks());
                }
                statusLabelRotation.setText("Executed with the pattern mode.");
                speedTableUpdateLabel.setText("Values were obtained by rotation, pattern mode.");
            }
            timeTableRotation.setVisible(true);

            for (int i = 1; i <= allPercentage.size(); i++) {
                for (int j = 1; j <= allPercentage.get(i - 1).size(); j++) {
                    gridRotation.add(new Label(String.format("%.2f", allPercentage.get(i - 1).get(j - 1))), j, i);
                }
            }
            for (int i = 1; i <= 12; i++) {
                gridSpeedValues.add(new Label(time.get(i - 1).get(0) + " ms"), 4, i);
                gridSpeedValues.add(new Label(time.get(i - 1).get(1) + " ms"), 5, i);
            }
            long[] averageTime = getAverageTime(time);
            gridSpeedValues.add(new Label(averageTime[0] + " ms"), 4, 13);
            gridSpeedValues.add(new Label(averageTime[1] + " ms"), 5, 13);
        } else {
            statusLabelRotation.setText("Load a file first!");
        }
    }

    public void runShearXExperiments(MouseEvent event) {
        if (experimentalFile != null) {
            clearGrid(gridShearX);
            initGrid(gridShearX);
            clearGrid(gridSpeedValues);
            initSpeedGrid(gridSpeedValues);
            expImage = new Image();
            expImage.setPrecision(6);

            try {
                expImage.getVectorImageContent(experimentalFile);
            } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }

            List<Image> copiesForExperiment = List.of(expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone());
            List<List<Double>> allPercentage = new ArrayList<>();
            List<List<Long>> time = new ArrayList<>();

            if (bitMethodShearX.isSelected()) {
                for (int i = 0; i < parametersSets.size(); i++) {
                    Experiment shearX = new Experiment("bit", "shearX", copiesForExperiment.get(i), 0.0005, parametersSets.get(i).getMaxBitAmount(), parametersSets.get(i).getPrecision(), parametersSets.get(i).getError(), 0.0005, 4, experimentalMessage);
                    shearX.executeExperiment();
                    List<Double> percentageOfLoss = shearX.getPercentage();
                    allPercentage.add(percentageOfLoss);
                    time.add(shearX.getTimeMarks());
                }
                statusLabelShearY.setText("Executed with the bit mode.");
                speedTableUpdateLabel.setText("Values were obtained by X-shear, bit mode.");
            } else {
                for (int i = 0; i < parametersSets.size(); i++) {
                    Experiment shearX = new Experiment("pattern", "shearX", copiesForExperiment.get(i), 0.0005, parametersSets.get(i).getMaxBitAmount(), parametersSets.get(i).getPrecision(), parametersSets.get(i).getError(), 0.0005, 4, experimentalMessage);
                    shearX.executeExperiment();
                    List<Double> percentageOfLoss = shearX.getPercentage();
                    allPercentage.add(percentageOfLoss);
                    time.add(shearX.getTimeMarks());
                }
                statusLabelShearY.setText("Executed with the pattern mode.");
                speedTableUpdateLabel.setText("Values were obtained by X-shear, pattern mode.");
            }
            timeTableShearX.setVisible(true);

            for (int i = 1; i <= allPercentage.size(); i++) {
                for (int j = 1; j <= allPercentage.get(i - 1).size(); j++) {
                    gridShearX.add(new Label(String.format("%.2f", allPercentage.get(i - 1).get(j - 1))), j, i);
                }
            }
            for (int i = 1; i <= 12; i++) {
                gridSpeedValues.add(new Label(time.get(i - 1).get(0) + " ms"), 4, i);
                gridSpeedValues.add(new Label(time.get(i - 1).get(1) + " ms"), 5, i);
            }
            long[] averageTime = getAverageTime(time);
            gridSpeedValues.add(new Label(averageTime[0] + " ms"), 4, 13);
            gridSpeedValues.add(new Label(averageTime[1] + " ms"), 5, 13);
        } else {
            statusLabelShearX.setText("Load a file first!");
        }
    }

    public void runShearYExperiments(MouseEvent event) {
        if (experimentalFile != null) {
            clearGrid(gridShearY);
            initGrid(gridShearY);
            clearGrid(gridSpeedValues);
            initSpeedGrid(gridSpeedValues);
            expImage = new Image();
            expImage.setPrecision(6);

            try {
                expImage.getVectorImageContent(experimentalFile);
            } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }

            List<Image> copiesForExperiment = List.of(expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone());
            List<List<Double>> allPercentage = new ArrayList<>();
            List<List<Long>> time = new ArrayList<>();


            if (bitMethodShearX.isSelected()) {
                for (int i = 0; i < parametersSets.size(); i++) {
                    Experiment shearY = new Experiment("bit", "shearY", copiesForExperiment.get(i), 0.0005, parametersSets.get(i).getMaxBitAmount(), parametersSets.get(i).getPrecision(), parametersSets.get(i).getError(), 0.0005, 4, experimentalMessage);
                    shearY.executeExperiment();
                    List<Double> percentageOfLoss = shearY.getPercentage();
                    allPercentage.add(percentageOfLoss);
                    time.add(shearY.getTimeMarks());
                }
                statusLabelShearX.setText("Executed with the bit mode.");
                speedTableUpdateLabel.setText("Values were obtained by Y-shear, bit mode.");
            } else {
                for (int i = 0; i < parametersSets.size(); i++) {
                    Experiment shearY = new Experiment("pattern", "shearY", copiesForExperiment.get(i), 0.0005, parametersSets.get(i).getMaxBitAmount(), parametersSets.get(i).getPrecision(), parametersSets.get(i).getError(), 0.0005, 4, experimentalMessage);
                    shearY.executeExperiment();
                    List<Double> percentageOfLoss = shearY.getPercentage();
                    allPercentage.add(percentageOfLoss);
                    time.add(shearY.getTimeMarks());
                }
                statusLabelShearX.setText("Executed with the pattern mode.");
                speedTableUpdateLabel.setText("Values were obtained by Y-shear, pattern mode.");
            }
            timeTableShearY.setVisible(true);


            for (int i = 1; i <= allPercentage.size(); i++) {
                for (int j = 1; j <= allPercentage.get(i - 1).size(); j++) {
                    gridShearY.add(new Label(String.format("%.2f", allPercentage.get(i - 1).get(j - 1))), j, i);
                }
            }
            for (int i = 1; i <= 12; i++) {
                gridSpeedValues.add(new Label(time.get(i - 1).get(0) + " ms"), 4, i);
                gridSpeedValues.add(new Label(time.get(i - 1).get(1) + " ms"), 5, i);
            }
            long[] averageTime = getAverageTime(time);
            gridSpeedValues.add(new Label(averageTime[0] + " ms"), 4, 13);
            gridSpeedValues.add(new Label(averageTime[1] + " ms"), 5, 13);
        } else {
            statusLabelShearY.setText("Load a file first!");
        }
    }

    public void runScaleCompExperiments(MouseEvent event) {
        if (experimentalFile != null) {
            clearGrid(gridScaleCompression);
            initGrid(gridScaleCompression);
            clearGrid(gridSpeedValues);
            initSpeedGrid(gridSpeedValues);
            expImage = new Image();
            expImage.setPrecision(6);

            try {
                expImage.getVectorImageContent(experimentalFile);
            } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }

            List<Image> copiesForExperiment = List.of(expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone());
            List<List<Double>> allPercentage = new ArrayList<>();
            List<List<Long>> time = new ArrayList<>();

            if (bitMethodScalex.isSelected()) {
                for (int i = 0; i < parametersSets.size(); i++) {
                    Experiment scalex = new Experiment("bit", "proportionalScaleForCompression", copiesForExperiment.get(i), 0.0005, parametersSets.get(i).getMaxBitAmount(), parametersSets.get(i).getPrecision(), parametersSets.get(i).getError(), 0.0005, 4, experimentalMessage);
                    scalex.executeExperiment();
                    List<Double> percentageOfLoss = scalex.getPercentage();
                    allPercentage.add(percentageOfLoss);
                    time.add(scalex.getTimeMarks());
                }
                statusLabelScalex.setText("Executed with the bit mode.");
                speedTableUpdateLabel.setText("Values were obtained by compression, bit mode.");
            } else {
                for (int i = 0; i < parametersSets.size(); i++) {
                    Experiment scalex = new Experiment("pattern", "proportionalScaleForCompression", copiesForExperiment.get(i), 0.0005, parametersSets.get(i).getMaxBitAmount(), parametersSets.get(i).getPrecision(), parametersSets.get(i).getError(), 0.0005, 4, experimentalMessage);
                    scalex.executeExperiment();
                    List<Double> percentageOfLoss = scalex.getPercentage();
                    allPercentage.add(percentageOfLoss);
                    time.add(scalex.getTimeMarks());
                }
                statusLabelScalex.setText("Executed with the pattern mode.");
                speedTableUpdateLabel.setText("Values were obtained by compression, pattern mode.");
            }
            timeTableScalex.setVisible(true);


            for (int i = 1; i <= allPercentage.size(); i++) {
                for (int j = 1; j <= allPercentage.get(i - 1).size(); j++) {
                    gridScaleCompression.add(new Label(String.format("%.2f", allPercentage.get(i - 1).get(j - 1))), j, i);
                }
            }
            for (int i = 1; i <= 12; i++) {
                gridSpeedValues.add(new Label(time.get(i - 1).get(0) + " ms"), 4, i);
                gridSpeedValues.add(new Label(time.get(i - 1).get(1) + " ms"), 5, i);
            }
            long[] averageTime = getAverageTime(time);
            gridSpeedValues.add(new Label(averageTime[0] + " ms"), 4, 13);
            gridSpeedValues.add(new Label(averageTime[1] + " ms"), 5, 13);
        } else {
            statusLabelScalex.setText("Load a file first!");
        }
    }

    public void runScaleExtExperiments(MouseEvent event) {
        if (experimentalFile != null) {
            clearGrid(gridScaleExtension);
            initGrid(gridScaleExtension);
            clearGrid(gridSpeedValues);
            initSpeedGrid(gridSpeedValues);
            expImage = new Image();
            expImage.setPrecision(6);

            try {
                expImage.getVectorImageContent(experimentalFile);
            } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }

            List<Image> copiesForExperiment = List.of(expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone());
            List<List<Double>> allPercentage = new ArrayList<>();
            List<List<Long>> time = new ArrayList<>();

            if (bitMethodScaleX.isSelected()) {
                for (int i = 0; i < parametersSets.size(); i++) {
                    Experiment scaleX = new Experiment("bit", "proportionalScaleForExtension", copiesForExperiment.get(i), 0.0005, parametersSets.get(i).getMaxBitAmount(), parametersSets.get(i).getPrecision(), parametersSets.get(i).getError(), 0.0005, 4, experimentalMessage);
                    scaleX.executeExperiment();
                    List<Double> percentageOfLoss = scaleX.getPercentage();
                    allPercentage.add(percentageOfLoss);
                    time.add(scaleX.getTimeMarks());
                }
                statusLabelScaleX.setText("Executed with the bit mode.");
                speedTableUpdateLabel.setText("Values were obtained by extension, bit mode.");
            } else {
                for (int i = 0; i < parametersSets.size(); i++) {
                    Experiment scaleX = new Experiment("pattern", "proportionalScaleForExtension", copiesForExperiment.get(i), 0.0005, parametersSets.get(i).getMaxBitAmount(), parametersSets.get(i).getPrecision(), parametersSets.get(i).getError(), 0.0005, 4, experimentalMessage);
                    scaleX.executeExperiment();
                    List<Double> percentageOfLoss = scaleX.getPercentage();
                    allPercentage.add(percentageOfLoss);
                    time.add(scaleX.getTimeMarks());
                }
                statusLabelScaleX.setText("Executed with the pattern mode.");
                speedTableUpdateLabel.setText("Values were obtained by extension, pattern mode.");
            }
            timeTableScaleX.setVisible(true);

            for (int i = 1; i <= allPercentage.size(); i++) {
                for (int j = 1; j <= allPercentage.get(i - 1).size(); j++) {
                    gridScaleExtension.add(new Label(String.format("%.2f", allPercentage.get(i - 1).get(j - 1))), j, i);
                }
            }
            for (int i = 1; i <= 12; i++) {
                gridSpeedValues.add(new Label(time.get(i - 1).get(0) + " ms"), 4, i);
                gridSpeedValues.add(new Label(time.get(i - 1).get(1) + " ms"), 5, i);
            }
            long[] averageTime = getAverageTime(time);
            gridSpeedValues.add(new Label(averageTime[0] + " ms"), 4, 13);
            gridSpeedValues.add(new Label(averageTime[1] + " ms"), 5, 13);
        } else {
            statusLabelScaleX.setText("Load a file first!");
        }
    }

    public void runAlmostAffineExperiments(MouseEvent event) {
        if (experimentalFile != null) {
            clearGrid(gridAlmostAffine);
            initGrid(gridAlmostAffine);
            clearGrid(gridSpeedValues);
            initSpeedGrid(gridSpeedValues);
            expImage = new Image();
            expImage.setPrecision(6);

            try {
                expImage.getVectorImageContent(experimentalFile);
            } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }

            List<Image> copiesForExperiment = List.of(expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone(), expImage.createClone());
            List<List<Double>> allPercentage = new ArrayList<>();
            List<List<Long>> time = new ArrayList<>();

            if (bitMethodAlmostAffine.isSelected()) {
                for (int i = 0; i < parametersSets.size(); i++) {
                    Experiment almostAffine = new Experiment("bit", "almostAffineTransformation", copiesForExperiment.get(i), 0.0005, parametersSets.get(i).getMaxBitAmount(), parametersSets.get(i).getPrecision(), parametersSets.get(i).getError(), 0.0005, 4, experimentalMessage);
                    almostAffine.executeExperiment();
                    List<Double> percentageOfLoss = almostAffine.getPercentage();
                    allPercentage.add(percentageOfLoss);
                    time.add(almostAffine.getTimeMarks());
                }
                statusLabelAlmostAffine.setText("Executed with the bit mode.");
                speedTableUpdateLabel.setText("Values were obtained by almost affine, bit mode.");
            } else {
                for (int i = 0; i < parametersSets.size(); i++) {
                    Experiment almostAffine = new Experiment("pattern", "almostAffineTransformation", copiesForExperiment.get(i), 0.0005, parametersSets.get(i).getMaxBitAmount(), parametersSets.get(i).getPrecision(), parametersSets.get(i).getError(), 0.0005, 4, experimentalMessage);
                    almostAffine.executeExperiment();
                    List<Double> percentageOfLoss = almostAffine.getPercentage();
                    allPercentage.add(percentageOfLoss);
                    time.add(almostAffine.getTimeMarks());
                }
                statusLabelAlmostAffine.setText("Executed with the pattern mode.");
                speedTableUpdateLabel.setText("Values were obtained by almost affine, pattern mode.");
            }
            timeTableAlmostAffine.setVisible(true);

            for (int i = 1; i <= allPercentage.size(); i++) {
                for (int j = 1; j <= allPercentage.get(i - 1).size(); j++) {
                    gridAlmostAffine.add(new Label(String.format("%.2f", allPercentage.get(i - 1).get(j - 1))), j, i);
                }
            }
            for (int i = 1; i <= 12; i++) {
                gridSpeedValues.add(new Label(time.get(i - 1).get(0) + " ms"), 4, i);
                gridSpeedValues.add(new Label(time.get(i - 1).get(1) + " ms"), 5, i);
            }
            long[] averageTime = getAverageTime(time);
            gridSpeedValues.add(new Label(averageTime[0] + " ms"), 4, 13);
            gridSpeedValues.add(new Label(averageTime[1] + " ms"), 5, 13);
        } else {
            statusLabelAlmostAffine.setText("Load a file first!");
        }
    }

    public void clearGrid(GridPane gridPane) {
        gridPane.getChildren().clear();
    }

    public void initGrid(GridPane gridPane) {
        gridPane.setGridLinesVisible(true);
        for (int i = 1; i <= parametersSet.size(); i++) {
            gridPane.add(new Label(parametersSet.get(i - 1)), 0, i);
        }
        for (int i = 1; i <= loopsNames.size(); i++) {
            gridPane.add(new Label(loopsNames.get(i - 1)), i, 0);
        }
    }

    public long[] getAverageTime(List<List<Long>> timeList) {
        long encodeTime = 0, decodeTime = 0;
        for (List<Long> list : timeList) {
            encodeTime += list.get(0);
            decodeTime += list.get(1);
        }
        return new long[]{encodeTime / timeList.size(), decodeTime / timeList.size()};
    }

    public void preloadSpeedGrid(GridPane speedGrid) {
        List<String> preloadData = List.of("Run", "tests", "to", "collect", "experimental", "data");

        speedGrid.add(new Label(preloadData.get(0)), 0, 6);
        speedGrid.add(new Label(preloadData.get(1)), 1, 6);
        speedGrid.add(new Label(preloadData.get(2)), 2, 6);
        speedGrid.add(new Label(preloadData.get(3)), 3, 6);
        speedGrid.add(new Label(preloadData.get(4)), 4, 6);
        speedGrid.add(new Label(preloadData.get(5)), 5, 6);
    }

    public void initSpeedGrid(GridPane speedGrid) {
        speedGrid.setGridLinesVisible(true);
        for (int i = 1; i <= parametersSet.size(); i++) {
            speedGrid.add(new Label(parametersSet.get(i - 1)), 0, i);
        }
        for (int i = 1; i <= parametersSets.size(); i++) {
            speedGrid.add(new Label(String.valueOf(parametersSets.get(i - 1).getError())), 1, i);
            speedGrid.add(new Label(String.valueOf(parametersSets.get(i - 1).getPrecision())), 2, i);
            speedGrid.add(new Label(String.valueOf(parametersSets.get(i - 1).getMaxBitAmount())), 3, i);

        }
        speedGrid.add(new Label("Error"), 1, 0);
        speedGrid.add(new Label("Precision"), 2, 0);
        speedGrid.add(new Label("Bit quantity"), 3, 0);
        speedGrid.add(new Label("Encode"), 4, 0);
        speedGrid.add(new Label("Decode"), 5, 0);
        speedGrid.add(new Label("Average"), 3, 13);

    }
}
