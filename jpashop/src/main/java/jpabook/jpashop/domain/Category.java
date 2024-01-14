package jpabook.jpashop.domain;

import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    @ManyToMany // ⚠️실무에서는 @JoinTable로 하면 , Intersection Table 형태로 불가능하므로, 따로 엔티티 만들어 풀어냄
    @JoinTable(name = "category_item", // Association Table과 매핑(RDB는 컬렉션 관계 양쪽 불가능N:M)
        joinColumns = @JoinColumn(name = "category_id"), // Association Table에 있는 FK to Category
        inverseJoinColumns = @JoinColumn(name = "item_id")) // Association Table에 있는 FK to Item
    private List<Item> items = new ArrayList<>();

    /* Recursive Mapping */
    @ManyToOne(fetch = LAZY) // ⚠️ XToOne은 default fetch 전략이 EAGER => JPA n+1 문제 발생
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> child;


    // 양방향일 때!! 양쪽 세팅하는 것을 atomic하게 해결.
    //==연관관계 (편의)메서드--⚠️핵심적으로 컨트롤하는 쪽이 들고 있는 것이 Good//
    public void addChildCategory(Category child) {
        this.child.add(child); //부모 입장에서의 자식 세팅
        child.setParent(this); //자식 입장에서의 부모 세팅
    }
}
