package me.stevenkin.boom.job.scheduler.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DubboConfig {
    private String applicationName = "boom-job-scheduler";
    private String applicationVersion = "0.0.1";

    private String registerAddress;
    private Integer registerPort;
    private String registerProtocol;

    private String protocolName = "dubbo";
    private Integer protocolPort = -1;
    private Integer protocolThreads = 200;

    private Integer timeout = 500;
    private Integer retries = 0;
}
