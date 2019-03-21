import org.jgroups.Address;
import org.jgroups.Message;

import java.io.Serializable;

class MapSynchroniseObject implements Serializable {
    private String key;
    private Integer value;
    private MapOperation operation;

    enum MapOperation {
        PUT,
        REMOVE
    }

    public MapSynchroniseObject(String key, MapOperation operation) {
        this(key, null, operation);
    }

    public MapSynchroniseObject(String key, Integer value, MapOperation operation) {
        this.key = key;
        this.value = value;
        this.operation = operation;
    }

    public String getKey() {
        return key;
    }

    public Integer getValue() {
        return value;
    }

    public MapOperation getOperation() {
        return operation;
    }
}
