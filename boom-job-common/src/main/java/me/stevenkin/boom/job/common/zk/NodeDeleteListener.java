package me.stevenkin.boom.job.common.zk;

@FunctionalInterface
public interface NodeDeleteListener {

    void onDelete(String path);

}
