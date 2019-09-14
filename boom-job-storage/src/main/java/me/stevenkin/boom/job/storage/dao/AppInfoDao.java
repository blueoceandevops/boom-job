package me.stevenkin.boom.job.storage.dao;

import me.stevenkin.boom.job.common.po.App;

import java.util.List;

public interface AppInfoDao {

    App selectAppById(Long id);

    App selectAppByUsernameAndAppName(String username, String appName);

    App selectAppByUsernameAndAppNameAndVersion(String username, String appName, String version);

    List<App> selectAppsByUsername(String username);

    List<App> selectAll();

    Integer countByUsername(String username);

    Integer count();

    Integer insert(App app);

    Integer delete(App app);

    Integer update(App app);

}
