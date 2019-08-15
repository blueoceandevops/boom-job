package me.stevenkin.boom.job.common.job;

import me.stevenkin.boom.job.common.bean.AppInfo;
import me.stevenkin.boom.job.common.bean.JobInfo;
import me.stevenkin.boom.job.common.bean.RegisterResponse;

public interface RegisterService {

    RegisterResponse registerAppInfo(AppInfo appInfo);

    RegisterResponse registerJobInfo(JobInfo jobInfo);
}
