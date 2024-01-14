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
@Getter @Setter
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = LAZY) // ⚠️ XToOne은 default fetch 전략이 EAGER => JPA n+1 문제 발생
    @JoinColumn(name = "member_id") //FK명이 member_id. (매핑할 엔티티의 PK 명을 관례적으로 사용)
    private Member member;

    @OneToMany(mappedBy = "order") // XToMany는 default fetch 전략이 LAZY임.
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY) // ⚠️ XToOne은 default fetch 전략이 EAGER => JPA n+1 문제 발생
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; // 주문 시간 - DateTime -> 시간과 분까지 모두 포함

    @Enumerated(EnumType.STRING) //순서에 의해 밀리는게 없도록 STRING 타입으로
    private OrderStatus status; // 주문상태 [ORDER, CANCEL]
}
