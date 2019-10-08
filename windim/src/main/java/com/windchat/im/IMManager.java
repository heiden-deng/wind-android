package com.windchat.im;

import com.windchat.im.bean.Site;
import com.windchat.im.socket.SiteAddress;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by anguoyue on 2019/10/6.
 */

public class IMManager {

    private static String TAG = IMManager.class.getSimpleName();
    /**
     * 多站点，使用MAP存放多个IM连接
     */
    private static ConcurrentHashMap<String, IMClient> connectionPool = new ConcurrentHashMap<>();


    public static IMClient get(Site site) {
        String fullUrl = site.getSiteAddress().getFullUrl();
        synchronized (TAG) {
            IMClient client;
            if (connectionPool.containsKey(fullUrl)) {
                client = connectionPool.get(fullUrl);
            } else {
                client = new IMClient(site);
                connectionPool.put(fullUrl, client);
            }
            return client;
        }
    }

    /**
     * 删除某个Client
     *
     * @param site
     */
    public static void remove(Site site) {
        synchronized (TAG) {
            IMClient client = null;
            if (connectionPool.containsKey(site.getHostAndPort())) {
                client = connectionPool.get(site.getHostAndPort());
            }
            if (client != null) {
                client.disconnect();
            }
            connectionPool.remove(site.getHostAndPort());
        }
    }
}
