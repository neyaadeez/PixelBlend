import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	int[][][] imgOne;
	double zoomFactor=1;
	double rotationFactor=0.0;
	int counter = 0;

	// Modify the height and width values here to read and display an image with
  	// different dimensions. 
	int width = 512;
	int height = 512;

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath)
	{
		try
		{
			int frameLength = width*height*3;

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
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

    public BufferedImage zoom(double zoomFactor, double angle){
        int temp[][][] = new int[width][height][3];
        BufferedImage rbuf = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int newHeight = height;
		int newWidth = width;
		if(zoomFactor < 1){
			newWidth = (int) (width * zoomFactor);
        	newHeight = (int) (height * zoomFactor);
			for(int y=0; y<newHeight; y++){
				for(int x=0; x<newWidth; x++){
					// pixels average range
					int startX = Math.max(0, (int)(x / zoomFactor - 1));
					int startY = Math.max(0, (int)(y / zoomFactor - 1));
					int endX = Math.min(width - 1, startX + 3);
					int endY = Math.min(height - 1, startY + 3);

					
					int totalRed = 0;
					int totalGreen = 0;
					int totalBlue = 0;
					int pixelsTotal = 0;

					// pixels average
					for (int j = startY; j < endY; j++) {
						for (int i = startX; i < endX; i++) {
							int originX = Math.min(i, width - 1);
							int originY = Math.min(j, height - 1);
							int r = imgOne[originX][originY][0];
							int g = imgOne[originX][originY][1];
							int b = imgOne[originX][originY][2];
							totalRed += r;
							totalGreen += g;
							totalBlue += b;
							pixelsTotal+=1;
						}
					}

					// average RGB values
					int avgRed = totalRed / pixelsTotal;
					int avgGreen = totalGreen / pixelsTotal;
					int avgBlue = totalBlue / pixelsTotal;
					int xCord = (width - newWidth) / 2;
					int yCord = (height - newHeight) / 2;
					temp[x+xCord][y+yCord][0] = avgRed;
					temp[x+xCord][y+yCord][1] = avgGreen;
					temp[x+xCord][y+yCord][2] = avgBlue;
				}
        	}
			for(int y=0; y<height; y++){
				for(int x=0; x<width; x++){
					double radians = Math.toRadians(angle);
                	int rotatedX = (int) (Math.cos(radians) * (x - width / 2) - Math.sin(radians) * (y - height / 2) + width / 2);
                	int rotatedY = (int) (Math.sin(radians) * (x - width / 2) + Math.cos(radians) * (y - height / 2) + height / 2);
					if (rotatedX >= 0 && rotatedX < width && rotatedY >= 0 && rotatedY < height){
						int r = temp[rotatedX][rotatedY][0];
						int g = temp[rotatedX][rotatedY][1];
						int b = temp[rotatedX][rotatedY][2];
						int pixVal = (255 << 24) | (r << 16) | (g << 8) | b;
						rbuf.setRGB(x, y, pixVal);
					}
				}
        	}
		}
		else{
			for(int y=0; y<newHeight; y++){
				for(int x=0; x<newWidth; x++){
					int originX = (int) ((x - width / 2) / zoomFactor + width / 2);
					int originY = (int) ((y - height / 2) / zoomFactor + height / 2);
					double radians = Math.toRadians(angle);
                	int rotatedX = (int) (Math.cos(radians) * (originX - width / 2) - Math.sin(radians) * (originY - height / 2) + width / 2);
                	int rotatedY = (int) (Math.sin(radians) * (originX - width / 2) + Math.cos(radians) * (originY - height / 2) + height / 2);
					if (rotatedX >= 0 && rotatedX < width && rotatedY >= 0 && rotatedY < height){
						int r = imgOne[rotatedX][rotatedY][0];
						int g = imgOne[rotatedX][rotatedY][1];
						int b = imgOne[rotatedX][rotatedY][2];
						int pixVal = (255 << 24) | (r << 16) | (g << 8) | b;
						rbuf.setRGB(x, y, pixVal);
					}
				}
        	}
		}

        return rbuf;
    }

	public void showIms(String[] args){

		// Read in the specified image
		readImageRGB(width, height, args[0]);

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);
		double zoomF = Double.parseDouble(args[1])-1;
		double rotation = Double.parseDouble(args[2]);
		int fps = Integer.parseInt(args[3]);

        BufferedImage im1 = zoom(zoomFactor, rotationFactor);

		lbIm1 = new JLabel(new ImageIcon(im1));
		Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
				counter+=1;
                zoomFactor = 1 + (counter * zoomF);
				System.out.println("At "+counter+" Second: "+zoomFactor);
                rotationFactor += rotation;

                lbIm1.setIcon(new ImageIcon(zoom(zoomFactor, rotationFactor)));

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

	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}
