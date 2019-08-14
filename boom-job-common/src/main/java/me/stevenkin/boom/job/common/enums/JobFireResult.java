package me.stevenkin.boom.job.common.enums;

import java.util.Objects;

public enum JobFireResult {

    FIRE_SUCCESS(0),

    FIRE_FAILED(1);

    private Integer code;

    JobFireResult(Integer code) {
        this.code = code;
    }

    public Integer code() {
        return code;
    }

    public static JobFireResult from(Integer code){
        for (JobFireResult jobFireResult : JobFireResult.values()) {
            if (Objects.equals(jobFireResult.code(), code)) {
                return jobFireResult;
            }
        }
        throw new IllegalArgumentException("the code " + code + "is illegal argument");
    }
}
