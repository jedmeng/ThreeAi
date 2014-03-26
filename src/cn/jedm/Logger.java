package cn.jedm;


public class Logger {
	static boolean DEBUG = false;
	static void debug(String string) {
		if (DEBUG) {
			System.out.print(string);
		}
	}
	static void info(String string) {
		System.out.print(string);
	}
}
