package com.akaxin.client.util.toast;

import android.view.Gravity;

public class MToaster extends Toaster {

	@Override
	protected void makeNewToast() {
		super.makeNewToast();
		toast.setGravity(Gravity.CENTER, -1, 0);
	}

	public static MToaster getInstance() {
		MToaster toaster = new MToaster();
		toaster.makeNewToast();
		return toaster;
	}
	
	/**
	 * 获得一个Toaster实例
	 * @param gravity 悬停类型
	 * @param xOffset x 轴的偏移量，居中可以为-1。偏移量是相对于设置的 gravity 而言。
	 * @param yOffset y 轴的偏移量, 居中可以为-1。偏移量是相对于设置的 gravity 而言。
	 * @return
	 */
	public static MToaster getInstance(int gravity, int xOffset, int yOffset) {
		MToaster toaster = getInstance();
		toaster.setGravity(gravity, xOffset, yOffset);
		return toaster;
	}

	
	@Override
	public void showMsg(String msg, boolean makeNew, int duration) {
		if(makeNew) {
			makeNewToast();
		}
		
//		textView.setText(msg);
		toast.setText(msg);
		
		
		if(msg != null && msg.length() > 9 && duration == LENGTH_SHORT){
			toast.setDuration(LENGTH_LONG);
		} else {
			toast.setDuration(duration);
		}
		
		toast.show();
	}
}
