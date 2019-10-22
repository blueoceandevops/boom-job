package me.stevenkin.boom.job.common.kit;

import com.alibaba.dubbo.common.URL;
import me.stevenkin.boom.job.common.dubbo.Node;
import org.apache.commons.lang3.StringUtils;

public class URLKit {

    public static Node urlToNode(URL url) {
        String startTime = url.getParameter("timestamp");
        if (StringUtils.isEmpty(startTime)) {
            throw new RuntimeException();
        }
        Integer.parseInt(startTime);
        return new Node(url.getAddress(), url.getPort(), startTime, false);
    }
}
