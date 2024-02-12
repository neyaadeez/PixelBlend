import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;

	int[][][] imgOne;
	float zoomFactor=1;
	float rotationFactor=0.0f;
	int counter = 0;
	int timerCounter = 0;
	BufferedImage[] framesResult;
	float prevZoomVal = 1;
	float prevRotateVal = 0;
	int fps;
	float zoomF;
	float rotation;
	int nThreads = 2;
	BlockingQueue<ResultWithIndex> franQueue = new LinkedBlockingQueue<>();

	// Modify the height and width values here to read and display an image with
  	// different dimensions. 
	int width = 512;
	int height = 512;
	BufferedImage[] rbuf = new BufferedImage[nThreads];


	// Helper class to store result with index
    static class ResultWithIndex {
        int index;
        BufferedImage[] result;

        public ResultWithIndex(int index, BufferedImage[] result) {
            this.index = index;
            this.result = result;
        }
    }

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

	public void arrToRGB(int[][][] arr, BufferedImage img){
		for(int y=0; y<height; y++){
			for(int x=0; x<width; x++){
				int r = arr[x][y][0];
				int g = arr[x][y][1];
				int b = arr[x][y][2];
				int pixVal = (255 << 24) | (r << 16) | (g << 8) | b;
				img.setRGB(x, y, pixVal);
			}
		}
	}
    public int[][][] zoom(float zoomFactor, float angle, int processId){
		int[][][] temp = new int[width][height][3];
		int[][][] resultArr = new int[width][height][3];
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
					float radians = (float)Math.toRadians(angle);
                	int rotatedX = (int) (Math.cos(radians) * (x - width / 2) - Math.sin(radians) * (y - height / 2) + width / 2);
                	int rotatedY = (int) (Math.sin(radians) * (x - width / 2) + Math.cos(radians) * (y - height / 2) + height / 2);
					if (rotatedX >= 0 && rotatedX < width && rotatedY >= 0 && rotatedY < height){
						resultArr[x][y][0] = temp[rotatedX][rotatedY][0];
						resultArr[x][y][1] = temp[rotatedX][rotatedY][1];
						resultArr[x][y][2] = temp[rotatedX][rotatedY][2];
						// int pixVal = (255 << 24) | (r << 16) | (g << 8) | b;
						
						// rbuf[processId].setRGB(x, y, pixVal);
					}
				}
        	}
		}
		else{
			for(int y=0; y<newHeight; y++){
				for(int x=0; x<newWidth; x++){
					int originX = (int) ((x - width / 2) / zoomFactor + width / 2);
					int originY = (int) ((y - height / 2) / zoomFactor + height / 2);
					float radians = (float)Math.toRadians(angle);
                	int rotatedX = (int) (Math.cos(radians) * (originX - width / 2) - Math.sin(radians) * (originY - height / 2) + width / 2);
                	int rotatedY = (int) (Math.sin(radians) * (originX - width / 2) + Math.cos(radians) * (originY - height / 2) + height / 2);
					if (rotatedX >= 0 && rotatedX < width && rotatedY >= 0 && rotatedY < height){
						resultArr[x][y][0] = imgOne[rotatedX][rotatedY][0];
						resultArr[x][y][1] = imgOne[rotatedX][rotatedY][1];
						resultArr[x][y][2] = imgOne[rotatedX][rotatedY][2];
						// int pixVal = (255 << 24) | (r << 16) | (g << 8) | b;
						// rbuf[processId].setRGB(x, y, pixVal);
					}
				}
        	}
		}

        return resultArr;
    }
	
	public BufferedImage[] frames(float zoomValue, float rotationValue, int fps, float prevRotateVal, float prevZoomVal, int processId){
		float zoomCalcFactor = (zoomValue-prevZoomVal)/fps;
		float rotataionCalcFactor = (rotationValue-prevRotateVal)/fps;
		BufferedImage[] framesArray = new BufferedImage[fps];
		int[][][] temp1 = new int[width][height][3];
		int k = 0;
		for(int i=0; i<fps; i++){
			// zoomSequence[i] = zoomCalcFactor*k;
			// rotationSequence[i] = rotataionCalcFactor*k;
			// k+=1;
			framesArray[i] = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);
			temp1 = zoom(prevZoomVal+zoomCalcFactor*k, ((rotataionCalcFactor*k)+prevRotateVal), processId);
			arrToRGB(temp1, framesArray[i]);
			k+=1;
		}
		return framesArray;
	}

	public void MultiTFrames(){
		
		ExecutorService executor = Executors.newFixedThreadPool(nThreads);

		for (int i = 0; i < nThreads; i++) {
			prevZoomVal = zoomFactor;
			prevRotateVal = rotationFactor;
			counter++;
			zoomFactor = 1 + (counter * zoomF);
			rotationFactor += rotation;
			float arg1Zoom = zoomFactor;
			float arg2Rotate = rotationFactor;
			float arg4PreR = prevRotateVal;
			float arg5PreZ = prevZoomVal;
			int c = counter;
			final int index = i;
			executor.execute(new Runnable() {
				@Override
				public void run() {
					System.out.println("PrevZ: "+arg5PreZ+" PrevR: "+arg4PreR+" Counter: "+c+" ZoomFactor: "+arg1Zoom+" rotationFactor: "+arg2Rotate);
					BufferedImage[] frame = frames(arg1Zoom, arg2Rotate, fps, arg4PreR, arg5PreZ, index);
					try{
						franQueue.put(new ResultWithIndex(c, frame));
					}
					catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			});
		}
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		 }
	}

	public void showIms(String[] args){

		// Read in the specified image
		readImageRGB(width, height, args[0]);

		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);
		zoomF = Float.parseFloat(args[1])-1;
		rotation = Float.parseFloat(args[2]);
		fps = Integer.parseInt(args[3]);
		for(int i=0; i<nThreads; i++){
			rbuf[i] = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);
		}

		int[][][] im1 = zoom(zoomFactor, rotationFactor, 0);
		BufferedImage imgBuff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		arrToRGB(im1, imgBuff);
		lbIm1 = new JLabel(new ImageIcon(imgBuff));
		
		Timer timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//framesResult = frames(zoomFactor, rotationFactor, fps);

				timerCounter+=1;
				
				
				ResultWithIndex result = (ResultWithIndex) franQueue.stream().filter(r -> ((ResultWithIndex) r).index == timerCounter).findFirst().orElse(null);
				framesResult = result.result;
				
				// Display each frame separately with a delay
				for (int i = 0; i < fps; i++) {
					final int index = i; // Final variable for use in the ActionListener
					Timer frameTimer = new Timer(i * 1000 / fps, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							lbIm1.setIcon(new ImageIcon(framesResult[index]));
							frame.revalidate();
							frame.repaint();
						}
					});
					frameTimer.setRepeats(false); // Ensure each frame is displayed only once
					frameTimer.start();
				}
			}
		});
		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
                MultiTFrames();
				System.gc();
        }, 1, 1, TimeUnit.SECONDS);
		MultiTFrames();
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
