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
		int[][] blocks = new int[5][4];
		RawImage rawImage = getScreenShot();
		Recognise recognise = Recognise.getRecognise(rawImage);
		for (int i=0; i<4; i++) {
			for (int j=0; j<4; j++) {
				try{
					blocks[i][j] = recognise.location(i, j);
				} catch(CannotRecogniseException e) {
					rawImage = getScreenShot();
					recognise = Recognise.getRecognise(rawImage);
					blocks[i][j] = recognise.location(i, j);
				}
			}

		}
		blocks[4][0] = recognise.next();
		return blocks;
	}

}
