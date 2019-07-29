package com.akaxin.client.util;


import com.akaxin.client.util.data.StringUtils;
import com.orhanobut.logger.Logger;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 *
 * @author <a href="mailt:wenlin56@sina.com"> wjh </a>
 */
public class DateUtil {
    public final static long DayMilliseconds = 86400000L;

    /**
     * 比较两个时间对象。得出比较结果字符串。所得到的结果可能是以下几种情况：<br/>
     *   ##30天前 (大于或等于30天) <br/>
     *   ##N天前 (N不大于30) <br/>
     *   ##N分钟前 (N不大于59) <br/>
     *   ##1分钟前 (差值等于或小于1——包括负值的情况（###1）)<br/>
     *   —— ###1 负值的情况： 当after时间在before时间之前时。例如，调用者这样传入参数：between(2011-10-8 12:12:00, 2011-10-1 12:12:00)<br>
     *   ##未知 （before==null || after ==null）
     *
     * @param before 较靠前的那个时间，
     * @param after 较靠后的那个时间。
     * @return
     */
    public static String between(Date before, Date after) {
        final int maxDay = 30;
        if (before == null || after == null) {
            return "未知";
        }

        if (after.before(before)) {
            return "1分钟前";
        }

        long afterL = after.getTime();
        long beforeL = before.getTime();

        long n = Math.abs(afterL - beforeL);

        n /= 1000;
        long minute = n / 60;
        long hour = minute / 60; // 最终相差小时
        minute %= 60;  // 最终相差分钟

        if (hour >= maxDay * 24) {
            return "30天前";
        }

        if (hour >= 24) {
            return (int) hour / 24 + "天前";  //(int)(Math.max(hour/24f, hour/24+0.9f)) + "天前";
        }

        if (hour > 0) {
            return hour + "小时前";
        }

        if (minute < 1) {
            minute = 1;
        }

        return minute + "分钟前";

    }

    public static String betweenForActiveUser(Date before, Date after) {
        final int maxDay = 30;
        if (before == null || after == null) {
            return "未知";
        }

        if (after.before(before)) {
            return "1分钟前";
        }

        long afterL = after.getTime();
        long beforeL = before.getTime();

        long n = Math.abs(afterL - beforeL);

        n /= 1000;
        long minute = n / 60;
        long hour = minute / 60; // 最终相差小时
        minute %= 60;  // 最终相差分钟

        if (hour >= maxDay * 24) {
            return "30天前";
        }

        if (hour >= 24) {
            int day = (int) hour / 24;
            if (day == 1) return "昨天";
            if (day == 2) return "前天";
            return (int) hour / 24 + "天前";  //(int)(Math.max(hour/24f, hour/24+0.9f)) + "天前";
        }

        if (hour > 0) {
            return hour + "小时前";
        }

        if (minute < 1) {
            minute = 1;
        }

        return minute + "分钟前";
    }

    public static long betweenTime(Date before, Date after) {
        if (before == null || after == null) {
            return -1;
        }

        long afterL = after.getTime();
        long beforeL = before.getTime();
        return Math.abs(afterL - beforeL);
    }

    /**
     * 获取今天凌晨的时间对象
     * @return
     */
    public static Calendar getDawnCalendar() {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());

