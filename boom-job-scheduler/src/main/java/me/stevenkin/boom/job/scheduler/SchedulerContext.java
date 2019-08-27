package me.stevenkin.boom.job.scheduler;

import me.stevenkin.boom.job.common.kit.NetworkKit;
import me.stevenkin.boom.job.common.kit.SystemKit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SchedulerContext {

    @Value("${server.address:-1}")
    private String address;

    private String pid;

    private String timestamp;

    @PostConstruct
    public void init(){
        if ("-1".equals(address)){
            address = NetworkKit.getSiteIp();
        }
        pid = SystemKit.pid();
        timestamp = Long.toString(System.currentTimeMillis());
    }

    public String getSchedulerId() {
        return address + ":" + pid + timestamp;
    }

}
