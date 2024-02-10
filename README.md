# PixelBlend

## Description
PixelBlend is a Java application designed to demonstrate the practical understanding of Spatial/Temporal Sampling and Filtering in the context of visual media types like images and videos. This project allows users to generate a video that simulates zooming and rotating into or out of an image.

## Functionality
- **Input:** 
  - The name of the image file (provided in an 8-bit per channel RGB format).
  - Zoom speed value (indicating how fast the zooming occurs).
  - Rotation speed value (controlling the rotation as the video zooms in or out).
  - Frames per second to display the video.
- **Output:**
  - A video with changing images, each of size 512x512, displayed successively until termination.
  - The content changes based on the input parameters, zooming and rotating as specified.
  - Frames are anti-aliased using simple averaging filters.

## Usage
1. Compile the program.
2. Run the program from the command line with the following arguments:
- `Z`: Zoom speed value (0.50 < Z < 2.00)
- `R`: Rotation speed value in degrees per second (-180.00 < R < 180.00)
- `F`: Frames per second to display the video (1 < F < 30)

## Example Invocations
1. `java YourProgramName C:/myDir/myImage.rgb 1.0 0.0 30`
- No zooming or rotation, displaying an unchanging image at 30 fps.
2. `java YourProgramName C:/myDir/myImage.rgb 1.25 30.0 1`
- Zooming at 1.25x per second with clockwise rotation of 30 degrees per second, displaying at 1 fps.
3. `java YourProgramName C:/myDir/myImage.rgb 0.8 -45.0 10`
- Zooming out, rotating anti-clockwise at 45 degrees per second, displaying at 10 fps.

## Implementation Details
- Images are displayed successively, with each frame generated by computing the RGB values for all pixels.
- Transformations are applied using matrices for zooming and rotating.
- Output frames are anti-aliased using simple averaging filters.
- Corner cases, such as areas outside the original image or undefined pixels, are handled by initializing them to white or black.

## Note
- The provided image is assumed to be of size 512x512.
- All parameters are expected to have reasonable values within specified ranges.

## Credits
This project was completed as part of the CSCI 576 course under the instruction of Professor Parag Havaldar at the University of Southern California.

**Professor Parag Havaldar**
- Website: [Parag Havaldar - USC Viterbi](https://viterbi.usc.edu/directory/faculty/Havaldar/Parag)
