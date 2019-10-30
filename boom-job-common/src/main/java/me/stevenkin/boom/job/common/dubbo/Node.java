package me.stevenkin.boom.job.common.dubbo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Node {
    private String group;
    private String address;
    private Integer port;
    private String startTime;
    private boolean disabled;

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (!(o instanceof Node))
            return false;
        Node o1 = (Node) o;
        return group.equals(o1.getGroup()) && address.equals(o1.address) && port.equals(o1.port) && startTime.equals(o1.startTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(group, address, port, startTime);
    }

    @Override
    public String toString() {
        return address + "_" + port + "_" + startTime;
    }
}
