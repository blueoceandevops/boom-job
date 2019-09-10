package me.stevenkin.boom.job.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse implements Serializable{
    private static final long serialVersionUID = -5046394415365577605L;
    public static final int SUCCESS = 0;
    public static final int FAILED = 1;
    public static final int REPEAT = 2;
    public static final int NO_LINKED = 3;

    private int code;
    private String message;

    public boolean isSuccess() {
        return code == SUCCESS;
    }

    public boolean isFailed() {
        return code == FAILED;
    }

    public boolean isRepeat() {
        return code == REPEAT;
    }

    public boolean isNoLinked() {
        return code == NO_LINKED;
    }

}
