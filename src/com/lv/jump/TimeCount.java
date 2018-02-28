package com.lv.jump;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * 计算按压时间工具类
 * @author lvbin
 */
public class TimeCount {
	
	private Point aimPoint = null;
	private Point playerPoint = null;
	
	private static double LENGTH_DIF;
	private static double TIME_DIF;
	
	public static void main(String[] args) throws Exception {
		new TimeCount().getTime("D:/PracticeWorkspace/JumpJump/screenimg/29.png", 29);
	}
	
	public int getTime(String path, int i) throws Exception {
		System.out.println("=========== " + i + " ============");
		aimPoint = null;
		playerPoint = null;
        File file = new File(path);  
        BufferedImage image = null;  
        try {
            image = ImageIO.read(file);  
        } catch (Exception e) {
            e.printStackTrace();  
        }
        int width = image.getWidth();
        LENGTH_DIF = (double)width / 1080;
        TIME_DIF = 1080.0 / width;
        findPoints(image);
        System.out.println("目标点：  " + aimPoint.x + "   " + aimPoint.y);
        System.out.println("玩家位置：  " + playerPoint.x + "   " + playerPoint.y);
        int x = aimPoint.x - playerPoint.x;
        int y = aimPoint.y - playerPoint.y;
        x = x < 0 ? -x : x;
        if(x < 40 * LENGTH_DIF){
        	//棋子与目标点出现重叠，极大可能为把好友头像剔除算法失败或者被画面动态效果干扰，重新截图即可
        	throw new DynamicInterferefException();
        }
        int time = (int)(Math.sqrt(x * x + y * y) * 1.378 * TIME_DIF);
        return time;
	}
	
	private void findPoints(BufferedImage image) throws Exception{
		int width = image.getWidth();  
        int height = image.getHeight();  
        int minx= 0;
        //避免消息弹窗（如短信）干扰
        int miny = (int) (500 * LENGTH_DIF);
        Point topPoint = null;
        int[] bgRgb = new int[3];
        int aimPixel = 0;
        int playerAreaLeft = 0;
    	int playerAreaRight = 0;
        for (int y = miny; y < height; y++) { 
        	for (int x = minx; x < width; x++) {  
                int pixel = image.getRGB(x, y); 
                int r = (pixel & 0xff0000) >> 16;  
	            int g = (pixel & 0xff00) >> 8;  
	            int b = (pixel & 0xff); 
                if(x == minx && y == miny){
                	bgRgb[0] = r; bgRgb[1] = g; bgRgb[2] = b;
                	if(r < 100 || g < 100 || b < 100)
                		throw new Exception("游戏结束！");
                }
                if(!isBgcolor(bgRgb, r, g, b) && aimPixel == 0){
                	//规避错误查找区域
                	if(playerAreaLeft != 0 && x > playerAreaLeft && x < playerAreaRight){
                		continue;
                	}
                	/*//头像比顶点高，规避头像区域寻找顶点
                	if(isHeadPhoto(image.getRGB(x + 20, y), image.getRGB(x + 10, y - 90), bgRgb) && playerAreaLeft == 0){
                		System.out.println(x + " " + y);
                		System.out.println("识别为头像");
                		//头像宽度为88，避开头像中心左右45像素可确保避开头像的范围继续寻找目标块顶点
                		playerAreaLeft = x + 35 - 45;
                		playerAreaRight = x + 35 + 45;
                		continue;
                	}*/
                	//棋子比顶点高，规避棋子区域寻找顶点
                	if(isPlayer(r , g, b) && playerAreaLeft == 0){
                		//棋子宽度为76，避开棋子中心左右40像素可确保避开棋子的范围继续寻找目标块顶点
                		Point playerTop = accuratePlayerPoint(image, new Point(x, y));
                		playerAreaLeft = playerTop.x - (int)(40 * LENGTH_DIF);
                		playerAreaRight = playerTop.x + (int)(40 * LENGTH_DIF);
                		playerPoint = new Point(playerTop.x, y + (int) (188 * LENGTH_DIF)); 
                		continue;
                	}
                	aimPixel = pixel;
                	topPoint = new Point(x, y);
                	Point leftPoint = findLeftPoint(image, topPoint, aimPixel);
                	//遇到不规则顶面，左顶点查找算法失败
    				if(topPoint.x - leftPoint.x < (int)(13 * LENGTH_DIF)){
    					leftPoint.y = topPoint.y + (int)(80 * LENGTH_DIF);
    				}
                	topPoint = accurateTopPoint(image, topPoint, aimPixel);
                	aimPoint = new Point(topPoint.x, leftPoint.y);
                	System.out.println("目标顶点:" + topPoint.x + "   " + topPoint.y);
                	System.out.println("目标左点:" + leftPoint.x + "   " + leftPoint.y);
                	//寻找中心白点
                	if(!nearColor(246, 246, 246, pixel, 2)){
	                	Point whitePoint  = findWhitePoint(image, topPoint);
	                	if(whitePoint != null){
	                		System.out.println("预定目标点:" + aimPoint.x + "   " + aimPoint.y);
	                		aimPoint = whitePoint;
	                		System.out.println("白点:" + aimPoint.x + "   " + aimPoint.y);
	                	}
                	}
                	playerPoint = findRlayerPoint(image, topPoint);
                	return;
            	}
            }
        }
        throw new Exception("未找到目标点");
	}
	
