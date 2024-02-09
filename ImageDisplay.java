import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;

	// Modify the height and width values here to read and display an image with
  	// different dimensions. 
	int width = 512;
	int height = 512;

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
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
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
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

    public BufferedImage zoom(BufferedImage bufimg, double zoomFactor){
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
					int startX = (int) (x / zoomFactor);
					int endX = (int) (((x + 1) / zoomFactor));
					int startY = (int) (y / zoomFactor);
					int endY = (int) (((y + 1) / zoomFactor));

					int totalRed = 0;
					int totalGreen = 0;
					int totalBlue = 0;

					// pixels average
					for (int j = startY; j < endY; j++) {
						for (int i = startX; i < endX; i++) {
							int originX = Math.min(i, width - 1);
							int originY = Math.min(j, height - 1);
							int rgb = bufimg.getRGB(originX, originY);
							totalRed += (rgb >> 16) & 0xFF;
							totalGreen += (rgb >> 8) & 0xFF;
							totalBlue += rgb & 0xFF;
						}
					}

					// average RGB values
					int avgRed = totalRed / ((endX - startX) * (endY - startY));
					int avgGreen = totalGreen / ((endX - startX) * (endY - startY));
					int avgBlue = totalBlue / ((endX - startX) * (endY - startY));
					int pixVal = (255 << 24) | (avgRed << 16) | (avgGreen << 8) | avgBlue;
					int xCord = (width - newWidth) / 2;
					int yCord = (height - newHeight) / 2;
					rbuf.setRGB(x+xCord, y+yCord, pixVal);
				}
        	}
		}
		else{
			for(int y=0; y<newHeight; y++){
				for(int x=0; x<newWidth; x++){
					int originX = (int) ((x - width / 2) / zoomFactor + width / 2);
					int originY = (int) ((y - height / 2) / zoomFactor + height / 2);
					int rgb = bufimg.getRGB(originX, originY);
					rbuf.setRGB(x, y, rgb);
				}
        	}
		}

        return rbuf;
    }

	public void showIms(String[] args){

		// Read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

        BufferedImage im1 = zoom(imgOne, Double.parseDouble(args[1]));

		lbIm1 = new JLabel(new ImageIcon(im1));

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
