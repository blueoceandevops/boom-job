package me.stevenkin.boom.job.router;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.alibaba.dubbo.rpc.cluster.RouterFactory;

public class BlacklistRouterFactory implements RouterFactory {
    @Override
    public Router getRouter(URL url) {
        return new BlacklistRouter(url);
    }
}
