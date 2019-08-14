package me.stevenkin.boom.job.processor.core;

import me.stevenkin.boom.job.common.bean.JobContext;
import me.stevenkin.boom.job.common.bean.JobResult;

public interface Job {
    /**
     * core job execute interface,overwrite job business logic in this method
     * @param jobContext
     * @return JobResult
     * @throws Throwable
     */
    JobResult execute(JobContext jobContext) throws Throwable;

}
