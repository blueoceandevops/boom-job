package me.stevenkin.boom.job.common.kit;

import me.stevenkin.boom.job.common.exception.ZKConnectException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.EnsurePath;

public class ZkKit {

    public static boolean mkdir(String dir, CuratorFramework framework) {
        try {
            EnsurePath clientAppPathExist =
                    new EnsurePath(dir);
            clientAppPathExist.ensure(framework.getZookeeperClient());
            return Boolean.TRUE;
        } catch (Exception e) {
            throw new ZKConnectException(e);
        }
    }
}
