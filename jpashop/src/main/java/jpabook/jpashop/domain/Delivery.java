package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
public class Delivery {

    @Id
    @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(mappedBy = "delivery", fetch = LAZY) // ⚠️ XToOne은 default fetch 전략이 EAGER => JPA n+1 문제 발생
    private Order order;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING) //순서에 의해 밀리는게 없도록 STRING 타입으로
    private DeliveryStatus status; // READY, COMP
}
