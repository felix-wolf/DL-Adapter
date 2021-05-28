package models;

public class MailServerInfo {

    private final String mailServer;
    private final Integer port;
    private final String emailId;
    private final String password;
    private final Boolean sslEnabled;

    public MailServerInfo(String mailServer, Integer port, String emailId, String password, Boolean sslEnabled) {
        this.mailServer = mailServer;
        this.port = port;
        this.emailId = emailId;
        this.password = password;
        this.sslEnabled = sslEnabled;
    }

    /**
     * use below constructor when deleting all
     */
    public MailServerInfo() {
        this.mailServer = null;
        this.port = null;
        this.emailId = null;
        this.password = null;
        this.sslEnabled = null;
    }

    public String getMailServer() {
        return mailServer;
    }

    public Integer getPort() {
        return port;
    }

    public String getEmailId() {
        return emailId;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getSslEnabled() {
        return sslEnabled;
    }

}
