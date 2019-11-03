package me.stevenkin.boom.job.storage.dao;

import java.util.List;

public interface BlacklistDao {
    List<String> selectByJobId(Long jobId);

    Integer insertBlacklist(Long jobId, String address);

    Integer deleteBlacklist(Long jobId, String address);
}
