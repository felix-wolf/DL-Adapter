package models;

public class MailServerInfo {

    private final String mailServer;
    private final Integer port;
    private final String emailID;
    private final String password;
    private final Boolean sslEnabled;

    public MailServerInfo(String mailServer, Integer port, String emailID, String password, Boolean sslEnabled) {
        this.mailServer = mailServer;
        this.port = port;
        this.emailID = emailID;
        this.password = password;
        this.sslEnabled = sslEnabled;
    }

    /**
     * use below constructor when deleting all
     */
    public MailServerInfo() {
        this.mailServer = null;
        this.port = null;
        this.emailID = null;
        this.password = null;
        this.sslEnabled = null;
    }

    public String getMailServer() {
        return mailServer;
    }

    public Integer getPort() {
        return port;
    }

    public String getEmailID() {
        return emailID;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getSslEnabled() {
        return sslEnabled;
    }

}
