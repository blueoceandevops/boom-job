package me.stevenkin.boom.job.common.enums;

import lombok.Getter;

public enum JobType {

    SIMPLE(0, "simple"),

    PLAN(1, "plan");
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
