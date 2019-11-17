package me.stevenkin.boom.job.common.kit;

import lombok.Getter;
import lombok.Setter;

public class Holder<T> {
    @Getter
    @Setter
    private volatile T data;
}
