package me.stevenkin.boom.job.common.job;

import me.stevenkin.boom.job.common.bean.JobExecReport;

public interface JobExecReportService {
    void reportJobExecResult(JobExecReport jobExecReport);
}
