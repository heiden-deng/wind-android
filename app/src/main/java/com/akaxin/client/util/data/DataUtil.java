package com.akaxin.client.util.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本类是公用方法的集合 对数据进行格式化 处理与时间有关的数据 编码的转换 数据的精度控制 字符串全角与半角之间的转换
 *
 * @author sinosoft
 * @version 1.0 2009-07-22 新建
 */

public final class DataUtil {

    public static final String YYMMDD = "yyyy-MM-dd";
    public static final String YYMMDDHHMMSS = "yyyy-MM-dd hh:mm:ss";
    public static final String XMPP_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * 得到一个纯数字的陌陌ID
     *
     * @param momoid
     * @return
     */
    public static String getMomoIDNumber(String momoid) {
        if (momoid.indexOf("@") > 0) {
            momoid = momoid.substring(0, momoid.indexOf("@"));
        }
        return momoid;
    }
//	public static String getCountryName(String countryCode){
//		String[] countryCodes = getCountryCodes();
//		for (int i = 0; i < countryCodes.length; i++) {
//			if(countryCodes[i].equals(countryCode)){
//				return getCountryNames()[i];
//			}
//		}
//		return null;
//	}

    public static String getEncodePhoneNumber(String phoneNumber) {
        if (phoneNumber.length() > 7) {
            return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4, phoneNumber.length());
        }

