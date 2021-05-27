package models;

public class Information {

    private final Timestamp timestamp;

    public Information(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long time) {
        this.timestamp.setTime(time);
    }
}
