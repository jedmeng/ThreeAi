package cn.jedm;

import com.android.ddmlib.*;

import java.io.IOException;
import java.util.HashSet;

public class Three {

	private IDevice device;
	public static void main(String[] args) {
		Three three = new Three();
		three.run();
		//three.test();
	}

	public void run() {
		FetchScreen fs = new FetchScreen(device);
		Direction move;
		int i = 0;
		try {
			do {
				int[][] data = fs.getBlocks();
				move = Ai.run(data);
				move(move);
				Thread.currentThread().sleep(200);
			} while (move != null);
		} catch (GameOverException e) {

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void test() {
/*
		int[][] data = {
			{0, 0, 0, 0},
			{0, 0, 0, 0},
			{0, 0, 0, 0},
			{0, 0, 0, 0},
			{0, 0, 0, 0}
		}
		int[][] data = {
			{0, 0, 0, 0},
			{0, 0, 0, 0},
			{0, 3, 3, 48},
			{0, 3, 1, 48},
			{1, 0, 0, 0}
		};
		int[][] data = {
				{2, 6, 48, 6},
				{0, 2, 192, 1},
				{3, 0, 1, 6},
				{0, 0, 3, 0},
				{1, 0, 0, 0}
		};
 96  2  6 12
  3 24 48  1
  0  6  1 12
  2  3  1  6
  3
		*/
		int[][] data = {
				{96, 2, 6, 12},
				{3, 24, 48, 1},
				{0, 6, 1, 12},
				{2, 3, 1, 6},
				{3, 0, 0, 0}
		};
		for (int i=0; i<30; i++) {
			try {
				Direction move = Ai.run(data);
				if (move != null) {
					System.out.println(move.toString());
				}
			} catch (GameOverException e) {
				e.printStackTrace();
			}
		}

	}

	public void move(Direction direction) {
		int charcode;
		switch (direction) {
			case UP:
				charcode = 19;
				break;
			case DOWN:
				charcode = 20;
				break;
			case LEFT:
				charcode = 21;
				break;
			case RIGHT:
				charcode = 22;
				break;
			default:
				return;
		}
		try {
			device.executeShellCommand("input keyevent " + charcode, null);
		} catch (NullPointerException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Three() {
		device = getDevice();

	}

	private IDevice getDevice() {
		return this.getDevice(0);
	}

	/**
	 * 获取得到device对象
	 *
	 * @param index 设备序号
	 * @return 指定设备device对象
	 */
	private IDevice getDevice(int index) {
		IDevice device = null;
		AndroidDebugBridge.init(false);
		AndroidDebugBridge bridge = AndroidDebugBridge
				.createBridge("adb", false);// 如果代码有问题请查看API，修改此处的参数值试一下
		waitDevicesList(bridge);
		IDevice devices[] = bridge.getDevices();

		for (IDevice iDevice : devices) {
			Logger.info("获取到设备：" + iDevice.toString() + "\n\n");
		}

		if (devices.length < index + 1) {
			//没有检测到第index个设备
			System.err.print("没有检测到第" + index + "个设备");
		} else {
			if (devices.length - 1 >= index) {
				device = devices[index];
			} else {
				device = devices[0];
			}
		}
		return device;
	}

	/**
	 * 等待查找device
	 *
	 * @param bridge bridge
	 */
	private void waitDevicesList(AndroidDebugBridge bridge) {
		int count = 0;
		//System.out.println("Waiting for device...");
		while (!bridge.hasInitialDeviceList()) {
			try {
				Thread.sleep(500);
				count++;
			} catch (InterruptedException e) {
			}
			if (count > 60) {
				System.err.print("等待获取设备超时");
				break;
			}
		}
	}

}
