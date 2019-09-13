package me.stevenkin.boom.job.test.scheduler;

import me.stevenkin.boom.job.common.dto.JobExecReport;
import me.stevenkin.boom.job.common.dto.JobResult;
import me.stevenkin.boom.job.common.po.JobShardExecuteLog;
import me.stevenkin.boom.job.scheduler.SchedulerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

public class BeanCopyTest {
    @Test
    public void test() {
        JobExecReport report = new JobExecReport();
        JobShardExecuteLog log = new JobShardExecuteLog();
        report.setJobKey("jobKey");
        report.setJobResult(JobResult.SUCCESS);
        BeanUtils.copyProperties(report, log);
        System.out.println("report=" + report + " \nlog=" + log);
    }
}
