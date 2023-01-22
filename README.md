# Research about steganographic methods of data concealing into vector graphic images

This project was created for a bachelor graduation work and is dedicated to an implementation of Kinzeryaviy's methods of data concealing in vector images.

The key points of the implemantation:
- SVG images
- Bezier curves
- De Casteljau algorithm
- JavaScript

Problems:
- parser for handling SVG-images is not ideal
- images should be specially chosen (or created on your own)

Additional:
- examples of recommended images-containers can be found in ./images folder
- after encoding stegokeys should be saved to a suggested directory
- decoding requires stegokeys saved while encoding

## NOTE
Experiments page does not have any visual representation, all obtained data is shown in the console. For any additional information you can get acquainted with my publications [here](https://www.scopus.com/authid/detail.uri?authorId=57208665914).

    
    
This is a project, devoted to implementation of the methods of data concealing in vector images (algorithms are based on Kinseryaviy's works). Please, use only images, which are provided in resources/svgImages directory (unless you do not mind getting nothing from application). A project is created for research, image parser is not ideal and does not track all exceptions of vector images. Used format of vector images is SVG. Initial code may change within time. Have fun!