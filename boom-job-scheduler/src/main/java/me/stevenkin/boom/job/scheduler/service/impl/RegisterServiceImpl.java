package me.stevenkin.boom.job.scheduler.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import lombok.extern.slf4j.Slf4j;
import me.stevenkin.boom.job.common.bean.AppInfo;
import me.stevenkin.boom.job.common.bean.JobInfo;
import me.stevenkin.boom.job.common.bean.RegisterResponse;
import me.stevenkin.boom.job.common.service.RegisterService;
import me.stevenkin.boom.job.data.dao.AppInfoDao;
import me.stevenkin.boom.job.data.dao.JobInfoDao;
import me.stevenkin.boom.job.data.dao.UserInfoDao;
import me.stevenkin.boom.job.scheduler.service.ZkService;
import org.springframework.beans.factory.annotation.Autowired;
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
        return null;
    }

    @Override
    public RegisterResponse registerJobInfo(JobInfo jobInfo) {
        return null;
    }
}
