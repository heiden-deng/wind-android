package com.akaxin.client.util.data;


import android.net.Uri;

import com.akaxin.client.Configs;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.orhanobut.logger.Logger;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

/**
 * 字符串处理类
 */
public class StringUtils {
    private static final char[] numChars = {'一', '二', '三', '四', '五', '六', '七', '八', '九'};
    private static final String[] units = {"千", "百", "十", ""};// 个位
    private static final String[] bigUnits = {"万", "亿"};
    private static char numZero = '零';
    private static Pattern niceMomoidPattern = Pattern.compile("^[0-9a-zA-Z]+$");
    public static final String TEST_REGEX_PHONDID = "^(20210000[0-9])\\d{2}$";

    /**
     * 判断字符串是否为空
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }


    /**
     * 获取pubkey对应的global user id
     *
     * @param body
     * @return
     */
    public static String getGlobalUserIdHash(String body) {
        String SHA1UserPubKey = new String(Hex.encodeHex(DigestUtils.sha1(body)));
        CRC32 c32 = new CRC32();
        c32.update(body.getBytes(), 0, body.getBytes().length);
        String CRC32UserPubKey = String.valueOf(c32.getValue());
        return SHA1UserPubKey + "-" + CRC32UserPubKey;
    }

    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    /**
     * 判断是否是空白string
     */
    public static boolean isBlank(final CharSequence str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(str.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    public static boolean isAlpha(String str) {
        return str.matches("[a-zA-Z]+");
    }

    /**
     * 判断是否全是数字
     * 不要使用{@link android.text.TextUtils#isDigitsOnly(CharSequence)}
     *
     * @return str为null或者length为0或者存在非数字字符, 返回false
     */
    public static boolean isDigitsOnly(CharSequence str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return false;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNiceMomoid(CharSequence str) {
        if (str == null) return false;
        Matcher matcher = niceMomoidPattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 判断字符串是否不为空
     *
     * @param str
     * @return
     */
    public static boolean notEmpty(CharSequence str) {
        return !isEmpty(str);
    }


    /**
     * 将字符串转换成字符串组数,按照指定的标记进行转换
     */
    public static String[] str2Arr(String value, String tag) {
        if (!isEmpty(value)) {
            return value.split(tag);
        }
        return null;
    }

    /**
     * 将一个字符串数组组合成一个以指定分割符分割的字符串
     */
    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, "", 0, array.length);
    }

    /**
     * 将一个字符串数组组合成一个以指定分割符分割的字符串
     */
    public static String join(Object[] array, String ch, String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, ch, 0, array.length);
    }

    /**
     * 将一个字符串数组的某一部分组合成一个以指定分割符分割的字符串
     */
    public static String join(Object[] array, String separator, String ch, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = "";
        }

        // 开始位置大于结束位置
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return "";
        }

        bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length()) + separator.length());

        StringBuffer buf = new StringBuffer(bufSize);

        for (int i = startIndex; i < endIndex; i++) {
            if (array[i] != null) {
                if (i > startIndex) {
                    buf.append(separator);
                }
                buf.append(ch + array[i] + ch);
            }
        }
        return buf.toString();
    }

    /**
     * 将一个集合组合成以指定分割符分割的字符串
     */
    public static String join(Collection collection, String separator) {
        if (collection == null) {
            return "";
        }
        return join(collection.iterator(), separator);
    }

    public boolean isIp(String IP) {//判断是否是一个IP
        boolean b = false;
        if (IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            String s[] = IP.split("\\.");
            if (Integer.parseInt(s[0]) < 255)
                if (Integer.parseInt(s[1]) < 255)
                    if (Integer.parseInt(s[2]) < 255)
                        if (Integer.parseInt(s[3]) < 255)
                            b = true;
        }
        return b;
    }

    /**
     * 根据迭代器，迭代的元素将组合成以指定分割符分割的字符串
     */
    public static String join(Iterator iterator, String separator) {

        // 空的迭代器，返回 null
        if (iterator == null) {
            return null;
        }
        // 空元素，返回 null
        if (!iterator.hasNext()) {
            return "";
        }

        Object first = iterator.next();
        // 只有一个元素
        if (!iterator.hasNext()) {
            if (first != null) {
                return first.toString();
            } else {
                return "";
            }
        }

        StringBuffer buf = new StringBuffer(256);
        if (first != null) {
            buf.append(first);
        }

        while (iterator.hasNext()) {
            if (separator != null) {
                buf.append(separator);
            }
            Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }

        return buf.toString();
    }

    /**
     * 将集合元素转换成字符串："'qq','aa','cc'"
     */
    public static String Conll2StringWithSingleGuotes(@SuppressWarnings("rawtypes") Collection collection, String separator) {
        if (collection == null) {
            return null;
        }
        Iterator iterator = collection.iterator();

        // 空的迭代器，返回 null
        if (iterator == null) {
            return null;
        }
        // 空元素，返回 null
        if (!iterator.hasNext()) {
            return "";
        }

        Object first = iterator.next();
        // 只有一个元素
        if (!iterator.hasNext()) {
            if (first != null) {
                return "'" + first.toString() + "'";
            } else {
                return "";
            }
        }

        StringBuffer buf = new StringBuffer(256);
        if (first != null) {
            buf.append("'" + first + "'");
        }

        while (iterator.hasNext()) {
            if (separator != null) {
                buf.append(separator);
            }
            Object obj = iterator.next();
            if (obj != null) {
                buf.append("'" + obj + "'");
            }
        }

        return buf.toString();
    }

    public static String escapeUnicode(String src) {
        int i;
        char j;
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length() * 6);
        for (i = 0; i < src.length(); i++) {
            j = src.charAt(i);
            if (Character.isDigit(j) || Character.isLowerCase(j) || Character.isUpperCase(j))
                tmp.append(j);
            else if (j < 256) {
                tmp.append("%");
                if (j < 16)
                    tmp.append("0");
                tmp.append(Integer.toString(j, 16));
            } else {
                tmp.append("%u");
                tmp.append(Integer.toString(j, 16));
            }
        }
        return tmp.toString();
    }

    public static String unescapeUnicode(String src) {
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length());
        int lastPos = 0, pos = 0;
        char ch;
        while (lastPos < src.length()) {
            pos = src.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (src.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else {
                if (pos == -1) {
                    tmp.append(src.substring(lastPos));
                    lastPos = src.length();
                } else {
                    tmp.append(src.substring(lastPos, pos));
                    lastPos = pos;
                }
            }
        }
        return tmp.toString();
    }

    private static final int HOURS_OF_DAY = 24;
    private static final int MINUTES_OF_HOUR = 60;
    private static final int SECONDS_OF_MINUTE = 60;

    private static long CalPassedDay(long totalSec) {
        long result = totalSec / (HOURS_OF_DAY * MINUTES_OF_HOUR * SECONDS_OF_MINUTE);
        return result;
    }

    private static long CalPassedHour(long totalSec) {
        long result = (totalSec % (HOURS_OF_DAY * MINUTES_OF_HOUR * SECONDS_OF_MINUTE)) / (MINUTES_OF_HOUR * SECONDS_OF_MINUTE);
        return result;
    }

    private static long CalPassedMin(long totalSec) {
        long result = ((totalSec % (HOURS_OF_DAY * MINUTES_OF_HOUR * SECONDS_OF_MINUTE)) % (MINUTES_OF_HOUR * SECONDS_OF_MINUTE)) / SECONDS_OF_MINUTE;
        return result;
    }

    private static long CalPassedSec(long totalSec) {
        long result = ((totalSec % (HOURS_OF_DAY * MINUTES_OF_HOUR * SECONDS_OF_MINUTE)) % (MINUTES_OF_HOUR * SECONDS_OF_MINUTE)) % SECONDS_OF_MINUTE;
        return result;
    }

    /**
     * 根据传入的时间戳返回时间
     *
     * @return
     */
    public static String TimeStampToTime(final long creat_stmap, final long server_stamp) {
        String result = "";
        long passedTotalSeconds = server_stamp - creat_stmap;
        if (passedTotalSeconds <= 0) {
            result = "1秒钟前";
            return result;
        }
        long passedDay = CalPassedDay(passedTotalSeconds);
        long passedHour = CalPassedHour(passedTotalSeconds);
        long passedMin = CalPassedMin(passedTotalSeconds);
        long passedSec = CalPassedSec(passedTotalSeconds);
        if (passedDay > 0) {
            result = new Long(passedDay).toString() + "天前";
            return result;
        }
        if (passedHour > 0) {
            result = new Long(passedHour).toString() + "小时前";
            return result;
        }
        if (passedMin > 0) {
            result = new Long(passedMin).toString() + "分钟前";
            return result;
        }
        if (passedSec > 0) {
            result = new Long(passedSec).toString() + "秒前";
            return result;
        }
        return result;
    }

    public static String md5(String s) {
        if (isEmpty(s)) {
            return "";
        }
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
        }
        return s;
    }

    public static String sha256(String orignal) {
        if (isEmpty(orignal)) {
            return orignal;
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Logger.e(e);
        }

        if (null != md) {
            byte[] oriBytes = orignal.getBytes();
            md.update(oriBytes);
            byte[] digestRes = md.digest();
            String digestStr = getDigestStr(digestRes);
            return digestStr;
        }

        return "";
    }

    private static String getDigestStr(byte[] origBytes) {
        String tempStr = null;
        StringBuilder stb = new StringBuilder();
        for (int i = 0; i < origBytes.length; i++) {
            tempStr = Integer.toHexString(origBytes[i] & 0xff);
            if (tempStr.length() == 1) {
                stb.append("0");
            }

            stb.append(tempStr);
        }

        return stb.toString();
    }

    public static byte[] md5(byte[] bytes) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(bytes);
            byte messageDigest[] = digest.digest();
            return messageDigest;
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }

    public static byte[] hash256(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(str.getBytes());
            return md.digest();
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] hash256(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes);
            return md.digest();
        } catch (Exception e) {
            return null;
        }
    }

