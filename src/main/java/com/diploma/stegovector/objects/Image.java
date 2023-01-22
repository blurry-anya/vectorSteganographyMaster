package com.diploma.stegovector.objects;


import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Image {
    private List<List<Curve>> curves;
    private String additionalData;
    private int precision;

    public Image() {
    }

    public List<List<Curve>> getCurves() {
        return curves;
    }

    public void setCurves(List<List<Curve>> curves) {
        this.curves = curves;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setPrecision(int precision){
        this.precision = precision;
    }

    public void getVectorImageContent(File svgImage) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
        NodeList svgPaths = getNodeListOfPaths(svgImage);
        this.setCurves(getElements(svgPaths));
    }


    public void setModifiedContent(File file, List<String> modifiedStrings, String additionalData, Stage stage) {
        parseIntoSVG(file, modifiedStrings, additionalData, stage);
    }

    private NodeList getNodeListOfPaths(File image) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

        /** getting a document type of loaded image **/

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(image);

        String xpathExpression = "//path/@d";

//        String xpathExpressionForLink = "//a/@id='w3orgSnaps'";

        /** parsing a XML (SVG img) to NodeList by <path> **/

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        XPathExpression expression = xpath.compile(xpathExpression);


        NodeList linkElements =  document.getElementsByTagName("a");
        String linkElemAttr = "";
        for (int i = 0; i < linkElements.getLength(); i++) {
            String id = String.valueOf(linkElements.item(i).getAttributes().getNamedItem("id"));
            if(id.equals("id=\"w3orgSnaps\"")){
                linkElemAttr = linkElements.item(i).getAttributes().getNamedItem("href").getTextContent();
            }
        }

        additionalData = linkElemAttr;

        System.out.println("\t\t\tGOT LINK FROM SVG: " + additionalData);

        NodeList svgPaths = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
//        System.out.println("NodeList of <path>s : ");
//        for (int i = 0; i < svgPaths.getLength(); i++) {
//            System.out.println(svgPaths.item(i).getNodeValue());
//        }
//        System.out.println();

        return svgPaths;
    }

    private List<List<Curve>> getElements(NodeList svgPaths) {

        String[] strPaths = new String[svgPaths.getLength()];

        for (int i = 0; i < svgPaths.getLength(); i++) {
            strPaths[i] = svgPaths.item(i).getNodeValue();
        }

        List<List<Curve>> curves = parsePaths(strPaths); // strPaths[i] is raw data from <path> in svg

        return curves;
    }

    public List<List<Curve>> parsePaths(String[] paths) {


        List<List<Curve>> curves = new ArrayList<>();

        for (String str : paths) {
            List<String> listSplitElem = getSeparateView(str);

            Point startPoint = new Point(Double.parseDouble(listSplitElem.get(1)), Double.parseDouble(listSplitElem.get(2)), this.precision);

            /** trim in the way that every sublist starts with a literal**/
            List<Curve> chunked = splitByLiteral(listSplitElem);

            /** trim those segments that have more than 3 points **/
            for (int i = 0; i < chunked.size(); i++) {
                chunked.set(i, transformLongSegments(chunked.get(i)));
            }

            /** representing everything in canonical view, when segments look like {P0, P1, P2, P3} {P3 P4 P5 P6}{P6 ....} **/
            for (int i = 0; i < chunked.size(); i++) {
                chunked.set(i, transformToCanonicalView(chunked.get(i), startPoint));
            }

            for (int i = 0; i < chunked.size(); i++) {
                chunked.set(i, switchToAbsoluteCoordinates(chunked.get(i)));
            }

            /** setting validness to each curved segment **/
            for (Curve curve : chunked) {
                for (Segment segment : curve.getSegments()) {
                    segment.setValid(segment.isValid());
                }
            }
            curves.add(chunked);
        }

//        System.out.println("Retrieved curves: ");
//        for (List<Curve> listOfCurves : curves) {
//            System.out.println(listOfCurves);
//        }

        return curves;
    }

    public List<String> getSeparateView(String path) {
        path = path.replaceAll("\\s", ",");
        String[] strPath = path.split(",");
        List<String> listPath = splitByMinus(strPath);

        List<String> listSplitElem = new ArrayList<>();
        for (int i = 0; i < listPath.size(); i++) {
            List<String> newStrings = new ArrayList<>();
            if (listPath.get(i).matches("(.*)[a-zA-Z]{1}(.*)")) {
                newStrings = splitByReg(listPath.get(i), "[a-zA-Z]");
                while (newStrings.get(newStrings.size() - 1).matches("(.*)[a-zA-Z](.*)")) {
                    newStrings.addAll(splitByReg(newStrings.get(newStrings.size() - 1), "[a-zA-Z]"));
                    newStrings.remove(newStrings.size() - 4);
                }
            } else {
                newStrings.add(listPath.get(i));
            }
            listSplitElem.addAll(newStrings);
        }
        listSplitElem.removeAll(Arrays.asList("", null)); // got list of literals and points in separated view
        return listSplitElem;
    }

    public List<Curve> splitByLiteral(List<String> list) {
        List<Curve> res = new ArrayList<>();
        List<Integer> indexesOfCurves = getIndexes(list, "[Mm]");
//        System.out.println("[Mm] are at the indexes: " + indexesOfCurves);
        List<List<String>> segmentPoints = new ArrayList<>();
        for (int i = 0; i < indexesOfCurves.size(); i++) {
            if (i == indexesOfCurves.size() - 1) {
                segmentPoints.add(list.subList(indexesOfCurves.get(i), list.size()));
            } else {
                segmentPoints.add(list.subList(indexesOfCurves.get(i), indexesOfCurves.get(i + 1)));
            }
//            System.out.println("Segment points: " + segmentPoints);
            List<Integer> indexesOfSegments = getIndexes(segmentPoints.get(i), "[CcLlHhVvZz]");
//            System.out.println("[CcLlHhVvZz] are at the indexes: " + indexesOfSegments);

            List<Segment> splittedPoints = new ArrayList<>();
            for (int j = 0; j < indexesOfSegments.size(); j++) {
                List<String> points = new ArrayList<>();
                if (j == indexesOfSegments.size() - 1) {
                    points = segmentPoints.get(i).subList(indexesOfSegments.get(j), segmentPoints.get(i).size());
                } else {
                    points = segmentPoints.get(i).subList(indexesOfSegments.get(j), indexesOfSegments.get(j + 1));
                }
//                System.out.println("points : " + points);
                splittedPoints.add(new Segment(points.get(0), parseIntoPoints(points.subList(1, points.size())), false));
//                System.out.println("segments: " + splittedPoints);

            }
            res.add(new Curve(segmentPoints.get(i).get(0), new Point(Double.parseDouble(segmentPoints.get(i).get(1)), Double.parseDouble(segmentPoints.get(i).get(2)), this.precision), splittedPoints));
        }

        return res;
    }

    private List<Point> parseIntoPoints(List<String> list) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < list.size(); i += 2) {
            points.add(new Point(Double.parseDouble(list.get(i)), Double.parseDouble(list.get(i + 1)), this.precision));
        }
        return points;
    }

    private List<Integer> getIndexes(List<String> list, String regex) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).matches(regex)) {
                indexes.add(i);
            }
        }
        return indexes;
    }


    public Curve transformLongSegments(Curve curve) {
        List<Segment> list = curve.getSegments();
        List<Segment> transformed = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<Segment> chunkedPoints = new ArrayList<>();
            if (list.get(i).getLiteral().matches("[Cc]") && list.get(i).getPoints().size() > 3) {
//                System.out.println("Segment: " + list.get(i));
                for (int j = 0; j < list.get(i).getPoints().size(); j += 3) {
                    chunkedPoints.add(new Segment(list.get(i).getLiteral(), list.get(i).getPoints().subList(j, j + 3), false));
                }
                transformed.addAll(chunkedPoints);
            } else {
                transformed.add(list.get(i));
            }
        }
        curve.setSegments(transformed);
        return curve;
    }

    public Curve transformToCanonicalView(Curve curve, Point startPoint) {
        // had to firstly create another list with shifted points
        // and then add all of them to existing list
        List<Segment> list = curve.getSegments();
        List<Segment> listCanonView = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getLiteral().matches("[Cc]")) {
                List<Point> completedPoints = new ArrayList<>();
                if (i == 0) {
                    completedPoints.add(curve.getStartPoint());
                } else {
                    completedPoints.add(list.get(i - 1).getPoints().get(list.get(i).getPoints().size() - 1));
                }
                completedPoints.addAll(list.get(i).getPoints());
                listCanonView.add(new Segment(list.get(i).getLiteral(), completedPoints, false));
            } else {
                listCanonView.add(list.get(i));
            }
        }
        curve.setSegments(listCanonView);
        return curve;
    }

    public Curve switchToAbsoluteCoordinates(Curve curve) {
        List<Segment> list = curve.getSegments();

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getLiteral().matches("c")) {
                Point startSegmentPoint = list.get(i).getPoints().get(0);
                Segment tempSegment = new Segment();

                tempSegment.setLiteral(list.get(i).getLiteral().toUpperCase(Locale.ROOT));

                List<Point> tempPoints = list.get(i).getPoints().stream().map(el -> el.addPoint(startSegmentPoint)).collect(Collectors.toList());
                tempPoints.set(0, tempPoints.get(0).subtractPoint(startSegmentPoint));
                tempSegment.setPoints(tempPoints);
                list.set(i, tempSegment);
                list = refreshSegments(list);
            }
        }
        curve.setSegments(list);
        return curve;

    }

    private List<String> splitByReg(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        List<String> newList = new ArrayList<>();

        if (matcher.find()) {
            newList.add(text.substring(0, matcher.start()));
            newList.add(text.substring(matcher.start(), matcher.end()));
            newList.add(text.substring(matcher.end()));
        }

        return newList;
    }

    private List<String> splitByMinus(String[] array) {
        List<String> res = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            String[] cut = array[i].split("(?=-)");
            for (int j = 0; j < cut.length; j++) {
                res.add(cut[j]);
            }
        }
        return res;
    }

    private List<Segment> refreshSegments(List<Segment> segments) {
        for (int i = 1; i < segments.size(); i++) {
            if (segments.get(i).getLiteral().matches("[Cc]")) {
                if (segments.get(i).getPoints().get(0) != segments.get(i - 1).getPoints().get(segments.get(i).getPoints().size() - 1)) {
                    segments.get(i).getPoints().set(0, segments.get(i - 1).getPoints().get(segments.get(i).getPoints().size() - 1));
                }
            }
        }
        return segments;
    }


    public int getAmountOfCurves() {
        int count = 0;
        for (List<Curve> curveList : this.curves) {
            count += curveList.size();
        }
        return count;
    }

    public int getAmountOfSegments() {
        int count = 0;
        for (List<Curve> curveList : this.curves) {
            for (Curve curve : curveList) {
                count += curve.getSegments().size();
            }
        }
        return count;
    }

    public int getAmountOfValidSegments() {
        int count = 0;
        for (List<Curve> curveList : this.curves) {
            for (Curve curve : curveList) {
                for (Segment segment : curve.getSegments()) {
                    if (segment.getValid()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public Image createClone(){
        Image cloned = new Image();
        List<List<Curve>> clonedList = new ArrayList<>();
        for (List<Curve> listOfCurves:this.getCurves()) {
            List<Curve> newList = new ArrayList<>();
            for (Curve curve:listOfCurves) {
                List<Segment> segmentList = new ArrayList<>();
                for (Segment segment: curve.getSegments()) {
                    List<Point> points = new ArrayList<>();
                    for (Point point:segment.getPoints()) {
                        points.add(point);
                    }
                    segmentList.add(new Segment(segment.getLiteral(), points, segment.getValid()));
                }
                newList.add(new Curve(curve.getLiteral(), curve.getStartPoint(), segmentList));
            }
            clonedList.add(newList);
        }
        cloned.setCurves(clonedList);
        return cloned;
    }


    /**
     * parsing back to document function
     **/

    private void parseIntoSVG(File originalImage, List<String> modifiedCurves, String link, Stage stage) {
        try {
            //File Path
            String filePath = originalImage.getPath();

            //Read XML file.
            File inputFile = new File(filePath);

            //Create DocumentBuilderFactory object.
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            //Get DocumentBuilder object.
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            //Parse XML file.
            Document document = dBuilder.parse(inputFile);

            //Get element by tag name.
            NodeList paths = document.getElementsByTagName("path");

            Element newLink = document.createElementNS("http://www.w3.org/2000/svg", "a");
            newLink.setAttribute("href", link);
            newLink.setAttribute("id", "w3orgSnaps");
            document.getElementsByTagName("g").item(0).appendChild(newLink);

            //Update 'd' attribute.
            System.out.println("Original paths found: " + paths.getLength());
            System.out.println("Modified curves amount: " + modifiedCurves.size());
            for (int i = 0; i < paths.getLength(); i++) {
                NamedNodeMap attr = paths.item(i).getAttributes();
                Node nodeAttr = attr.getNamedItem("d");
                nodeAttr.setTextContent(modifiedCurves.get(i));
                System.out.println("index: " + i);
                System.out.println("set value: " + nodeAttr.getNodeValue());
            }

            FileChooser fileChooser = new FileChooser();
            //Set extension filter for text files
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SVG files (*.svg)", "*.svg");
            fileChooser.getExtensionFilters().add(extFilter);
//            File file = fileChooser.showSaveDialog(stage);

            //Save changes into XML file.
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
//            StreamResult result = new StreamResult(new File("C:/Users/Anna/Desktop/diplomaTemporary/modified.svg"));
            StreamResult result = new StreamResult(fileChooser.showSaveDialog(stage));
            transformer.transform(source, result);




            //For console Output.
            StreamResult consoleResult = new StreamResult(System.out);
            transformer.transform(source, consoleResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public String toString() {
        String curves = "";
        for (List<Curve> list : this.curves) {
            curves += list.toString() + ' ';
        }
        return curves;
    }

}