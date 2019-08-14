package me.stevenkin.boom.job.common.kit;

import java.lang.management.ManagementFactory;

public class SystemKit {
    private static final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

    private static final String HOST_PID = NetworkKit.getSiteIp() + ":" + PID;

    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();

    public static String pid() {
        return PID;
    }

    public static int cpuNum(){
        return CPU_CORES;
    }

    public static String hostPid(){
        return HOST_PID;
    }
}
