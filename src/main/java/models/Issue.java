package models;

public class Issue {
    private final String memberId;
    private final String bookId;
    private final String renewCount;
    private final long timestamp;

    public Issue(String memberId, String bookId, String renewCount, long timestamp) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.timestamp = timestamp;
        this.renewCount = renewCount;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getBookId() {
        return bookId;
    }

    public String getRenewCount() {
        return renewCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
