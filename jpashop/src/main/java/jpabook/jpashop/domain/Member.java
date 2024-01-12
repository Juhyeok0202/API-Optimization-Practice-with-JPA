package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;


    private String name;

    @Embedded // "내장 타입을 포함했다." (둘 중 하나만 있으면, 되지만 명시적으로 양쪽에 어노테이션)
    private Address address;

    @OneToMany(mappedBy = "member") // "Order Table에 있는 member 필드에 의해 매핑 된거야"
    private List<Order> orders = new ArrayList<>();
}