	private Point findWhitePoint(BufferedImage image, Point topPoint) {
		Point whitePoint = null;
		int minX = topPoint.x - (int)(24 * LENGTH_DIF); 
		int maxY = topPoint.x + (int)(24 * LENGTH_DIF);
        int height = topPoint.y + (int)(150 * LENGTH_DIF);  
        int miny = topPoint.y;
        for (int y = miny; y < height; y++) { 
        	for (int x = minX; x < maxY; x++) {  
                int pixel = image.getRGB(x, y);  
                int r = (pixel & 0xff0000) >> 16;  
	            int g = (pixel & 0xff00) >> 8;  
	            int b = (pixel & 0xff); 
	            if(r == 245 && g == 245 && b == 245){
	            	Point whiteTop = new Point(x, y);
	            	Point whiteLeft = findLeftPoint(image, whiteTop, pixel);
	            	whiteTop = accurateTopPoint(image, whiteTop, pixel);
	            	whitePoint = new Point(whiteTop.x, whiteLeft.y);
	            	return whitePoint;
	            }
            }
        }
		return whitePoint;
	}

	private Point accurateTopPoint(BufferedImage image, Point topPoint, int aimPixel) {
		int rightX = topPoint.x;
		while (true) {
			if (sameColor(image.getRGB(rightX + 1, topPoint.y), aimPixel)){
				rightX ++;
			}else {
				int ceterX = (int) (topPoint.x + ((double)rightX - topPoint.x) / 2);
				return new Point(ceterX, topPoint.y);
			}
		}
	}

	private Point findLeftPoint(BufferedImage image, Point topPoint, int aimPixel){
		Point leftPoint = topPoint;
		int i = 0;
		while(true){
			//优先级别：1、左边一位 2、左下一位 3、下边一位
			if(sameColor(image.getRGB(leftPoint.x - 1, leftPoint.y), aimPixel)){
				leftPoint = new Point(leftPoint.x - 1, leftPoint.y);
				i = 0;
			} else if (sameColor(image.getRGB(leftPoint.x - 1, leftPoint.y + 1), aimPixel)){
				leftPoint = new Point(leftPoint.x - 1, leftPoint.y + 1);
				i = 0;
			} else if (sameColor(image.getRGB(leftPoint.x, leftPoint.y + 1), aimPixel) && i < 8){
				leftPoint = new Point(leftPoint.x, leftPoint.y + 1);
				i++;
			} else {
				leftPoint = accurateLeftPoint(image, leftPoint, aimPixel);
				return leftPoint;
			}
		}
	}
	
	private Point accurateLeftPoint(BufferedImage image, Point leftPoint, int aimPixel) {
		int topY = leftPoint.y;
		while (true) {
			if (sameColor(image.getRGB(leftPoint.x, topY - 1), aimPixel)){
				topY = topY - 1;
			}else {
				int ceterY = (int) (leftPoint.y - (leftPoint.y - (double)topY) / 2);
				return new Point(leftPoint.x, ceterY);
			}
		}
	}
	
	private Point findRlayerPoint(BufferedImage image, Point topPoint) throws Exception {
		if(playerPoint != null) {
			return playerPoint;
		}
		int h = (int) (188 * LENGTH_DIF);
		int w = (int) (2 * LENGTH_DIF);
		int width = image.getWidth();  
        int height = image.getHeight();  
        int miny = topPoint.y;
        for (int y = miny; y < height; y++) { 
        	for (int x = 0; x < width; x++) {  
                int pixel = image.getRGB(x, y);  
                int r = (pixel & 0xff0000) >> 16;  
	            int g = (pixel & 0xff00) >> 8;  
	            int b = (pixel & 0xff); 
	            if(isPlayer(r , g, b) && isPlayerBody(image.getRGB(x, y + h)) && isPlayer(image.getRGB(x + w, y))){
	            	Point palyerTop = accuratePlayerPoint(image, new Point(x, y));
	            	return new Point(palyerTop.x, y + h);
	            }
            }
        }
        throw new Exception("未找到玩家位置！");
	}
	
	private Point accuratePlayerPoint(BufferedImage image, Point playerPoint) {
		int rightX = playerPoint.x;
		while (true) {
			if (isPlayer(image.getRGB(rightX + 1, playerPoint.y))){
				rightX ++;
			}else {
				int ceterX = (int) (playerPoint.x + ((double)rightX - playerPoint.x) / 2);
				return new Point(ceterX, playerPoint.y);
			}
		}
	}
	
	private boolean isBgcolor(int[] rgb, int r, int g, int b){
		return nearColor(r, g, b, rgb[0], rgb[1], rgb[2], 25);
	}
	
	private boolean isPlayer(int r, int g, int b){
		return nearColor(r, g, b, 52, 53, 59, 10);
	}
	
	private boolean isPlayer(int pixel){
		return nearColor(52, 53, 59, pixel, 15);
	}
	
	private boolean isPlayerBody(int pixel){
		return nearColor(57, 56, 98, pixel, 25);
	}
	
	/*private boolean isHeadPhoto(int pixel, int pixel2, int[] rgb){
		return !nearColor(rgb[0], rgb[1], rgb[2], pixel, 25) && nearColor(rgb[0], rgb[1], rgb[2], pixel2, 25);
	}*/
	
	private boolean nearColor(int r, int g, int b, int ur, int ug, int ub, int dif){
		int difR = r - ur;
		int difG = g - ug;
		int difB = b - ub;
		return difR > -dif && difR < dif && difG > -dif && difG < dif && difB > -dif && difB < dif;
	}
	
	private boolean nearColor(int r, int g, int b, int pixel, int dif){
        int ur = (pixel & 0xff0000) >> 16;  
        int ug = (pixel & 0xff00) >> 8;  
        int ub = (pixel & 0xff); 
        return nearColor(r, g, b, ur, ug, ub, dif);
	}
	
	private boolean sameColor(int pixel0, int pixel1){
		int r = (pixel0 & 0xff0000) >> 16;  
        int g = (pixel0 & 0xff00) >> 8;  
        int b = (pixel0 & 0xff); 
		return nearColor(r, g, b, pixel1, 3);
	}

}
