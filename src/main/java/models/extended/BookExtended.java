package models.extended;

import models.Book;

public class BookExtended extends Book implements Extended {

    private final String updatedAt;
    private final boolean isDeleted;

    public BookExtended(
            String id, String title, String author, String publisher,
            boolean isAvailable, String updatedAt, boolean isDeleted
    ) {
        super(id, title, author, publisher, isAvailable);
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    @Override
    public String getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean getIsDeleted() {
        return isDeleted;
   }

    @Override
    public Object getBasicType() {
        return new Book(getId(), getTitle(), getAuthor(), getPublisher(), isAvailable());
    }
}