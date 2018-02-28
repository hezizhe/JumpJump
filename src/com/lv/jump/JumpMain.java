package com.lv.jump;

import java.util.Random;

/**
 * 跳一跳外挂工具
 * @author lvbin
 */
public class JumpMain {
	
	private static String ROOT;
	private static Random rondom = new Random();
	
	static{
		ROOT = JumpMain.class.getResource("/").getPath();
		if(ROOT.endsWith("bin/"))
			ROOT = ROOT.substring(1, ROOT.length() - 4);
	}
	
	public static void main(String[] args) throws Exception {
		TimeCount imgLengthCount = new TimeCount();
		for (int i = 0; i < 100; i++) {
			try {
				if(i == 99) i = 0;
				//Date d1 = new Date();
				Process process = Runtime.getRuntime().exec("adb shell /system/bin/screencap -p /sdcard/screenshot.png");
				process.waitFor();
				String imgPath = ROOT + "screenimg/" + i + ".png";
				if(imgPath.startsWith("/"))
					imgPath = imgPath.substring(1, imgPath.length());
				process = Runtime.getRuntime().exec("adb pull /sdcard/screenshot.png " + imgPath);
				process.waitFor();
				//Date d2 = new Date();
				int time = imgLengthCount.getTime(imgPath, i);
				int pressPointX = 400 + rondom.nextInt(100);
				int pressPointY = 1200 + rondom.nextInt(100);
				process = Runtime.getRuntime().exec("adb shell input swipe " + pressPointX + " " + pressPointY + " " + pressPointX + " " + pressPointY + " " + time);
				//Date d3 = new Date();
				/*System.out.println("截图加上传：" + (d2.getTime() - d1.getTime()));
				System.out.println("计算：" + (d3.getTime() - d2.getTime()));
				System.out.println("按压时间：" + time);
				System.out.println("休眠：" + (time * 2 + 1500));*/
				Thread.sleep(time * 2 + 1500);
			} catch (DynamicInterferefException e) {
				e.printStackTrace();
				//重新截图扫描可去除动效干扰
				continue;
			}
		}
	}

}
