package me.stevenkin.boom.job;

import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.bean.AppInfo;
import me.stevenkin.boom.job.common.bean.JobInfo;
import me.stevenkin.boom.job.common.bean.RegisterResponse;
import me.stevenkin.boom.job.common.job.RegisterService;

@Slf4j
public class RegisterServiceTest implements RegisterService {
    @Override
    public RegisterResponse registerAppInfo(AppInfo appInfo) {
        log.info(appInfo.toString());
        return new RegisterResponse(RegisterResponse.SUCCESS, null);
    }

    @Override
    public RegisterResponse registerJobInfo(JobInfo jobInfo) {
        log.info(jobInfo.toString());
        return new RegisterResponse(RegisterResponse.SUCCESS, null);
    }
}
