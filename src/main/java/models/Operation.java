package models;

import com.google.gson.Gson;

public class Operation {

    final private OperationType operationType;
    final private ObjectType objectType;
    final private Object object;
    final private long time;

    public Operation(long time, OperationType operationType, ObjectType objectType, Object object) {
        this.time = time;
        this.object = object;
        this.objectType = objectType;
        this.operationType = operationType;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public Object getObject() {
        return object;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public long getTime() {
        return time;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
