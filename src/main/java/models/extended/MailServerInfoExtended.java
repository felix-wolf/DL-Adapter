package models.extended;

import models.MailServerInfo;

public class MailServerInfoExtended extends MailServerInfo implements Extended {

    private final String updatedAt;
    private final boolean isDeleted;

    public MailServerInfoExtended(
            String mailServer, Integer port, String emailId,
            String password, Boolean sslEnabled, String updatedAt, boolean isDeleted
    ) {
        super(mailServer, port, emailId, password, sslEnabled);
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    @Override
    public String getId() {
        return getMailServer();
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    @Override
    public MailServerInfo getBasicType() {
        return new MailServerInfo(getMailServer(), getPort(), getEmailId(), getPassword(), getSslEnabled());
    }
}