package com.windchat.client.util;

import com.windchat.client.util.data.StringUtils;
import com.akaxin.proto.core.UserProto;

import java.util.Comparator;

/**
 * Created by alexfan on 2018/3/29.
 */


public class SimpleUserProfileComparator implements Comparator<SimpleUserProfileComparator.Temp> {

    public static final String TAG = SimpleUserProfileComparator.class.getSimpleName();

    @Override
    public int compare(SimpleUserProfileComparator.Temp p1, SimpleUserProfileComparator.Temp p2) {
        if (p1 == null && p2 == null)
            return 0;
        if (p1 == null)
            return 1;
        if (p2 == null)
            return -1;
        return compareObj(p1, p2);
    }

    public static int compareObj(Temp t1, Temp t2) {
        int o1Length = t1.getPinYin().length();
        int o2Length = t2.getPinYin().length();
        for (int i = 0; i < o1Length && i < o2Length; i++) {
            char codePoint1 = t1.getPinYin().toUpperCase().charAt(i);
            char codePoint2 = t2.getPinYin().toUpperCase().charAt(i);
            int codePointASCII1 = (int) codePoint1;
            int codePointASCII2 = (int) codePoint2;


            if (i == 0) {
                if (!StringUtils.isAlpha(t1.getPinYin().substring(0, 1).toUpperCase()) &&
                        !StringUtils.isAlpha(t2.getPinYin().substring(0, 1).toUpperCase()))
                    return 0;
                if (!StringUtils.isAlpha(t1.getPinYin().substring(0, 1).toUpperCase()))
                    return 1;
                if (!StringUtils.isAlpha(t2.getPinYin().substring(0, 1).toUpperCase()))
                    return -1;
            }

            if (codePointASCII1 == codePointASCII2) {
                return 0;
            }
            if (codePointASCII1 > codePointASCII2)
                return 1;
            if (codePointASCII1 < codePointASCII2)
                return -1;
        }
        return 0;
    }


    public static class Temp {
        private String pinYin;
        private UserProto.SimpleUserProfile userProfile;

        public Temp(UserProto.SimpleUserProfile userProfile) {
            this.userProfile = userProfile;
            if (userProfile.getUsernameInLatin() == null || userProfile.getUsernameInLatin().length() < 1) {
                this.setPinYin(ChineseUtils.getPingYin(userProfile.getUserName()));
            } else {
                this.setPinYin(userProfile.getUsernameInLatin());
            }
        }

        public String getPinYin() {
            if (!StringUtils.isEmpty(pinYin))
                return pinYin;
            return "1";
        }


        public void setPinYin(String pinYin) {
            this.pinYin = pinYin;
        }


        public UserProto.SimpleUserProfile getUserProfile() {
            return userProfile;
        }

    }


}
