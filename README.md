# Research about steganographic methods of data concealing into vector graphic images

This project was created for a master graduation work and is dedicated to an implementation of Kinzeryaviy's methods of data concealing in vector images.

The key points of the implemantation:
- SVG images
- Bezier curves
- De Casteljau algorithm
- Java

Problems:
- parser for handling SVG-images is not ideal
- images should be specially chosen (or created on your own)

Additional:
- examples of recommended images-containers can be found in resources\com\diploma\stegovector\svgImages folder

## APP DESCRIPTION
In this project for GUI a JavaFX library was used.  
The main window looks like following:
- image is uploaded, message goes to the according text area
<!-- ![Main window - encoding](./guiDemos/startWindowUploadedImg.jpg) -->
<img src="./guiDemos/startWindowUploadedImg.jpg" width="600" title="Main window - encoding"/>
- image is decoded, obtained secret message is in the according text area
<!-- ![Main window - decoding](./guiDemos/startWindowDecodedImg.jpg) -->
<img src="./guiDemos/startWindowDecodedImg.jpg" width="600" title="Main window - decoding"/>

The experiments window looks like following:
- initial view of the random chosen tab (before the experiment)
<!-- ![Experiments window - initial](./guiDemos/expWindowInitial.jpg) -->
<img src="./guiDemos/expWindowInitial.jpg" width="600" title="Experiments window - initial"/>
- a view of the random chosen tab (after the experiment)
<!-- ![Experiments window - performed experiment](./guiDemos/expWindowRunExp.jpg) -->
<img src="./guiDemos/expWindowRunExp.jpg" width="600" title="Experiments window - performed experiment"/>
- obtained time measures
<!-- ![Experiments window - obtained time measures](./guiDemos/expWindowRunExpTimeMeasures.jpg) -->
<img src="./guiDemos/expWindowRunExpTimeMeasures.jpg" width="600" title="Experiments window - obtained time measures"/>

*The point of experiments lays in getting the values of lost bits while applying different types of the affine transformations (for each set of parameters an affine transformation is applied 10 times - each time to previously changed container).*

## NOTE
For any additional information you can get acquainted with my publications [here](https://www.scopus.com/authid/detail.uri?authorId=57208665914).

    
