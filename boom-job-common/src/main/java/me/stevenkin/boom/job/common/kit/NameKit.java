package me.stevenkin.boom.job.common.kit;

import com.google.common.base.Joiner;

public class NameKit {
    public static String genName(String... names) {
        return Joiner.on(".").join(names);
    }

    public static String getAppId(String appName, String author) {
        return genName(author, appName);
    }

    public static String getJobId(String appName, String author, String jobClassName) {
        return genName(author, appName, jobClassName);
    }
}