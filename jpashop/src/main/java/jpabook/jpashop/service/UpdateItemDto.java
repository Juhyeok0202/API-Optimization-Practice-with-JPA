package jpabook.jpashop.service;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateItemDto {
    //Parameters가 많으면 Dto로
    private String name;
    private int price;
    private int stockQuantity;
}
