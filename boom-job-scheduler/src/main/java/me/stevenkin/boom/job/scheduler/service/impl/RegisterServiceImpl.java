package me.stevenkin.boom.job.scheduler.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import jdk.internal.misc.JavaObjectInputFilterAccess;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.bean.AppInfo;
import me.stevenkin.boom.job.common.bean.JobInfo;
import me.stevenkin.boom.job.common.bean.RegisterResponse;
import me.stevenkin.boom.job.common.model.App;
import me.stevenkin.boom.job.common.model.Job;
import me.stevenkin.boom.job.common.model.User;
import me.stevenkin.boom.job.common.service.RegisterService;
import me.stevenkin.boom.job.data.dao.AppInfoDao;
import me.stevenkin.boom.job.data.dao.JobInfoDao;
import me.stevenkin.boom.job.data.dao.UserInfoDao;
import me.stevenkin.boom.job.scheduler.service.ZkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Slf4j
@Service
@Component
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private AppInfoDao appInfoDao;
    @Autowired
    private JobInfoDao jobInfoDao;
    @Autowired
    private ZkService zkService;

    @Override
    public RegisterResponse registerAppInfo(AppInfo appInfo) {
        User user = userInfoDao.selectUserByUsername(appInfo.getAuthor());
        if (user == null) {
            return new RegisterResponse(RegisterResponse.NO_LINKED, "user " + appInfo.getAuthor() + " not exist");
        }
        App app = new App();
        app.setAppName(appInfo.getAppName());
        app.setVersion(appInfo.getVersion());
        app.setUserId(user.getId());
        boolean insertSuccess;
        try {
            insertSuccess = appInfoDao.insert(app) > 0;
        }catch (DuplicateKeyException e) {
            return new RegisterResponse(RegisterResponse.REPEAT, "app has existed");
        }
        return insertSuccess ? new RegisterResponse(RegisterResponse.SUCCESS, null) :
                new RegisterResponse(RegisterResponse.FAILED, "register failed");
    }

    @Override
    public RegisterResponse registerJobInfo(JobInfo jobInfo) {
        App app = appInfoDao.selectAppByUsernameAndAppNameAndVersion(jobInfo.getAuthor(), jobInfo.getAppName(), jobInfo.getVersion());
        if (app == null) {
            return new RegisterResponse(RegisterResponse.NO_LINKED, "app " + jobInfo.getAppName() + " not exist");
        }
        Job job = new Job();
        job.setAppId(app.getId());
        job.setJobClass(jobInfo.getJobClassName());
        boolean insertSuccess;
        try{
            insertSuccess = jobInfoDao.insert(job) > 0;
            if (insertSuccess) {
                zkService.createJob(jobInfo);
            }
        }catch (DuplicateKeyException e) {
            return new RegisterResponse(RegisterResponse.REPEAT, "job has existed");
        }
        return insertSuccess ? new RegisterResponse(RegisterResponse.SUCCESS, null) :
                new RegisterResponse(RegisterResponse.FAILED, "register failed");
    }
}
