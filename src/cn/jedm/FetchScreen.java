package cn.jedm;


import com.android.ddmlib.*;

class FetchScreen {

	private IDevice device;
	public FetchScreen(IDevice device) {
		this.device = device;
	}

	/**
	 * 直接抓取屏幕数据
	 * @return 屏幕数据
	 */
	public RawImage getScreenShot(){
		RawImage rawScreen = null;
		try {
			rawScreen = device.getScreenshot();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rawScreen;
	}

	public int[][] getBlocks() {
		int[][] blocks = new int[Grid.height + 1][Grid.width];
		RawImage rawImage = getScreenShot();
		Recognise recognise = Recognise.getRecognise(rawImage);
		for (int i=0; i<Grid.height; i++) {
			for (int j=0; j<Grid.width; j++) {
				try{
					blocks[i][j] = recognise.location(i, j);
				} catch(CannotRecogniseException e) {
					rawImage = getScreenShot();
					recognise = Recognise.getRecognise(rawImage);
					blocks[i][j] = recognise.location(i, j);
				}
			}

		}
		blocks[Grid.height][0] = recognise.next();
		return blocks;
	}

}
