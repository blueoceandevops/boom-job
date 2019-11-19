package me.stevenkin.boom.job.processor.job;

import me.stevenkin.boom.job.common.dto.JobContext;
import me.stevenkin.boom.job.common.dto.JobResult;
import me.stevenkin.boom.job.common.service.JobPlanExecuteService;
import me.stevenkin.boom.job.common.zk.ZkClient;
import me.stevenkin.boom.job.processor.core.Job;

public class PlanJob implements Job {
    private JobPlanExecuteService jobPlanExecuteService;

    private ZkClient zkClient;

    public PlanJob(JobPlanExecuteService jobPlanExecuteService, ZkClient zkClient) {
        this.jobPlanExecuteService = jobPlanExecuteService;
        this.zkClient = zkClient;
    }

    @Override
    public JobResult execute(JobContext jobContext) throws Throwable {
        return null;
    }
}
