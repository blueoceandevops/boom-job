package me.stevenkin.boom.job.common.zk;

import me.stevenkin.boom.job.common.support.Lifecycle;

public class CommandProcessor extends Lifecycle {
    private ZkClient zkClient;

    private String commandPath;

    private String id;

    public CommandProcessor(ZkClient zkClient, String commandPath, String id) {
        this.zkClient = zkClient;
        this.commandPath = commandPath;
        this.id = id;
    }

    @Override
    public void doStart() throws Exception {

    }

    @Override
    public void doPause() throws Exception {

    }

    @Override
    public void doResume() throws Exception {

    }

    @Override
    public void doShutdown() throws Exception {

    }
}
