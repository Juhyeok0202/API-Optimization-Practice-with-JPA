package jpabook.jpashop.domain.item;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jpabook.jpashop.controller.BookForm;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static lombok.AccessLevel.PROTECTED;

@Entity
@DiscriminatorValue("B")
@Getter
@Setter
@NoArgsConstructor(access = PROTECTED)
public class Book extends Item {

    private String author;
    private String isbn;

//    public static Book createBook(String author, String isbn, String name, int price, int stockQuantity) {
//        Book book = new Book();
//        book.setName(name);
//        book.setPrice(price);
//        book.setStockQuantity(stockQuantity);
//        book.setAuthor(author);
//        book.setIsbn(isbn);
//
//        return book;
//    }
    public static Book createBook(BookForm form) {
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        return book;
    }
}
