package me.stevenkin.boom.job.processor.job;

import com.alibaba.dubbo.config.annotation.Reference;
import me.stevenkin.boom.job.common.dto.JobContext;
import me.stevenkin.boom.job.common.dto.JobResult;
import me.stevenkin.boom.job.common.service.JobAdminService;
import me.stevenkin.boom.job.common.service.JobExecuteService;
import me.stevenkin.boom.job.common.service.JobInstanceAdminService;
import me.stevenkin.boom.job.common.zk.ZkClient;
import me.stevenkin.boom.job.processor.annotation.BoomJob;
import me.stevenkin.boom.job.processor.core.Job;
import org.springframework.beans.factory.annotation.Autowired;

@BoomJob
public class PlanJob implements Job {
    @Reference
    private JobAdminService jobAdminService;
    @Reference
    private JobExecuteService jobExecuteService;
    @Reference
    private JobInstanceAdminService jobInstanceAdminService;
    @Autowired
    private ZkClient zkClient;

    @Override
    public JobResult execute(JobContext jobContext) throws Throwable {
        return null;
    }
}
