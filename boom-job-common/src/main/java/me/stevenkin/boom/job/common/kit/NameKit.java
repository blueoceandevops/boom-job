package me.stevenkin.boom.job.common.kit;

import com.alibaba.dubbo.common.URL;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;

public class NameKit {
    public static String genName(String... names) {
        return Joiner.on("_").join(names);
    }

    public static String getAppKey(String appName, String author) {
        return genName(author, appName);
    }

    public static String getJobKey(String appName, String author, String jobClassName) {
        return genName(author, appName, jobClassName);
    }

    public static String getNodeId(String url) {
        URL url1 = URL.valueOf(url);
        String timestamp = url1.getParameter("timestamp");
        if (StringUtils.isEmpty(timestamp)) {
            return null;
        }
        Integer.parseInt(timestamp);
        return url1.getHost() + "_" + url1.getPort() + "_" + timestamp;
    }
}
