package me.stevenkin.boom.job.common.enums;

import lombok.Getter;

public enum JobType {
    NOT_CONFIGURED(0, "not configured"),

    SIMPLE(1, "simple"),

    CRON(2, "cron");
    @Getter
    private Integer code;
    @Getter
    private String message;

    JobType(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static JobType fromCode(Integer code) {
        for (JobType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException();
    }
}
