package jpabook.jpashop;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

@Repository //Component Scan이 되는 대상이 되도록
public class MemberRepository {

    @PersistenceContext
    private EntityManager em;

    public Long save(Member member) {
        /*
        [Member가 아닌, Id를 반환하는 이유]
        "Command와 Query를 분리해라"
        저장을 하고 나면, 가급적 사이드 이펙트를 일으키는 커맨드성이기에
        리턴 값을 거의 만들지 않음.
        대신, ID정도 있으면, 다음에 다시 조회할 수 있으니. 이정도만.
         */
        em.persist(member);
        return member.getId();
    }

    public Member find(Long id) {
        return em.find(Member.class, id);
    }
}
