package jpabook.jpashop.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static jpabook.jpashop.domain.QMember.member;
import static jpabook.jpashop.domain.QOrder.order;

@Repository
public class OrderRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    public OrderRepository(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAllByString(OrderSearch orderSearch) { // 검색 조건에 동적으로 쿼리를 생성해서 주문 엔티티를 조회한다.
//language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    /**
     * JPA Criteria(권장X)
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        //JPA가 제공하는 표준 동적 쿼리를 빌드 해주는. JPQL를 JAVA 코드로
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName()
                            + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건

        return query.getResultList();
    }

    public List<Order> findAll(OrderSearch orderSearch) {
        //QueryDSL 맛보기 -> Compile Time 에러 체크 , 동적 쿼리에서 굉장히 강하다.
        //Q파일은 ignore해주자.(어차피 Build Time에 generate 함)
        //findAllByString -> QueryDSL

 //       QOrder order = QOrder.order;
 //       QMember member = QMember.member;


        return query.select(order)
                .from(order)
                .join(order.member, member)
                .where(statusEq(orderSearch.getOrderStatus()), nameLike(orderSearch)) // Dynamic Query
//                .where(order.status.eq(orderSearch.getOrderStatus())) // Static Query
                .limit(1000)
                .fetch();
    }

    private BooleanExpression nameLike(OrderSearch orderSearch) {
        if (!StringUtils.hasText(orderSearch.getMemberName())) {
            return null;
        }
        return member.name.like(orderSearch.getMemberName());
    }

    private BooleanExpression statusEq(OrderStatus statusCond) {
        if (statusCond == null) {
            return null;
        }
        return order.status.eq(statusCond);
    }

    public List<Order> findAllWithMemberDelivery() { //Fetch Join
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" ,Order.class
                ).getResultList();
    }

    public List<Order> findAllWithItem() {
        /*XToMany 관계에서의 컬렉션 조회를 해결하자.*/
        /*OneToMany Fetch Join -> Paging이 불가능(Memory Paging을 함.)*/
        /*N 쪽인 OrderITem 기준 paging 된다고 생각하면 된다.(따라서, N이 1개 일 때만 사용)*/
        return em.createQuery( // 이렇게 복잡한 쿼리는 QueryDSL로 하는 편이 좋다.
                "select distinct o from Order o" + // ①.DB에 Distinct 키워드 쿼리 ②.엔티티가 중복인 경우에 걸러서 담아줌
                        " join fetch o.member m" + // ManyToOne => 데이터 뻥튀기 X
                        " join fetch o.delivery d"+ // OneToOne => 데이터 뻥튀기 X
                        " join fetch o.orderItems oi"+ //orderItems 4개. Order 2개 => Order가 DB입장에서 4개가 되어버림
                        " join fetch oi.item i", Order.class)
                .setFirstResult(1)
                .setMaxResults(100)
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                // ToOne 관계도 join fetch 안해줘도 Batch Size로 최적화가 가능하나, 네트워크를 2번 더 타게 된다. (24.02.18 Hibernate5 기준)
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" ,Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }



    /*결론 : Query DSL로 동적 쿼리 작성(마지막 장에서 다룰 예정)*/
}