package me.stevenkin.boom.job.scheduler.service;

import me.stevenkin.boom.job.common.bean.JobInfo;
import me.stevenkin.boom.job.common.kit.NameKit;
import me.stevenkin.boom.job.common.kit.PathKit;
import me.stevenkin.boom.job.common.zk.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZkService {
    private final static String JOB = "job";
    @Autowired
    private ZkClient zkClient;

    public Boolean createJob(JobInfo jobInfo) {
        String jobPath = PathKit.format(JOB,
                NameKit.getAppId(jobInfo.getAppName(), jobInfo.getAuthor(), jobInfo.getVersion()),
                jobInfo.getJobClassName());
        return zkClient.mkdirs(jobPath);
    }
}
