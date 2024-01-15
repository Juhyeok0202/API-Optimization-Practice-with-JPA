package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) { // Item은 JPA 저장할 때 까지 ID값이 없음. 완전 새로 생성하는 객체
            em.persist(item);
        } else { // DB에서 이미 있던걸 가져온 경우
            em.merge(item); // update '비슷'한건데 (나중에 설명해주실 예정)
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
