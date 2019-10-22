package me.stevenkin.boom.job.common.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.StringUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class Configuration {
    private String service;
    private String params;
    private String application;
    private String address;
    private int port;
    private String username;
    private boolean enabled;

    public URL toUrl() {
        String group = null;
        String version = null;
        String path = service;
        int i = path.indexOf("/");
        if (i > 0) {
            group = path.substring(0, i);
            path = path.substring(i + 1);
        }
        i = path.lastIndexOf(":");
        if (i > 0) {
            version = path.substring(i + 1);
            path = path.substring(0, i);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.OVERRIDE_PROTOCOL);
        sb.append("://");
        if (!StringUtils.isBlank(address) && !Constants.ANY_VALUE.equals(address)) {
            sb.append(address);
        } else {
            sb.append(Constants.ANYHOST_VALUE);
        }
        if (port != 0) {
            sb.append(":").append(port);
        }
        sb.append("/");
        sb.append(path);
        sb.append("?");
        Map<String, String> param = StringUtils.parseQueryString(params);
        param.put(Constants.CATEGORY_KEY, Constants.CONFIGURATORS_CATEGORY);
        param.put(Constants.ENABLED_KEY, String.valueOf(isEnabled()));
        param.put(Constants.DYNAMIC_KEY, "false");
        if (!StringUtils.isBlank(application) && !Constants.ANY_VALUE.equals(application)) {
            param.put(Constants.APPLICATION_KEY, application);
        }
        if (group != null) {
            param.put(Constants.GROUP_KEY, group);
        }
        if (version != null) {
            param.put(Constants.VERSION_KEY, version);
        }
        sb.append(StringUtils.toQueryString(param));
        return URL.valueOf(sb.toString());
    }
}
