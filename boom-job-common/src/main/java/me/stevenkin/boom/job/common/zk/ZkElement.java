package me.stevenkin.boom.job.common.zk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZkElement {
    private String node;
    private byte[] data;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ZkElement zkElement = (ZkElement) o;
        return Objects.equals(node, zkElement.node);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), node);
    }
}
