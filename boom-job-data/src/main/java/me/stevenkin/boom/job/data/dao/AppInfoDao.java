package me.stevenkin.boom.job.data.dao;

import me.stevenkin.boom.job.common.model.App;

import java.util.List;

public interface AppInfoDao {

    App selectAppById(Long id);

    List<App> selectAppsByUsernameAndAppName(String username, String appName);

    App selectAppByUsernameAndAppNameAndVersion(String username, String appName, String version);

    List<App> selectAppsByUsername(String username);

    List<App> selectAll();

    Integer countByUsername(String username);

    Integer count();

    Integer delete(App app);

    Integer update(App app);

}