        return phoneNumber;
    }

    public static String getEncodePhoneNumberWith7Stars(String phoneNumber) {
        if (phoneNumber.length() > 4) {
            return phoneNumber.substring(0, 2) + "*******" + phoneNumber.substring(phoneNumber.length() - 2, phoneNumber.length());
        }
        return phoneNumber;
    }

    public static String getCountryCode(String str) {
        //如果外部直接传入的就是+xx，直接返回即可
        if (!StringUtils.isEmpty(str) && str.contains("+")) {
            return str;
        }
        return "+" + str.substring(str.indexOf("(") + 1, str.length() - 1);
    }

    public static String getCountryName(String countrycode, String[] names) {
        String code = "(" + countrycode.substring(countrycode.indexOf("+") + 1) + ")";
        for (int i = 0; i < names.length; i++) {
            if (names[i].indexOf(code) > 0) {
                return names[i];
            }
        }

        return "未知";
    }

    /**
     * 默认构造器
     */
    private DataUtil() {
    }

    /**
     * 验证输入的邮箱格式是否符合
     *
     * @param email
     * @return 是否合法
     */
    public static boolean emailFormat(String email) {
        boolean tag = true;
        final String pattern1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        final Pattern pattern = Pattern.compile(pattern1);
        final Matcher mat = pattern.matcher(email);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    /**
     * 获得两个坐标点的距离
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public static float getDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] result = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, result);
        return result[0];
    }

    // 将角度转换为弧度
    static double deg2rad(double degree) {
        return degree / 180 * Math.PI;
    }

    // 将弧度转换为角度
    static double rad2deg(double radian) {
        return radian * 180 / Math.PI;
    }

    /**
     * 将输入流转换成字节数组
     *
     * @param inStream
     * @return
     * @throws Exception
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;

    }

    /**
     * 将字节流转换成Bitmap
     *
     * @param bytes
     * @param opts
     * @return
     */
    public static Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
        if (bytes != null)
            if (opts != null)
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
            else
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return null;
    }


    /**
     * 保留小数，保留后的小数位数在0~100范围内
     *
     * @param value 需要格式化的数字
     * @param num   需要保留的小数位数
     * @return 格式化后的数字
     * @throws RuntimeException 如果value不为数字或者num不在0~100范围内，则抛出异常
     */
    public static String formatPoint(String value, int num) throws RuntimeException {
        double tmpValue = 0;
        try {
            tmpValue = Double.parseDouble(value);
        } catch (Exception e) {
            throw new RuntimeException(value + "不是数字");
        }
        if (num < 0 || num > 100) {
            throw new RuntimeException(num + "不在保留的范围（0~100）内");
        }
        StringBuffer pointStr = new StringBuffer("##0");
        if (num != 0) {
            pointStr.append(".");
        }
        for (int i = 0; i < num; i++) {
            pointStr.append("0");
        }
        java.text.DecimalFormat df = new java.text.DecimalFormat(pointStr.toString());
        return df.format(tmpValue);
    }

    /**
     * 判断值是否不为空
     *
     * @param value 被判断的值
     * @return true-value不为空，false-value为空值
     */
    public static boolean hasValue(String value) {
        return (value != null && !"".equals(value));
    }

    public static boolean hasValue(String[] value) {
        return (value != null && value.length != 0);
    }

    public static boolean hasValue(Double mDouble) {
        return !(mDouble == null || mDouble == 0d);
    }

    public static boolean hasValue(Integer mInt) {
        return !(mInt == null || mInt == 0);
    }

    public static boolean hasValue(Float mFloat) {
        return !(mFloat == null || mFloat == 0f);
    }

    /**
     * 判断值是否不为空
     *
     * @param orignalValue 被判断的值
     * @return true-value不为空，false-value为空值
     */
    public static boolean hasValueN(String orignalValue) {
        if (orignalValue == null) {
            return false;
        } else if (orignalValue.equals("")) {
            return false;
        } else return !orignalValue.equalsIgnoreCase("null");
    }

    /**
     * 把日期类型转换成String
     *
     * @param value Date型日期
     * @param type  转换成String型日期后的格式
     * @return String型日期
     */
    public static String convertDateToString(Date value, String type) {
        SimpleDateFormat df = new SimpleDateFormat(type);
        return df.format(value);
    }

    /**
     * 把String转换成日期
     *
     * @param value String型日期
     * @param type  String型日期的格式
     * @return Date型日期
     * @throws ParseException 日期转换发生错误
     */
    public static Date convertStringToDate(String value, String type) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(type);
        return df.parse(value);
    }

    /**
     * 获取当前日期时间 参考格式：yyyy年MM月dd日HH时（hh时）mm分ss秒ms毫秒E本月第F个星期
     * 对应的值：2009年07月22日15时（03时）05分30秒530毫秒星期三本月第4个星期
     *
     * @param type 日期时间的格式
     * @return String型日期时间
     */
    public static String getCurrentDateTime(String type) {
        SimpleDateFormat df = new SimpleDateFormat(type);
        return df.format(new Date());
    }

    /**
     * 日期时间格式转换
     *
     * @param value      转换前的值
     * @param srcFormat  转换前的格式
     * @param destFormat 转换后的格式
     * @return 转换后的值
     * @throws ParseException 日期转换发生错误
     */
    public static String dateFormat(String value, String srcFormat, String destFormat) throws ParseException {
        Date date = convertStringToDate(value, srcFormat);
        return convertDateToString(date, destFormat);
    }

    /**
     * 计算两个日期的间隔
     *
     * @param type   间隔的单位：y-年，m-月，d-日，不填默认为日
     * @param sdate1 String型日期，格式为yyyy-MM-dd
     * @param sdate2 String型日期，格式为yyyy-MM-dd
     * @return 间隔的数量。如果日期sdate2在日期sdate1之后，则结果为正数；如果日期sdate2在日期sdate1之前，则结果为负数
     * @throws ParseException 日期转换发生错误
     */
    public static int dateDiff(String type, String sdate1, String sdate2) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = df.parse(sdate1);
        Date date2 = df.parse(sdate2);
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        int yearDiff = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);
        if ("y".equalsIgnoreCase(type)) {
            return yearDiff;
        } else if ("m".equalsIgnoreCase(type)) {
            int monthDiff = yearDiff * 12 + cal2.get(Calendar.MONTH) - cal1.get(Calendar.MONTH);
            return monthDiff;
        } else {
            long ldate1 = date1.getTime() + cal1.get(Calendar.ZONE_OFFSET) + cal1.get(Calendar.DST_OFFSET);
            long ldate2 = date2.getTime() + cal2.get(Calendar.ZONE_OFFSET) + cal2.get(Calendar.DST_OFFSET);
            int dayDiff = (int) ((ldate2 - ldate1) / (3600000 * 24));
            return dayDiff;
        }
    }

    /**
     * 毫秒值转换为Date类型
     *
     * @param time
     * @return
     */
    public static Date long2Date(Long time) {
        Date date = new Date(time);
        return date;
    }

    /**
     * 计算日期加上一段间隔之后的新日期
     *
     * @param type  间隔的单位：y-年，m-月，d-日，不填默认为日
     * @param sdate String型日期，格式为yyyy-MM-dd
     * @param num   间隔数量
     * @return 返回新日期，格式为yyyy-MM-dd
     * @throws ParseException 日期转换发生错误
     */
    public static String dateAdd(String type, String sdate, int num) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = df.parse(sdate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if ("y".equalsIgnoreCase(type)) {
            cal.add(Calendar.YEAR, num);
        } else if ("m".equalsIgnoreCase(type)) {
            cal.add(Calendar.MONTH, num);
        } else {
            cal.add(Calendar.DATE, num);
        }
        return df.format(cal.getTime());
    }

    /**
     * 计算两个时间相差的秒数
     *
     * @param time1 String型时间，格式为yyyy-MM-dd HH:mm:ss
     * @param time2 String型时间，格式为yyyy-MM-dd HH:mm:ss
     * @return 相差的秒数。如果时间time2在时间time1之后，则结果为正数；如果时间time2在时间time1之前，则结果为负数
     * @throws ParseException
     */
    public static long timeDiff(String time1, String time2) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = df.parse(time1);
        Date date2 = df.parse(time2);
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        long ldate1 = date1.getTime() + cal1.get(Calendar.ZONE_OFFSET) + cal1.get(Calendar.DST_OFFSET);
        long ldate2 = date2.getTime() + cal2.get(Calendar.ZONE_OFFSET) + cal2.get(Calendar.DST_OFFSET);
        long result = (ldate2 - ldate1) / 1000;
        return result;
    }

    /**
     * Unicode编码转换成GB2312编码
     *
     * @param src Unicode编码的字符串
     * @return GB2312编码的字符串
     * @throws UnsupportedEncodingException 编码转换错误
     */
    public static String UnicodeToGB(String src) throws UnsupportedEncodingException {
        if (DataUtil.hasValue(src)) {
            return new String(src.getBytes("ISO-8859-1"), "GB2312");
        } else {
            return src;
        }
    }

    /**
     * GB2312编码转换成Unicode编码
     *
     * @param src GB2312编码的字符串
     * @return Unicode编码的字符串
     * @throws UnsupportedEncodingException 编码转换错误
     */
    public static String GBToUnicode(String src) throws UnsupportedEncodingException {
        if (DataUtil.hasValue(src)) {
            return new String(src.getBytes("GB2312"), "ISO-8859-1");
        } else {
            return src;
        }
    }

    /**
     * 过滤跨站脚本关键字
     *
     * @param src 输入的字符串
     * @return 过滤后的字符串
     */
    public static String filterStr(String src) {
        if (DataUtil.hasValue(src)) {
            src = src.replaceAll("<", "");
            src = src.replaceAll(">", "");
            src = src.replaceAll("'", "");
            src = src.replaceAll("&", "＆");
            src = src.replaceAll("#", "＃");
            src = src.replaceAll("%", "％");
            src = src.replaceAll("\"", "");
            src = src.trim();
            return src;
        } else {
            return "";
        }
    }

    /**
     * 收集页面参数
     *
     * @param session
     *            HttpSession
     * @param request
     *            HttpServletRequest
     * @return session和request中的所有参数
     */
    /*
     * public static IndexMap getParameters(HttpSession session,
	 * HttpServletRequest request){ IndexMap hm = new IndexMap(); Enumeration en
	 * = null; if(session != null){ en = session.getAttributeNames();
	 * while(en.hasMoreElements()){ String tempName = (String) en.nextElement();
	 * hm.put(tempName, session.getAttribute(tempName)); } } if(request !=
	 * null){ en = request.getParameterNames(); while(en.hasMoreElements()){
	 * String tempName = (String) en.nextElement(); hm.put(tempName,
	 * filterStr(request.getParameter(tempName))); } } return hm; }
	 */

    /**
     * 半角转全角 半角空格为32，全角空格为12288 其他字符半角(33-126)与全角(65281-65374)的对应关系为：相差65248
     *
     * @param src 待转换的字符串
     * @return 转换后的全角字符串
     */
    public static String toSBC(String src) {
        char[] c = src.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 32) {
                c[i] = 12288;
            } else if (c[i] >= 33 && c[i] <= 126) {
                c[i] += 65248;
            }
        }
        return new String(c);
    }

    /**
     * 全角转半角 半角空格为32，全角空格为12288 其他字符半角(33-126)与全角(65281-65374)的对应关系为：相差65248
     *
     * @param src 待转换的字符串
     * @return 转换后的半角字符串
     */
    public static String toDBC(String src) {
        char[] c = src.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = 32;
            } else if (c[i] >= 65281 && c[i] <= 65374) {
                c[i] -= 65248;
            }
        }
        return new String(c);
    }

    /**
     * 获取项目所在路径
     *
     * @return 项目路径
     * @throws Exception 未找到路径
     */
    public static String getProjectLocalPath() throws Exception {
        String path = DataUtil.class.getResource("").getFile();
        path = URLDecoder.decode(path, "UTF-8");
        path = path.substring(0, path.lastIndexOf("/WEB-INF"));
        String temp = path.substring(0, 5);
        if ("file:".equalsIgnoreCase(temp)) {
            path = path.substring(5);
        }
        return path;
    }

    /**
     * 读取网页内容
     *
     * @param str 网页地址
     * @return str所指向的网页内容
     * @throws IOException
     */
    public static String getHtmlCodeByURL(String str) throws IOException {
        URL url = new URL(str);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStream in = urlConnection.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int count = -1;
        while (in != null && (count = in.read(data)) != -1) {
            out.write(data, 0, count);
        }
        out.flush();
        String result = out.toString();
        out.close();
        return result;
    }

