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
        statusMap.put(DISABLED, OFFLINE);
        statusMap.put(OFFLINE, DISABLED);
        statusMap.put(OFFLINE, ONLINE);
        statusMap.put(ONLINE, OFFLINE);
        statusMap.put(ONLINE, RUNNING);
        statusMap.put(RUNNING, ONLINE);
        statusMap.put(OFFLINE, RUNNING);
        statusMap.put(RUNNING, OFFLINE);
        statusMap.put(ONLINE, PAUSED);
        statusMap.put(PAUSED, ONLINE);
        statusMap.put(PAUSED, OFFLINE);
    }

    public boolean isLegalStatus(JobStatus from, JobStatus to) {
        return statusMap.containsEntry(from, to);
    }


}
