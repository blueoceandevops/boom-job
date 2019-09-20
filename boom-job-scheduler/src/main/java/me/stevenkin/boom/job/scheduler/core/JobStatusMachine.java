package me.stevenkin.boom.job.scheduler.core;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import me.stevenkin.boom.job.common.enums.JobStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static me.stevenkin.boom.job.common.enums.JobStatus.*;

@Component
public class JobStatusMachine {
    private Multimap<JobStatus, JobStatus> statusMap = LinkedHashMultimap.create();

    @PostConstruct
    public void init() {

    }

    public boolean isLegalStatus(JobStatus from, JobStatus to) {
        return statusMap.containsEntry(from, to);
    }


}
