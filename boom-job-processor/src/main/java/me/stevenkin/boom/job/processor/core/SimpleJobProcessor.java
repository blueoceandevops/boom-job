package me.stevenkin.boom.job.processor.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.*;
import me.stevenkin.boom.job.common.enums.JobFireResult;
import me.stevenkin.boom.job.common.service.JobExecuteService;
import me.stevenkin.boom.job.common.service.JobProcessor;
import me.stevenkin.boom.job.common.service.ShardExecuteService;
import org.springframework.beans.BeanUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

@Getter
@NoArgsConstructor
@Slf4j
public class SimpleJobProcessor implements JobProcessor {

    public SimpleJobProcessor(Job job, String jobId, BoomJobClient jobClient) {

    }

    @Override
    public JobFireResponse fireJob(JobFireRequest request) {
        return null;
    }

}
