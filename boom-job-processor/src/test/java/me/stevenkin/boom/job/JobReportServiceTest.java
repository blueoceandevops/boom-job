package me.stevenkin.boom.job;

import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.bean.JobExecReport;
import me.stevenkin.boom.job.common.job.JobExecReportService;

@Slf4j
public class JobReportServiceTest implements JobExecReportService {
    @Override
    public void reportJobExecResult(JobExecReport jobExecReport) {
        log.info(jobExecReport.toString());
    }
}
