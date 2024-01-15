package jpabook.jpashop.domain.item;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
public abstract class Item { //구현체를 가지고 할 것이기에 -> 추상 클래스로

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    /*공통 속성*/
    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    //==비즈니스 로직==//
    /*
    💡DDD라고 할 때, Entity자체가 해결할 수 있는 것들은 주로
    Entity 안에 비즈니스 로직을 넣는게 좋다. -> '객체지향적인 것임'

    @Setter를 쓰는 것이 아니라 아래의 핵심비즈니스로직을 가지고 변경 해야함.
     */

    /**
     * stock 증가
     */
    public void addStock(int stockQuantity) {
        this.stockQuantity += stockQuantity;
    }

    /**
     * stock 감소
     */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }
}
