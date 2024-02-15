package jpabook.jpashop.repository.order.simpleQuery;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository는 순수 엔티티를. 객체 그래프를 탐색하는 용도로 사용한다.
 * 따라서, 조회 전용으로 화면에 맞추어서
 * DTO스펙이 Repository 자체에 노출된. 특정 DTO에 한정된 최적화 쿼리를
 * 유지 보수를 위해 따로 패키지를 파서 사용
 */
@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    public List<SimpleOrderQueryDto> findOrderDtos() {
        return em.createQuery(
                        // new operation에서 Entity를 넘기면, 식별자(id)값이 넘어감
                        "select new jpabook.jpashop.repository.order.simpleQuery.SimpleOrderQueryDto(o.id,m.name,o.orderDate,o.status,d.address)" +
                                " from Order o" +
                                " join o.member m" +
                                " join o.delivery d", SimpleOrderQueryDto.class)
                .getResultList();
    }
}
