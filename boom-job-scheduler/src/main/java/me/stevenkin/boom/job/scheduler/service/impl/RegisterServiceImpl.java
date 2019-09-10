package me.stevenkin.boom.job.scheduler.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.dto.AppInfo;
import me.stevenkin.boom.job.common.dto.RegisterResponse;
import me.stevenkin.boom.job.common.po.App;
import me.stevenkin.boom.job.common.po.Job;
import me.stevenkin.boom.job.common.po.User;
import me.stevenkin.boom.job.common.service.RegisterService;
import me.stevenkin.boom.job.data.dao.AppInfoDao;
import me.stevenkin.boom.job.data.dao.JobInfoDao;
import me.stevenkin.boom.job.data.dao.UserInfoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Service
@Component
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    private AppInfoDao appInfoDao;
    @Autowired
    private JobInfoDao jobInfoDao;

    @Override
    public RegisterResponse registerAppInfo(AppInfo appInfo) {
        App app = appInfoDao.selectAppByUsernameAndAppName(appInfo.getAuthor(), appInfo.getAppName());
        if (app == null) {
            return new RegisterResponse(RegisterResponse.NO_LINKED, "app " + appInfo.getAuthor() + "/" + appInfo.getAppName() + " not exist");
        }
        try {
            for (String jobClass : appInfo.getJobs()) {
                Job job = new Job();
                job.setAppId(app.getId());
                job.setJobClass(jobClass);
                job.setCreateTime(LocalDateTime.now());
                jobInfoDao.insert(job);
            }
            return new RegisterResponse(RegisterResponse.SUCCESS, null);
        }catch (DuplicateKeyException e) {
            return new RegisterResponse(RegisterResponse.REPEAT, "app has existed");
        }catch (Exception e) {
            return new RegisterResponse(RegisterResponse.FAILED, e.getMessage());
        }

    }
}
