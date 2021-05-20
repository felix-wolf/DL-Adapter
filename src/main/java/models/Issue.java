package models;

public class Issue {

    private String memberId;
    private String bookId;
    private String renewCount;
    long timestamp;

    public Issue(String memberId, String bookId, String renewCount, long timestamp) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.timestamp = timestamp;
        this.renewCount = renewCount;
    }

    public Issue(String bookId, String renewCount, long timestamp) {
        this.bookId = bookId;
        this.renewCount = renewCount;
        this.timestamp = timestamp;
    }

    public Issue(String bookId) {
        this.bookId = bookId;
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
