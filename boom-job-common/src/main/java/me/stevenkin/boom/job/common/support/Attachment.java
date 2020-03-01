package me.stevenkin.boom.job.common.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Attachment {
    private Map<String, Object> attach = new HashMap<>();

    public Attachment put(String key, Object value) {
        attach.put(key, value);
        return this;
    }

    public Optional<Object> get(String key) {
        return Optional.ofNullable(attach.get(key));
    }
}
