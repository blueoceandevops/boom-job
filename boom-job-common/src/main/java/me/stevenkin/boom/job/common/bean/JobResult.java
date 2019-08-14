package me.stevenkin.boom.job.common.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class JobResult implements Serializable {
    private static final long serialVersionUID = 27524604536152L;

    public static final JobResult SUCCESS = new JobResult(0);

    public static final JobResult FAIL = new JobResult(1);

    public static final JobResult RETRY = new JobResult(2);

    @Setter @Getter
    private int code;

    @Setter @Getter
    private String message;

    public JobResult(int code) {
        this.code = code;
    }

    public boolean is(JobResult r){
        return r.code == code;
    }

    public static JobResult failed(String message) {
        JobResult r = new JobResult(JobResult.FAIL.code);
        r.setMessage(message);
        return r;
    }
}
