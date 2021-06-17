package models;

public class Book {

    private final String id;
    private final String title;
    private final String author;
    private final String publisher;
    private final boolean isAvailable;

    public Book(
            String id, String title, String author, String publisher,
            boolean isAvailable
    ) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.isAvailable = isAvailable;
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

    public Boolean isAvailable() {
        return isAvailable;
    }

}
