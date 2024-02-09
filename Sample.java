import java.awt.*;
import java.awt.image.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;

public class Sample {

    JFrame frame;
    JLabel lbIm1;
    int[][][] imgOne;

    int width = 512;
    int height = 512;

    double zoomFactor = 1.0;
    double rotationAngle = 0.0;

    public void readImageRGB(int width, int height, String imgPath) {
        try {
            int frameLength = width * height * 3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;
            imgOne = new int[width][height][3];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    imgOne[x][y][0] = bytes[ind] & 0xFF; // Red
                    imgOne[x][y][1] = bytes[ind + height * width] & 0xFF; // Green
                    imgOne[x][y][2] = bytes[ind + height * width * 2] & 0xFF; // Blue
                    ind++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showIms(String[] args) {
        readImageRGB(width, height, args[0]);
		double zzoomm = Double.parseDouble(args[1]);
		double rotf = Double.parseDouble(args[2]);
		int fps = 1000/Integer.parseInt(args[3]);

        frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);

        lbIm1 = new JLabel(new ImageIcon(getBufferedImage()));

        Timer timer = new Timer(fps, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomFactor *= zzoomm;
                rotationAngle += rotf;

                lbIm1.setIcon(new ImageIcon(getTransformedImage()));

                frame.revalidate();
                frame.repaint();
            }
        });

        timer.start();

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(lbIm1, c);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public BufferedImage getBufferedImage() {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = imgOne[x][y][0];
                int g = imgOne[x][y][1];
                int b = imgOne[x][y][2];
                int pixelValue = (255 << 24) | (r << 16) | (g << 8) | b;
                bufferedImage.setRGB(x, y, pixelValue);
            }
        }
        return bufferedImage;
    }

    public BufferedImage getTransformedImage() {
        int[][][] transformedImage = new int[width][height][3];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Apply zoom and rotation transformations
                int originalX = (int) ((x - width / 2) / zoomFactor + width / 2);
                int originalY = (int) ((y - height / 2) / zoomFactor + height / 2);
                System.out.println(originalX +"---"+ originalY);

                double radians = Math.toRadians(rotationAngle);
                int rotatedX = (int) (Math.cos(radians) * (originalX - width / 2) - Math.sin(radians) * (originalY - height / 2) + width / 2);
                int rotatedY = (int) (Math.sin(radians) * (originalX - width / 2) + Math.cos(radians) * (originalY - height / 2) + height / 2);

                if (rotatedX >= 0 && rotatedX < width && rotatedY >= 0 && rotatedY < height) {
                    transformedImage[x][y][0] = imgOne[rotatedX][rotatedY][0];
                    transformedImage[x][y][1] = imgOne[rotatedX][rotatedY][1];
                    transformedImage[x][y][2] = imgOne[rotatedX][rotatedY][2];
                }
            }
        }

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = transformedImage[x][y][0];
                int g = transformedImage[x][y][1];
                int b = transformedImage[x][y][2];
                int pixelValue = (255 << 24) | (r << 16) | (g << 8) | b;
                bufferedImage.setRGB(x, y, pixelValue);
            }
        }

        return bufferedImage;
    }

    public static BufferedImage zoom(BufferedImage bufimg, double zoomFactor) {
        int width = bufimg.getWidth();
        int height = bufimg.getHeight();

        // Calculate the dimensions of the zoomed-out image
        int newWidth = (int) (width / zoomFactor);
        int newHeight = (int) (height / zoomFactor);

        BufferedImage rbuf = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        // Iterate over each pixel in the zoomed-out image
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                // Calculate the range of pixels to average
                int startX = (int) (x * zoomFactor);
                int endX = (int) ((x + 1) * zoomFactor);
                int startY = (int) (y * zoomFactor);
                int endY = (int) ((y + 1) * zoomFactor);

                // Initialize variables to accumulate RGB values
                int totalRed = 0;
                int totalGreen = 0;
                int totalBlue = 0;

                // Iterate over the pixels to average
                for (int j = startY; j < endY; j++) {
                    for (int i = startX; i < endX; i++) {
                        // Ensure i and j are within the bounds of the original image
                        int originX = Math.min(Math.max(i, 0), width - 1);
                        int originY = Math.min(Math.max(j, 0), height - 1);

                        int rgb = bufimg.getRGB(originX, originY);
                        totalRed += (rgb >> 16) & 0xFF;
                        totalGreen += (rgb >> 8) & 0xFF;
                        totalBlue += rgb & 0xFF;
                    }
                }

                // Calculate the average RGB values
                int avgRed = totalRed / (int) (zoomFactor * zoomFactor);
                int avgGreen = totalGreen / (int) (zoomFactor * zoomFactor);
                int avgBlue = totalBlue / (int) (zoomFactor * zoomFactor);

                // Combine the averaged RGB values into a single pixel value
                int pixelValue = (255 << 24) | (avgRed << 16) | (avgGreen << 8) | avgBlue;

                // Set the pixel value in the zoomed-out image
                rbuf.setRGB(x, y, pixelValue);
            }
        }

        return rbuf;
    }

    public static void main(String[] args) {
        Sample ren = new Sample();
        ren.showIms(args);
    }
}
