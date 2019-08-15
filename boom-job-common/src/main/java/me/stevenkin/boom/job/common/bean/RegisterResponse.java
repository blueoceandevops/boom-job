package me.stevenkin.boom.job.common.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse implements Serializable{
    private static final long serialVersionUID = -5046394415365577605L;
    private static final int SUCCESS = 0;
    private static final int FAILED = 1;
    private static final int REPEAT = 2;
    private static final int NO_LINKED = 3;

    private int code;
    private String message;

}
