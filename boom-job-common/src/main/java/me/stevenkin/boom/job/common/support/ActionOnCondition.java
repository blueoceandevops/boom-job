package me.stevenkin.boom.job.common.support;

/**
 * 当某个条件成立时执行某个动作
 */
public interface ActionOnCondition<C, P> {

    boolean test(C c);

    void action(P p);

}
