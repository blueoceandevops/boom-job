package me.stevenkin.boom.job.common.enums;

import lombok.Getter;

public enum JobTriggerType {
    AUTO(0, "auto"),

    MANUAL(1, "manual"),

    PLAN(2, "plan");

    @Getter
    private Integer code;
    @Getter
    private String message;

    JobTriggerType(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static JobTriggerType fromCode(Integer code) {
        for (JobTriggerType type : values()) {
            if (type.code.equals(code))
                return type;
        }
        throw new IllegalArgumentException();
    }
}
