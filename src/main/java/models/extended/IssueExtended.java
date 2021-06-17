package models.extended;

import models.Issue;

public class IssueExtended extends Issue implements Extended {

    private final String updatedAt;
    private final boolean isDeleted;

    public IssueExtended(String memberId, String bookId, String renewCount, long timestamp, String updatedAt, boolean isDeleted) {
        super(memberId, bookId, renewCount, timestamp);
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    @Override
    public String getId() {
        return getBookId();
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    @Override
    public Object getBasicType() {
        return new Issue(getMemberId(), getBookId(), getRenewCount(), getTimestamp());
    }
}
