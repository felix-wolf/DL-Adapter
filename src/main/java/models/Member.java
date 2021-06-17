package models;

public class Member {
    private final String id;
    private final String name;
    private final String email;
    private final String mobile;

    public Member(String id, String name, String mobile, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }
}
