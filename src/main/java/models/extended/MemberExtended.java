package models.extended;

import models.Member;

public class MemberExtended extends Member implements Extended {
    private final String updatedAt;
    private final boolean isDeleted;

    public MemberExtended(String id, String name, String mobile, String email, String updatedAt, boolean isDeleted) {
        super(id, name, mobile, email);
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    @Override
    public Object getBasicType() {
        return new Member(getId(), getName(), getMobile(), getEmail());
    }
}
