package me.stevenkin.boom.job.common.kit;

import com.alibaba.dubbo.common.Constants;
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
        boolean enabled = true;
        if (url.hasParameter(Constants.DISABLED_KEY)) {
            enabled = !url.getParameter(Constants.DISABLED_KEY, false);
        } else {
            enabled = url.getParameter(Constants.ENABLED_KEY, true);
        }
        String group = url.getParameter(Constants.GROUP_KEY, "");
        return new Node(group, url.getAddress(), url.getPort(), startTime, !enabled);
    }
}
