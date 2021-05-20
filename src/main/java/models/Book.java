package models;

public class Book {

    private final String id;
    private final String title;
    private final String author;
    private final String publisher;
    private final boolean isAvailable;
    private final String updatedAt;

    public Book(String id, String title, String author, String publisher, boolean isAvailable, String updatedAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.isAvailable = isAvailable;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublisher() {
        return publisher;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