//    /**
//     * 精简字符串（去除表情字符、空格、转小写字母）
//     *
//     * @param string
//     * @return
//     */
//    public static String makeClean(String string) {
//        if (StringUtils.isEmpty(string)) {
//            return "";
//        }
//
//        return string.trim().replaceAll("([\ue000-\ue5ff])", "").replaceAll(" ", "").replaceAll(MomoEmotionUtil.getEmotesRegex(), "")
//                     .replaceAll(MomoEmotionUtil.getOldEmoteRegex(), "").toLowerCase();
//    }

    public static String trimBlank(String string) {
        if (StringUtils.isEmpty(string)) {
            return "";
        }

        return string.trim().replaceAll(" ", "");
    }

    public static String removeEmoji(String string) {
        if (StringUtils.isEmpty(string)) {
            return "";
        }
        return string.trim().replaceAll("([\ue000-\ue5ff])", "");
    }

    /**
     * 一个字符串是否包含另一字符串
     *
     * @param str1
     * @param str2
     * @return
     */
    public static boolean contains(String str1, String str2) {
        if (isEmpty(str1) && isEmpty(str2)) {
            return true;
        } else if (isEmpty(str1)) {
            return false;
        } else if (isEmpty(str2)) {
            return false;
        } else {
            return (str1).contains(str2) || (str2).contains((str1));
        }
    }

    /**
     * 一个字符串是否包含另一字符串，忽略大小写
     *
     * @param str1
     * @param str2
     * @return
     */
    public static boolean containsIgnoreCase(String str1, String str2) {
        if (isEmpty(str1) && isEmpty(str2)) {
            return true;
        } else if (isEmpty(str1)) {
            return false;
        } else if (isEmpty(str2)) {
            return false;
        } else {
            str1 = str1.toLowerCase();
            str2 = str2.toLowerCase();
            return (str1).contains(str2) || (str2).contains((str1));
        }
    }

    public static boolean isPhone(String phone) {
        if (isTestPhoneId(phone)) {
            return true;
        }

        String regex = "^1([358][0-9]|4[579]|66|7[0135678]|9[89])[0-9]{8}$";
        if (phone.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(phone);
            boolean isMatch = m.matches();
            return isMatch;
        }
    }

    // 是否为测试手机号格式
    public static boolean isTestPhoneId(String phoneId) {
        if (phoneId == null) {
            return false;
        }
        return Pattern.matches(TEST_REGEX_PHONDID, phoneId);
    }

    /**
     * 格式化手机号码 186-8134-1780
     *
     * @param phoneNumber
     * @param split       指定的分隔符
     * @return
     */

    public static String formatPhoneNumber(CharSequence phoneNumber, String split) {
        if (StringUtils.isEmpty(phoneNumber)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < phoneNumber.length(); i++) {
            sb.append(phoneNumber.charAt(i));
            if (split.equals(String.valueOf(phoneNumber.charAt(i))) || i == phoneNumber.length() - 1) {
                continue;
            }

            if (i == 2) {
                sb.append(split);
            } else if ((i - 2) % 4 == 0) {
                sb.append(split);
            }
        }
        return sb.toString();
    }

    /**
     * 一个字符串是以另一字符串开头
     *
     * @param str1
     * @param str2
     * @return
     */
    public static boolean startsWith(String str1, String str2) {
        if (isEmpty(str1) && isEmpty(str2)) {
            return true;
        } else if (isEmpty(str1)) {
            return false;
        } else if (isEmpty(str2)) {
            return false;
        } else {
            return str1.startsWith(str2) || str2.startsWith(str1);
        }
    }

    /**
     * 字符串替换，从头到尾查询一次，替换后的字符串不检查
     *
     * @param str    源字符串
     * @param oldStr 目标字符串
     * @param newStr 替换字符串
     * @return 替换后的字符串
     */
    public static String replaceAll(String str, String oldStr, String newStr) {
        int i = str.indexOf(oldStr);
        while (i != -1) {
            str = str.substring(0, i) + newStr + str.substring(i + oldStr.length());
            i = str.indexOf(oldStr, i + newStr.length());
        }
        return str;
    }

    /**
     * 将一位数字转换为一位中文数字,只支持0~10000;
     *
     * @return
     * @throws Exception
     */
    public static String numberKArab2CN(Integer num) throws Exception {
        if (num < 0 || num > 10000) {
            throw new Exception("数字超出支持范围");
        }

        char[] numChars = (num + "").toCharArray();

        String tempStr = "";

        int inc = units.length - numChars.length;

        for (int i = 0; i < numChars.length; i++) {
            if (numChars[i] != '0') {
                tempStr += numberCharArab2CN(numChars[i]) + units[i + inc];
            } else {
                tempStr += numberCharArab2CN(numChars[i]);
            }
        }

        // 将连续的零保留一个
        tempStr = tempStr.replaceAll(numZero + "+", numZero + "");

        // 去掉未位的零
        tempStr = tempStr.replaceAll(numZero + "$", "");

        return tempStr;

    }

    private static char numberCharArab2CN(char onlyArabNumber) {

        if (onlyArabNumber == '0') {
            return numZero;
        }

        if (onlyArabNumber > '0' && onlyArabNumber <= '9') {
            return numChars[onlyArabNumber - '0' - 1];
        }

        return onlyArabNumber;
    }

    /**
     * Getting file name from url without extension
     *
     * @param url string
     * @return file name
     */
    public static String getFileName(String url) {
        if (isEmpty(url)) {
            return "";
        }
        String fileName;
        int slashIndex = url.lastIndexOf("/");
        int qIndex = url.lastIndexOf("?");
        if (qIndex > slashIndex) {//if has parameters
            fileName = url.substring(slashIndex + 1, qIndex);
        } else {
            fileName = url.substring(slashIndex + 1);
        }
        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

    /**
     * 是否为数字或字母
     *
     * @param c 目标char
     * @return true:表示目标char为数字或者字母； false:表示不是
     */
    public static boolean isNumberOrLetter(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /**
     * 字符串是否为英文
     *
     * @param s
     * @return
     */
    public static boolean isWordOnly(String s) {
        if (null == s) return false;
        return s.matches("[a-zA-Z]+");
    }

    /**
     * 字符串为英文数字
     *
     * @param s
     * @return
     */
    public static boolean isWordOrNumOnly(String s) {
        if (null == s) return false;
        return s.matches("[a-zA-Z0-9]+");
    }

    /**
     * 这个方法目的是为了取出输入的片段拼音对应的汉字，如，王端晴-wangduanqing，输入gdu取出王端，输入duanq取出端晴
     *
     * @param pinyinIndexArr 拼音对应的索引位置，如wangduanqing，这个字段存储为"0,4,7"
     * @param pinyinContent  拼音内容，即 wangduanqing
     * @param hanContent     汉字内容，即 王端晴
     * @param inputKey       输入的key，如，duanq,wan,duanqing等等
     * @return
     */
    public static String subStringInHan(String pinyinIndexArr, String pinyinContent, String hanContent, String inputKey) {
        String arr[] = StringUtils.str2Arr(pinyinIndexArr, ",");
        //输入的拼音在整个拼音全拼里的位置
        int start = pinyinContent.indexOf(inputKey);
        int end = start + inputKey.length();
        //输入的拼音对应汉字的开始和结束位置
        int hanStartIndex = 0;
        int hanEndIndex = 0;
        for (String indexStr : arr) {
            //一次循环找到汉字位置
            int number = Integer.parseInt(indexStr);
            if (number != 0) {
                if (start >= number) {
                    hanStartIndex += 1;
                }
                if (end <= number) {
                    hanEndIndex += 1;
                    break;
                } else {
                    hanEndIndex += 1;
                }
            }
        }
        int length = hanContent.length();
        if (hanStartIndex >= 0 && hanEndIndex <= length) {
            String desKey = hanContent.substring(hanStartIndex, hanEndIndex);
            return desKey;
        }
        return "";
    }

    /**
     * 是否为合法的输入拼音，比如，wang，这是合法的，ng这是不合法的
     *
     * @return
     */
    public static boolean isValidInputPinyin(String pinyinIndexArr, String pinyinContent, String inputKey) {
        String arr[] = StringUtils.str2Arr(pinyinIndexArr, ",");
        int start = pinyinContent.indexOf(inputKey);
        for (String indexStr : arr) {
            int number = Integer.parseInt(indexStr);
            if (number == start) {
                return true;
            } else if (number > start) {
                break;
            }
        }
        return false;
    }

    /**
     * 判断字符串是否是以http或https开始
     *
     * @param inputStr
     * @return
     */
    public static boolean isStartWithHttpOrHttps(String inputStr) {
        return isNotEmpty(inputStr) && (inputStr.startsWith("http://") || inputStr.startsWith("https://"));
    }

    /**
     * 获取字符串中第一个中文字符串
     * 比如截取goto中的描述：[附近群组|goto_grouplist|]
     *
     * @param content 待检查字符串
     * @return 中文字符串，没匹配到返回“”
     */
    public static String getChineseStr(String content) {
        String result = "";
        if (isNotBlank(content)) {
            Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]+");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                result = matcher.group();
            }
        }
        return result;
    }

    /**
     * 获取字符串中的中文字符集合
     *
     * @param content
     * @return
     */
    public static List<String> getChineseStrs(String content) {
        List<String> result = new ArrayList<>();
        if (isNotBlank(content)) {
            Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]+");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                for (int i = 0, size = matcher.groupCount(); i < size; i++) {
                    result.add(matcher.group(i));
                }
            }
        }
        return result;
    }

    public static String getBubbleString(int num) {
        if (num > 99) return "99+";
        else return String.valueOf(num);
    }

    public static String getSiteSubTitle(Site site) {
        try {
            String subTitle = site.getSiteName() + " | " + site.getSiteHost();
            if (site.getSitePort() != 2021) subTitle += (":" + site.getSitePort());
            return subTitle;
        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
            return "";
        }
    }

    public static String hidePhoneNumber(String phoneNumber) {
        if (phoneNumber.length() >= 7) {
            return phoneNumber.replaceFirst(phoneNumber.substring(3, 7), "****");
        }
        return phoneNumber;
    }

    public static int getInfoForFileId(String fileId, int index) {
        if (!fileId.isEmpty() && StringUtils.contains(fileId, "_")) {
            String[] fileInfo = fileId.split("_");
            if (index > fileInfo.length) {
                return 0;
            }
            try {
                return Integer.parseInt(fileInfo[index]);
            } catch (NumberFormatException nfe) {
                return 0;
            }
        }
        return 0;
    }

    public static Map<String, String> getParamsFromUrl(String url) {
        Uri uri = Uri.parse(url);
        String uriScheme = uri.getScheme();
        for (String scheme : SiteConfig.SCHEMES) {
            if (scheme.equals(uriScheme)) {
                Set<String> paramNames = uri.getQueryParameterNames();
                HashMap<String, String> params = new HashMap<>();
                for (String paramName : paramNames) {
                    params.put(paramName, uri.getQueryParameter(paramName));
                }
                return params;
            }
        }
        return null;
    }

    public static String getSiteAddress(String url, Site site) {
        Uri uri = Uri.parse(url);
        //拿前面标签 如zaly
        String uriScheme = uri.getScheme();
        for (String scheme : SiteConfig.SCHEMES) {
            if (scheme.equals(uriScheme)) {
                String host = uri.getHost();
                if (host.equals(Configs.LOCAL_SITE_DEFAULT_MARK)) {
                    host = site.getSiteHost();
                }
                int port = uri.getPort();
                if (port < 1) {
                    port = Integer.valueOf(SiteConfig.SITE_PROT);
                }
                return host + ":" + port;
            }
        }
        return "";
    }

    public static boolean isUrlHostEqualPoint(String url) {
        Uri uri = Uri.parse(url);
        String uriScheme = uri.getScheme();
        for (String scheme : SiteConfig.SCHEMES) {
            if (scheme.equals(uriScheme)) {
                String host = uri.getHost();
                if (host.equals(Configs.LOCAL_SITE_DEFAULT_MARK)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String changeReferer(String url) {
        url = url.replace(SiteConfig.ZALY_SCHEME, SiteConfig.HTTP_SCHEME);
        url = url.replace(SiteConfig.ZALYS_SCHEME, SiteConfig.HTTPS_SCHEME);
        return url;
    }
}
