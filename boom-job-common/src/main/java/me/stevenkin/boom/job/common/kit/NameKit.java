package me.stevenkin.boom.job.common.kit;

import com.google.common.base.Joiner;

public class NameKit {
    public static String genName(String... names) {
        return Joiner.on("_").join(names);
    }

    public static String getAppId(String appName, String author, String version) {
        return genName(author, appName, version);
    }

    public static String getJobId(String appName, String author, String version, String jobClassName) {
        return genName(author, appName, version, jobClassName);
    }
}