//
//    /**
//     * 验证当前密码是否为弱类型
//     *
//     * @param password
//     * @return
//     */
//    public static boolean isWeakPwd(String password) {
//        String regexNumber = ".*[0-9]+.*";
//        String regexLetter = ".*[a-zA-Z]+.*";
//
//        if (password.length() < Configs.PASSWORD_MIN_LENGTH || password.length() > Configs.PASSWORD_MAX_LENGTH) {
//            return true;
//        }
//
//        if (password.matches(regexNumber)
//                && password.matches(regexLetter)) {
//            return false;
//        } else {
//            return true;
//        }
//    }


    // 星座计算
    public static String getConstellation(int month, int day) {
        if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) {
            return "水瓶座";
        } else if ((month == 2 && day >= 19) || (month == 3 && day <= 20)) {
            return "双鱼座";
        } else if ((month == 3 && day >= 21) || (month == 4 && day <= 19)) {
            return "白羊座";
        } else if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) {
            return "金牛座";
        } else if ((month == 5 && day >= 21) || (month == 6 && day <= 21)) {
            return "双子座";
        } else if ((month == 6 && day >= 22) || (month == 7 && day <= 22)) {
            return "巨蟹座";
        } else if ((month == 7 && day >= 23) || (month == 8 && day <= 22)) {
            return "狮子座";
        } else if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) {
            return "处女座";
        } else if ((month == 9 && day >= 23) || (month == 10 && day <= 23)) {
            return "天秤座";
        } else if ((month == 10 && day >= 24) || (month == 11 && day <= 22)) {
            return "天蝎座";
        } else if ((month == 11 && day >= 23) || (month == 12 && day <= 21)) {
            return "射手座";
        } else if ((month == 12 && day >= 22) || (month == 1 && day <= 19)) {
            return "摩羯座";
        }
        return "";
    }

//    /**
//     * 注册密码难度降低
//     * 1表示密码ok，
//     * 0表示密码为空
//     * -1表示密码长度不够
//     * -2表示密码长度超过限制
//     */
//    public static int registerPwdCheck(String pwd) {
//        if (StringUtils.isEmpty(pwd)) {
//            return 0;
//        } else if (pwd.length() < Configs.REGISTER_PASSWORD_MIN_LENGTH) {
//            return -1;
//        } else if (pwd.length() > Configs.PASSWORD_MAX_LENGTH) {
//            return -2;
//        } else {
//            return 1;
//        }
//    }

    public static String printHexString(String hint, byte[] b) {

        if (b == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase()).append(" ");
        }

        return sb.toString();
    }

}