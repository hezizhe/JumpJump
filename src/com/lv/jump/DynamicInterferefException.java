package com.lv.jump;

/**
 * 画面动效干扰异常
 * @author lvbin
 */
public class DynamicInterferefException extends Exception {

	private static final long serialVersionUID = 1L;

	public DynamicInterferefException() {
		super("好友头像误识别，需重新扫描！");
	}
}
