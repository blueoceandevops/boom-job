package me.stevenkin.boom.job.test.processor;

import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.AppInfo;
import me.stevenkin.boom.job.common.dto.JobInfo;
import me.stevenkin.boom.job.common.dto.RegisterResponse;
import me.stevenkin.boom.job.common.service.RegisterService;

@Slf4j
public class RegisterServiceTest implements RegisterService {
    @Override
    public RegisterResponse registerAppInfo(AppInfo appInfo) {
        log.info(appInfo.toString());
        return new RegisterResponse(RegisterResponse.SUCCESS, null);
    }
}