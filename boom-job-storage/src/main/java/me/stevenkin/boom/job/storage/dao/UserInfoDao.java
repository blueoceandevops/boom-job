package me.stevenkin.boom.job.storage.dao;

import me.stevenkin.boom.job.common.po.User;

import java.util.List;

public interface UserInfoDao {

    User selectUserById(Long id);

    User selectUserByUsername(String username);

    List<User> selectAllUsers();

    Integer count();

    Integer insert(User user);

    Integer delete(User user);

    Integer update(User user);
}
