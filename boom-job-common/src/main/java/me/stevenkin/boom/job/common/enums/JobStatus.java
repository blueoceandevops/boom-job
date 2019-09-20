package me.stevenkin.boom.job.common.enums;

import lombok.Getter;

public enum JobStatus {
    ONLINE(0, "online"),

    OFFLINE(1, "offline"),

    PAUSED(3, "paused"),

    DISABLED(-1, "disabled");
    @Getter
    private Integer code;
    @Getter
    private String message;

    JobStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static JobStatus fromCode(Integer code) {
        for (JobStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException();
    }

}
