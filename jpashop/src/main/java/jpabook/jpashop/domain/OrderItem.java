package jpabook.jpashop.domain;

import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = LAZY) // ⚠️ XToOne은 default fetch 전략이 EAGER => JPA n+1 문제 발생
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = LAZY) // ⚠️ XToOne은 default fetch 전략이 EAGER => JPA n+1 문제 발생
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; //주문 가격 - 상품 가격은 변경될 여지가 있음
    private int count; //주문 수량 - 주문 당시의 수량

    //===생성메서드==//
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        //📝 item.getPrice를 orderPrice해도 되지않나? No, 할인될 수도 있으니까. 바뀔 수 있기에 따로 가져간다.
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        //OrderItem을 생성하면 기본적으로 재고를 까줘야 함.
        item.removeStock(count);
        return orderItem;
    }

    //==비즈니스 로직==//
    public void cancel() {
        //취소 -> 재고를 주문 수량만큼 다시 늘린다
        getItem().addStock(count);
    }

    //==조회 로직==//

    /**
     * 주문상품 전체 가격 조회
     */
    public int getTotalPrice() {
        return orderPrice * count;
    }
}
