package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "orders") // DB 예약어와 겹치는 문제로 orders라고 사용
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = LAZY) // ⚠️ XToOne은 default fetch 전략이 EAGER => JPA n+1 문제 발생
    @JoinColumn(name = "member_id") //FK명이 member_id. (매핑할 엔티티의 PK 명을 관례적으로 사용)
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) // XToMany는 default fetch 전략이 LAZY임. // cascade 관련 이슈 생성함.
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL) // ⚠️ XToOne은 default fetch 전략이 EAGER => JPA n+1 문제 발생
    @JoinColumn(name = "delivery_id")
    private Delivery delivery; //⚠️cascade option -> delivery값만 세팅해두면, order 저장할 때, 같이 persist해줌(원래는 각각 해주어야함)

    private LocalDateTime orderDate; // 주문 시간 - DateTime -> 시간과 분까지 모두 포함

    @Enumerated(EnumType.STRING) //순서에 의해 밀리는게 없도록 STRING 타입으로
    private OrderStatus status; // 주문상태 [ORDER, CANCEL]


    // 양방향일 때!! 양쪽 세팅하는 것을 atomic하게 해결.
    //==연관관계 (편의)메서드--⚠️핵심적으로 컨트롤하는 쪽이 들고 있는 것이 Good//
    public void setMember(Member member) {
        // 양방향 매핑관계 이기에 양쪽에 모두 값 세팅을 해주어야 함.
        // 이러한 로직을 하나의 메서드로 묶어주는 역할
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        // 양방향 매핑관계 이기에 양쪽에 모두 값 세팅을 해주어야 함.
        // 이러한 로직을 하나의 메서드로 묶어주는 역할
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        // 양방향 매핑관계 이기에 양쪽에 모두 값 세팅을 해주어야 함.
        // 이러한 로직을 하나의 메서드로 묶어주는 역할
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //==생성 메서드==//
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);

        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }

        order.setStatus(OrderStatus.ORDER); //초기상태를 ORDER로 강제로 지정
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //==비즈니스 로직==//

    /**
     * 주문 취소
     */
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel(); //📝 고객이 한 번 주문할 때, 상품 2개 주문할 수 있으니 모두 취소해주어야 함.
        }
    }

    //==조회 로직==//

    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        int totalPrice=0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice(); // 역할 분리는 단호하게.
        }
        return totalPrice;
    }
}