        int year = now.get(Calendar.YEAR);
        int mounth = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);

        // 今天的凌晨
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(year, mounth, day, 0, 0);
        return todayStart;
    }

    /**
     * 通过Date格式化一个 科学倒序 的时间字符串
     * @param before 需要格式化的时间 1分钟前 /三小时前/ 昨天19：30 / 09-30
     * @return
     */
    public static String getTimeLineString(Date before) {
        return getTimeLineString(before, false);
    }

    /**
     * 通过Date格式化一个 科学倒序 的时间字符串
     * @param before 需要格式化的时间
     * @param isProfile 详情页面在倒叙格式上日期还需要显示具体的时间 1分钟前 /三小时前/ 昨天19：30 / 09-30 23：25
     * @return
     */
    public static String getTimeLineString(Date before, boolean isProfile) {
        if (before == null) {
            return "未知";
        }

        Date after = new Date();
        if (after.before(before)) {
            return "1分钟前";
        }

        Calendar calendarBefore = Calendar.getInstance();
        calendarBefore.setTime(before);
        int yearbefore = calendarBefore.get(Calendar.YEAR);
        int dayBefore = calendarBefore.get(Calendar.DAY_OF_YEAR);

        Calendar calendarNow = Calendar.getInstance();
        calendarNow.setTime(after);
        int yearNow = calendarNow.get(Calendar.YEAR);
        int dayNow = calendarNow.get(Calendar.DAY_OF_YEAR);

        if (yearNow > yearbefore) {
            if (isProfile) {
                return formateDateTimeWithOutYear(before);
            } else {
                return formateDate3(before);
            }
        }

        if (dayNow - dayBefore > 1) {
            if (isProfile) {
                return formateDateTimeWithOutYear(before);
            } else {
                return formateDate3(before);
            }
        }

        if (dayNow - dayBefore == 1) {
            return "昨天 " + formateTime(before);
        }

        long afterL = after.getTime();
        long beforeL = before.getTime();
        long n = Math.abs(afterL - beforeL);

        n /= 1000;
        long minute = n / 60;
        long hour = minute / 60; // 最终相差小时
        minute %= 60;  // 最终相差分钟

        if (hour >= 1) {
            return formateTime(before);
        }

        if (minute < 1) {
            minute = 1;
        }

        return minute + "分钟前";

    }

    /**
     * 通过Date格式化一个 科学倒序 的时间字符串
     * @return 11:32 / 昨天11:32 / 11月2日
     */
    public static String getTimeLineStringStyle2(Date before) {
        if (before == null) {
            return "未知";
        }

        Date after = new Date();
        if (after.before(before)) {
            // 消息时间晚于当前时间,显示小时和分钟
            Logger.i("yichao ===== after.before(before)");
            return formateDate3(before);
        }

        Calendar calendarBefore = Calendar.getInstance();
        calendarBefore.setTime(before);
        int yearbefore = calendarBefore.get(Calendar.YEAR);
        int dayBefore  = calendarBefore.get(Calendar.DAY_OF_YEAR);

        Calendar calendarNow = Calendar.getInstance();
        calendarNow.setTime(after);
        int yearNow = calendarNow.get(Calendar.YEAR);
        int dayNow = calendarNow.get(Calendar.DAY_OF_YEAR);

        if (yearNow > yearbefore) {
            return formateDateTime(before);
        }

        if (dayNow - dayBefore > 1) {
            return formateDateTimeWithOutYear(before);
        }

        if (dayNow - dayBefore == 1) {
            return "昨天 " + formateTime(before);
        }

        return formateTime(before);

    }

    /**
     * 7.5.2 修改
     * 新增规则：
     * 2016-12-01 、2015-12-09（超过今年，只显示年月日）
     * @param before
     * @return
     */
    public static String getTimeLineStringStyle3(Date before) {
        if (before == null) {
            return "未知";
        }

        Date after = new Date();
        if (after.before(before)) {
            return "1分钟前";
        }

        Calendar calendarBefore = Calendar.getInstance();
        calendarBefore.setTime(before);
        int yearbefore = calendarBefore.get(Calendar.YEAR);
        int dayBefore = calendarBefore.get(Calendar.DAY_OF_YEAR);

        Calendar calendarNow = Calendar.getInstance();
        calendarNow.setTime(after);
        int yearNow = calendarNow.get(Calendar.YEAR);
        int dayNow = calendarNow.get(Calendar.DAY_OF_YEAR);

        if (yearNow > yearbefore) {
            return formatDate(before);
        }

        if (dayNow - dayBefore > 1) {
            return formateDate3(before);
        }

        if (dayNow - dayBefore == 1) {
            return "昨天 " + formateTime(before);
        }

        long afterL = after.getTime();
        long beforeL = before.getTime();
        long n = Math.abs(afterL - beforeL);

        n /= 1000;
        long minute = n / 60;
        long hour = minute / 60; // 最终相差小时
        minute %= 60;  // 最终相差分钟

        if (hour >= 1) {
            return formateTime(before);
        }

        if (minute < 1) {
            minute = 1;
        }

        return minute + "分钟前";

    }

    /**
     * 7.6.2 新增
     * 60分钟以下，展示格式为“xx分钟前”
     * 60分钟以上，展示格式为“xx小时前” 最大为19小时前
     * @param before
     * @return
     */
    public static String getTimeLineForActiveUser(Date before) {
        if (before == null) {
            return "未知";
        }

        Date after = new Date();
        if (after.before(before)) {
            return "1分钟前";
        }

        Calendar calendarBefore = Calendar.getInstance();
        calendarBefore.setTime(before);

        Calendar calendarNow = Calendar.getInstance();
        calendarNow.setTime(after);

        long afterL = after.getTime();
        long beforeL = before.getTime();
        long n = Math.abs(afterL - beforeL);

        n /= 1000;
        long minute = n / 60;

        long hour = minute / 60; // 最终相差小时
        if (hour > 19) {
            hour = 19;
        }

        minute %= 60;  // 最终相差分钟
        if (minute < 1) {
            minute = 1;
        }

        if (hour >= 1) {
            return hour + "小时前";
        } else {
            return minute + "分钟前";
        }
    }

    /**
     * 判断当前时间是否为自然日的“几天内”
     * @param before
     * * @param xday 指定X天前
     * @return 如果指定时间是X天前 或者时间为null时返回false  ，在X天内返回true
     */
    public static boolean inXDayBefore(Date before, int xday) {
        if (before == null) {
            return false;
        }

        // 指定时间比当前时间还要晚
        Date currentDate = new Date();
        if (before.after(currentDate)) {
            return true;
        }

        Calendar now = Calendar.getInstance();
        now.setTime(currentDate);

        Calendar beforeTime = Calendar.getInstance();
        beforeTime.setTime(before);

        int year = now.get(Calendar.YEAR);
        int mounth = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        // 今天的凌晨
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(year, mounth, day, 0, 0);

        Calendar compileDate = Calendar.getInstance();
        compileDate.set(Calendar.DATE, (todayStart.get(Calendar.DATE) - xday));
        //
        return !beforeTime.before(compileDate);

    }

    public static String getTimeLineStringInThreeDays(Date before) {
        if (before == null) {
            return "";
        }

        Date currentDate = new Date();
        if (currentDate.before(before)) {
            return "1分钟前";
        }

        Calendar now = Calendar.getInstance();
        now.setTime(currentDate);

        Calendar beforeTime = Calendar.getInstance();
        beforeTime.setTime(before);

        int year = now.get(Calendar.YEAR);
        int mounth = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        // 今天的凌晨
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(year, mounth, day, 0, 0);

        Calendar compileDate = Calendar.getInstance();
        compileDate.set(Calendar.DATE, (todayStart.get(Calendar.DATE) - 2));
        // 比前天还要早的时间就不显示
        if (beforeTime.before(compileDate)) {
            return "";
        }
        // 比今天凌晨早两天的时间 （前天）
        compileDate.set(Calendar.DATE, (todayStart.get(Calendar.DATE) - 1));
        if (beforeTime.before(compileDate)) {
            return "前天";
        }
        // 比今天凌晨早一天的时间 （昨天）
        if (beforeTime.before(todayStart)) {
            return "昨天";
        }

        long nowL = currentDate.getTime();
        long lastcommentL = before.getTime();
        long n = Math.abs(nowL - lastcommentL);

        n /= 1000;
        long minuteL = n / 60;
        long hourL = minuteL / 60; // 最终相差小时
        minuteL %= 60;  // 最终相差分钟

        if (hourL >= 1) {
            return hourL + "小时前";
        }

        if (minuteL < 1) {
            minuteL = 1;
        }

        return minuteL + "分钟前";

    }

    /**
     * 用一个较靠前的时间对象，和当前时间对比。<br/>
     * 详细规则查看：{@link #between(Date, Date)}
     *
     * @param before
     * @return
     */
    public static String betweenWithCurDate(Date before) {
        return between(before, new Date());
    }

    /**
     * 获取某一天所在的周的第一天(星期一)
     *
     * @param date
     * @return
     */
    public static Date getFirstDayByWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (day == 1) {
            calendar.add(Calendar.DAY_OF_MONTH, -6);
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, -(day - 2));
        }
        return calendar.getTime();
    }

    /**
     * 获取某一天所在的周的最后一天(星期日)
     *
     * @param date
     * @return
     */
    public static Date getLastDayByWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (day != 1) {
            calendar.add(Calendar.DAY_OF_MONTH, 7 - (day - 1));
        }
        return calendar.getTime();
    }

    /**
     * 获取时间对象的时间戳表示，即1970年以来的秒数
     *
     * @param date
     * @return
     */
    public static long formateTimestamp(Date date) {
        return date.getTime() / 1000;
    }

    /**
     * 格式化日期对象为字符串，格式为：yyyyMMddHHmmss
     *
     * @param date
     * @return
     */
    public static String formateDateTime2(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(date);
    }

    /**
     * 格式化日期对象为字符串，格式为：yyyyMMddHHmmssSSS
     *
     * @param date
     * @return
     */
    public static String formateDateTime5(Date date) {
        if (date == null) {
            return "UNKNOWN";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return dateFormat.format(date);
    }

    /**
     * 格式化日期对象为字符串，格式为：yyyyMMdd
     *
     * @param date
     * @return
     */
    public static String formateDateTime11(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(date);
    }

    /**
     * 格式化日期对象为字符串，格式为：HHmmss
     *
     * @param date
     * @return
     */
    public static String formateDateTime12(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss");
        return dateFormat.format(date);
    }

    /**
     * 格式化日期对象为字符串，格式为：MM/dd HH:mm
     *
     * @param date
     * @return
     */
    public static String formateDateTime3(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm");
        return dateFormat.format(date);
    }

    /**
     * 格式化一个日期对象，格式为：yyyy/MM/dd
     *
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(date);
    }

    /**
     * 格式化一个字符串为日期对象，字符串格式为：yyyy/MM/dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String formateDateTime(Date date) {
        if (date == null || date.getTime() == 0) {
            return "未知时间";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy/MM/dd HH:mm:ss");
        return dateFormat.format(date);
    }

    /**
     * 格式化一个字符串为日期对象，字符串格式为：MM/dd HH:mm
     *
     * @param date
     * @return
     */
    public static String formateDateTimeWithOutYear(Date date) {
        if (date == null || date.getTime() == 0) {
            return "未知时间";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "MM/dd HH:mm");
        return dateFormat.format(date);
    }

    /**
     * 格式化一个字符串为日期对象，字符串格式为：yyyy/MM/dd HH:mm
     *
     * @param date
     * @return
     */
    public static String formateDateTime4(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy/MM/dd HH:mm");
        return dateFormat.format(date);
    }

    /**
     * 格式日期，格式为：yyyyMMdd，不包含时间
     *
     * @param date
     *            日期对象
     * @return
     */
    public static String formateDate2(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(date);
    }

    /**
     * 格式日期，格式为：MM/dd，不包含时间
     *
     * @param date
     *            日期对象
     * @return
     */
    public static String formateDate3(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
        return dateFormat.format(date);
    }

    /**
     * 格式时间，格式为：HH:mm，不包含日期
     *
     * @param date
     *            日期对象
     * @return
     */
    public static String formateTime(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(date);
    }

    /**
     * 将一个 yyyy/MM/dd 格式的字符串转换为 Date 对象，忽略时间
     *
     * @param dateStr
     * @return
     */
    public static Date parseStringToDate(String dateStr) {
        if (StringUtils.isEmpty(dateStr)) {
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = null;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            Logger.e(e);
            return null;
        }
        return date;
    }

    /**
     * 将时间戳(秒)字符串转换为 Date 对象
     *
     * @param timestamp
     *            时间戳，单位是“秒”
     * @return
     */
    public static Date parseTimeStampToDate(long timestamp) {
        if (timestamp <= 0) {
            return null;
        }

        Date date = new Date();
        date.setTime(timestamp * 1000); // JAVA的Date对象以毫秒来表示时间戳
        return date;
    }


	/*public static Date parseTimeStampToDate2(long milSeconds){
        if(milSeconds <= 0) {
			return null;
		}

		Date date = new Date();
		date.setTime(milSeconds); // JAVA的Date对象以毫秒来表示时间戳
		return date;
	}*/

    /**
     * 将一个 yyyyMMdd 格式的字符串转换为 Date 对象，忽略时间
     *
     * @param dateStr
     * @return
     */
    public static Date parseStringToDate2(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = null;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            Logger.e(e);
        }
        return date;
    }

    /**
     * 将一个 yyyy/MM/dd HH:mm:ss 格式的字符串转换为 Date 对象，对象必须包含时间细节
     *
     * @param dateStr
     * @return
     */
    public static Date parseStringToDateTime(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy/MM/dd HH:mm:ss");
        Date date = null;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            Logger.e(e);
        }
        return date;
    }

    /**
     * 将一个 yyyyMMddHHmmss 格式的字符串转换为 Date 对象，对象必须包含时间细节
     *
     * @param dateStr
     * @return
     */
    public static Date parseStringToDateTime2(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = null;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            Logger.e(e);
        }
        return date;
    }

    /**
     * 将一个 yyyyMMddHHmmssSSS 格式的字符串转换为 Date 对象，对象必须包含时间细节
     *
     * @param dateStr
     * @return
     */
    public static Date parseStringToDateTime5(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date date = null;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            Logger.e(e);
        }
        return date;
    }

    /**
     * 获取日期所在月的第一天(一号)
     *
     * @param date
     * @return
     */
    public static Date getFirstDayInMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    /**
     * 获取日期所在月的最后一天
     *
     * @param date
     * @return
     */
    public static Date getLastDayInMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, 1);
        calendar.roll(Calendar.DATE, -1);

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int month = c.get(Calendar.MONTH);

        return calendar.getTime();
    }

    /**
     * 获取某一日期的星期表示（星期一为1，星期天为7）
     *
     * @param date
     * @return
     */
    public static int getWeekDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        if (day == 0)
            day = 7;

        return day;
    }

    /**
     * 格式化日期：  XX 月 XX 日
     *
     * @param date
     * @return
     */
    public static String formatDateWithMonthDay(Date date) {
        StringBuilder dateTime = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        dateTime.append(calendar.get(Calendar.MONTH) + 1).append("月");
        dateTime.append(calendar.get(Calendar.DAY_OF_MONTH)).append("日");
        return dateTime.toString();
    }

    /***
     *  星期X 对应的大写格式，from SUNDAY -- index 0
     */
    public static String[] DayOfWeek = {
            "日", "一", "二", "三", "四", "五", "六"
    };

    /**
     * 格式化日期
     *
     * @param date
     * @return 星期 X / 周 X
     */

    public static String getDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (day < 0) {
            day = 0;
        } else if (day >= DayOfWeek.length) {
            day = DayOfWeek.length - 1;
        }
        return DayOfWeek[day];
    }

    /**
     * 获取今天的星期表示（星期一为1，星期天为7）
     *
     * @param data
     * @return
     */
    public static int getWeekDay() {
        Calendar calendar = Calendar.getInstance();

        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        if (day == 0)
            day = 7;

        return day;
    }

    /**
     * 获取今天是几号
     * @return
     */
    public static int getMonthDay() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return day;
    }

    /**
     * 获取昨天的日期
     *
     * @param date
     * @return
     */
    public static Date getYesterday(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);

        return calendar.getTime();
    }

    /**
     * 明天
     *
     * @param date
     * @return
     */
    public static Date getTomorrow(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);
        return calendar.getTime();
    }

    /**
     * 某天的开始时间，即零点
     *
     * @param date
     * @return
     */
    public static Date getDateOfBegin(Date date) {
        String dateStr = formatDate(date);

        dateStr += " 00:00:00";

        return parseStringToDateTime(dateStr);
    }

    /**
     * 某天的结束时间，即第二天的零点
     *
     * @param date
     * @return
     */
    public static Date getDateOfEnd(Date date) {
        String dateStr = formatDate(getTomorrow(date));

        dateStr += " 00:00:00";

        return parseStringToDateTime(dateStr);
    }

    public static boolean isDrinkingTime() {
        int december = Calendar.getInstance().get(Calendar.DECEMBER);
        return (december >= 1) && (december < 5);
    }

    /**
     * 计算年龄。如果生日在当前系统时间之前，则返回-1
     * @param birthDay
     * @return
     */
    public static int getAge(Date birthDay) {
        Calendar cal = Calendar.getInstance();

        if (cal.before(birthDay)) {
            return -1;
        }

        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH);
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);

        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;

        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) {
                    age--;
                }
            } else {
                age--;
            }
        } else {
        }

        return age;
    }

    public static Date getServerDate() {
        Calendar c = Calendar.getInstance();
        try {
            URL url = new URL("http://www.beijing-time.org/time.asp");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.connect();
            InputStream is = conn.getInputStream();
            Properties properties = new Properties();
            properties.load(is);
            is.close();

            c.set(Calendar.YEAR, Integer.valueOf(((String) (properties.get("nyear"))).replace(";", "")));
            c.set(Calendar.MONTH, Integer.valueOf(((String) (properties.get("nmonth"))).replace(";", "")) - 1);
            c.set(Calendar.DATE, Integer.valueOf(((String) (properties.get("nday"))).replace(";", "")) - 1);
            c.set(Calendar.HOUR, Integer.valueOf(((String) (properties.get("nhrs"))).replace(";", "")));
            c.set(Calendar.MINUTE, Integer.valueOf(((String) (properties.get("nmin"))).replace(";", "")));
            c.set(Calendar.SECOND, Integer.valueOf(((String) (properties.get("nsec"))).replace(";", "")));

        } catch (Throwable e) {
            Logger.e(e);
        }

        return c.getTime();
    }

    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        if (date1.getYear() != date2.getYear()) {
            return false;
        }

        if (date1.getDate() != date2.getDate()) {
            return false;
        }

        return date1.getMonth() == date2.getMonth();

    }

    /**
     * 算时间差函数
     * @param start
     * @param end
     * @return 返回格式 String = hh:mm:ss
     */
    public static String getBetween(long start, long end) {
        int hour = 0, minute = 0, second = 0;
        if (start > end) {
            return "00:00:00";
        }
        long between = end - start;
        //从毫秒转变成秒
        //        between = between / 1000;
        second = (int) between % 60;
        minute = ((int) between / 60) % 60;
        hour = ((int) between / 3600);
        StringBuffer sb = new StringBuffer();
        sb.append(hour < 10 ? "0" + hour : String.valueOf(hour));
        sb.append(":").append(minute < 10 ? "0" + minute : String.valueOf(minute));
        sb.append(":").append(second < 10 ? "0" + second : String.valueOf(second));
        return sb.toString();
    }

    public static boolean isSameMonth(Date date1, Date date2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(date1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(date2);
        if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR))
            return false;
        return c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH);
    }

    public static boolean isSameWeek(Date date1, Date date2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(date1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(date2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR);
    }

    public static int delta(long time1, long time2, int calenderField) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(time1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(time2);
        return calendar1.get(calenderField) - calendar2.get(calenderField);
    }

    public static int dayDelta(long time1, long time2) {
        return delta(time1, time2, Calendar.DAY_OF_YEAR);
    }

    public static boolean isSameDay(long time1, long time2) {
        return dayDelta(time1, time2) == 0;
    }

    public static String formatTime(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        return dateFormat.format(new Date(time));
    }
}
