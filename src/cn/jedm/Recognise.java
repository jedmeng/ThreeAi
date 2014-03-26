package cn.jedm;


import com.android.ddmlib.RawImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

abstract public class Recognise {

	protected byte[] data;
	private boolean isTurnOver = false;

	public static Recognise getRecognise(RawImage rawImage) {
		return getRecognise(rawImage.data, rawImage.width, rawImage.height);
	}

	public static Recognise getRecognise(byte[] data, int width, int height) {
		switch (height) {
			case 1920:
				return new Recognise_1920_1080(data);
		}
		throw new RuntimeException("Cannot found recognise class in your phone's resolution.");
	}

	public Recognise(byte[] data) {
		this.data = data;

		int[] rgb = getRGBColor(0, 0);
		if (rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0) {
			isTurnOver = true;
		}
	}

	abstract int location(int row, int cloumn) throws CannotRecogniseException;

	abstract int next();

	protected int next(int x, int y) {
		int[] rgb = getRGBColor(x, y);
		switch (rgb[1]) {
			case 0:
				return 4;
			case 255:
				return 3;
			case 102:
				return 2;
			case 204:
				return 1;
		}
		return 0;
	}

	protected int[] getRGBColor(int x, int y) {
		int i = y * getWidth() + x;
		if (isTurnOver) {
			i = getWidth() * getHeight() - 1 - i;
		}
		i *= 4;
		return new int[]{byte2int(data[i]), byte2int(data[i+1]), byte2int(data[i+2])};
	}

	protected int byte2int(Byte b) {
		return (int) b & 0xFF;
	}

	abstract protected int getWidth();
	abstract protected int getHeight();

}

class Recognise_1920_1080 extends Recognise {

	protected int width = 1080;
	protected int height = 1920;

	public Recognise_1920_1080(byte[] data) {
		super(data);
	}

	protected int getWidth() {
		return width;
	}

	protected int getHeight() {
		return height;
	}

	public int next() {
		return next(540, 200);
	}

	public int location(int row, int column) throws CannotRecogniseException {
		int startX = 153;
		int startY = 465;
		int stepX = 202;
		int stepY = 270;

		int x = startX + stepX * column;
		int y = startY + stepY * row;


		int[] rgb = getRGBColor(x + 20, y + 70);

		switch (rgb[1]) {
			case 102:
				return 2;
			case 204:
				return 1;
			case 217:
				return 0;
		}

		int[] t = countIntersection(x, y);

		switch (t[0] *10 + t[1]) {
			case 202: //10+10 2
				return 768;
			case 203: //10+10 3
			case 213: //10+11 3
				return 384;
			case 220: //10+13 0
				return 192;
			case 84: //8 4
				return 96;
			case 114: //11 4
				return 48;
			case 113: //11 3
				return 24;
			case 92: //9 2
				return 12;
			case 42: //4 2
				return 6;
			case 41: //4 1
				return 3;
		}
		save(x + 20, x + 150, y + 70, y + 170, t[0] + "_" + t[1]);
		throw new CannotRecogniseException("Can't recognise x:" + x + " y:" + y);
	}

	private int[] countIntersection(int x, int y) {
		int[] result = {0, 0};
		boolean intersection;
		int threshold = 30;
		int[] offsetY = {140, 87, 100, 119, 153};
		for (int i=0; i<offsetY.length; i++) {
			int yy = y + offsetY[i];
			int count = 0;
			intersection = false;
			for (int j=20; j<150; j++) {
				int[] rgb = getRGBColor(x + j, yy);
				if (rgb[1] < 150) {
					intersection = true;
					count++;
				} else if (intersection) {
					intersection = false;
					if (count >= threshold) {
						result[1]++;
					} else {
						result[0]++;
					}
					count = 0;
				}
			}
			if (i == 0 && result[0] == 0) {//100+
				result[0] += 10;
				threshold = 23;
			}
		}
		return result;
	}


	public void save(int x1, int x2, int y1, int y2) {
		save(x1, x2, y1, y2, "");
	}
	public void save(int x1, int x2, int y1, int y2, String add) {
		BufferedImage image = new BufferedImage(x2 - x1, y2 - y1,
				BufferedImage.TYPE_INT_RGB);
		for (int y = y1; y < y2; y++) {
			for (int x = x1; x < x2; x++) {
				int[] rgb = getRGBColor(x, y);
				int value = rgb[0] * 256 * 256 + rgb[1] * 256 + rgb[2];
				image.setRGB(x - x1, y - y1, value);
			}
		}
		try {
			ImageIO.write((RenderedImage) image, "PNG", new File("/tmp/threes/" + new Date().getTime()/1000  + "_" + y1 + "x" + x1 + "-" + y2 + "_" + add + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

class CannotRecogniseException extends RuntimeException {
	public CannotRecogniseException(String string) {
		super(string);
	}

}
