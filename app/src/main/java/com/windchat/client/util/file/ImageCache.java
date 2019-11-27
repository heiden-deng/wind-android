package com.windchat.client.util.file;

import java.util.Date;

public class ImageCache {
	public String id = null;
	public String path = null;
	public int maxday = 7;
	public int type = -1;
	public Date time = null;
	
	public final static int TIME_EXPIRED = 15;
	
	
	/**
	 * 数据库字段：图片id，确保唯一性
	 */
	public final static String DBFIELD_IMAGEID = "i_imageid";
	
	/**
	 * 数据库字段：最后使用此图片的时间
	 */
	public final static String DBFIELD_TIME = "i_time";
	
	/**
	 * 数据库字段：图片类型1为头像小图，2为头像大图，3为聊天小图，4为聊天大图，5为其它,6为Gif头像，7为Video头像
	 */
	public final static String DBFIELD_TYPE = "i_type";
	
	/**
	 * 图片所在的绝对路径
	 */
	public final static String DBFIELD_PATH = "i_path";
	
	/**
	 * 图片所能保存的最大天数
	 */
	public final static String DBFIELD_MAXDAY = "i_maxday";
	
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof ImageCache && path != null) {
			return path.equals(((ImageCache)o).path);
		}
		
		return super.equals(o);
	}
	
}