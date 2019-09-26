package me.stevenkin.boom.job.common.support;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import static me.stevenkin.boom.job.common.support.ComponentStatus.*;

public final class LifeStatusMachine {
    private static Multimap<ComponentStatus, ComponentStatus> statusMap;

    static {
        statusMap = LinkedHashMultimap.create();
        statusMap.put(NEW, RUNNING);
        statusMap.put(RUNNING, PAUSED);
        statusMap.put(PAUSED, RUNNING);
        statusMap.put(PAUSED, CLOSED);
        statusMap.put(RUNNING, CLOSED);
    }

    public static boolean isLegalStatus(ComponentStatus from, ComponentStatus to) {
        return statusMap.containsEntry(from, to);
    }
}
