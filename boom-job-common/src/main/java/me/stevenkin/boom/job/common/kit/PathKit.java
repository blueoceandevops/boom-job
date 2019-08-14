package me.stevenkin.boom.job.common.kit;

import org.apache.commons.lang3.StringUtils;

public class PathKit {

    private final static String SLASH = "/";

    public static String format(Object... parts){
        StringBuilder key = new StringBuilder();
        for (Object part : parts){
            key.append(SLASH).append(part);
        }
        String strKey = key.toString();
        return strKey.startsWith("//") ? strKey.replace("//", "/") : strKey;
    }

    public static String lastNode(String path){
        if (StringUtils.isEmpty(path)){
            return null;
        }
        return path.substring(path.lastIndexOf("/") + 1);
    }

}
