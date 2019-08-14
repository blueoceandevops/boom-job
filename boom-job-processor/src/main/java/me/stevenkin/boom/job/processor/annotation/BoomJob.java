package me.stevenkin.boom.job.processor.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface BoomJob {
    /**
     * job name
     */
    String name() default "";

    /**
     * job version
     */
    String version() default "0.0.1";

    /**
     * job desc
     */
    String description() default "";
}
