package me.stevenkin.boom.job.common.service;

import me.stevenkin.boom.job.common.dto.AppInfo;
import me.stevenkin.boom.job.common.dto.JobInfo;
import me.stevenkin.boom.job.common.dto.RegisterResponse;

public interface AppRegisterService {

    RegisterResponse registerAppInfo(AppInfo appInfo);
}
