package jpabook.jpashop.controller;

import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookForm {

    /*Common attributes*/
    private Long id; // for 상품 수정
    private String name;
    private int price;
    private int stockQuantity;

    /*About book*/
    private String author;
    private String isbn;
}
