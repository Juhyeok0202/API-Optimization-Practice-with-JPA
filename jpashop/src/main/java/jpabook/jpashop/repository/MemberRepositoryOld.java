package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // 컴포넌트 스캔으로 자동으로 빈 관리가 됨.(스프링 빈 등록)
@RequiredArgsConstructor
public class MemberRepositoryOld {

//    @PersistenceContext //스프링이 EntityManager를 만들어 em에 Injection해줌. BY SPRING
//    private EntityManager em;

    private final EntityManager em; //'스프링 부트 JPA 사용시', 이렇게 일관성 있게 DI할 수도 있다.

    public void save(Member member) {
        em.persist(member); //참고: Transaction 이 commit 되는 시점에 DB에 반영될 것임.
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        // 1: Query 2: return type
        // JPQL은 SQL랑 약간 다름. From의 대상이 SQL은 테이블, JPQL은 Member에 대한 엔티티 객체를 대상으로 쿼리함.(기본편참고)
        // 💡Ctrl+Alt+N 으로 인라인 변수 단축키
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    //💡[내생각] 해당 컬럼을 Unique로 제약하고, service계층에서 validation 과정을 거치기에 굳이 List로 받을 필요는 없다고 느껴진다.
    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = : name", Member.class)
                .setParameter("name",name) // Parameter Binding 해주어야함.(JPQL)
                .getResultList(); // getSingleResult();로 처리하는 것이 더 나은 방법인 것 같다.
    }
}
