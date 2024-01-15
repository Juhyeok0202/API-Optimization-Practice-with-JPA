package jpabook.jpashop.service;


import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class) // "Junit4 실행할 때, Spring이랑 같이 엮어서 실행할래."
@SpringBootTest // 스프링부트를 띄움. 스프링 컨테이너가 있어야 Autowired가 정상 작동. 즉, 스프링 컨테이너 안에서 테스트를 돌리는 것
@Transactional //데이터 변경해야 하기에 Rollback시키기 위해
public class MemberServiceTest {


    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;

    @Test
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("Kim");

        //when
        Long savedId = memberService.join(member);
            /*
            [Insert 쿼리가 만들어지지 않았다.]
            당연하다! em.persist()를 하면 영속성 컨텍스트에 저장되고,
            DB 트랜잭션이 정확하게 COMMIT을 하는 순간 Flush가 되면서
            JPA영속성 컨텍스트에 있는 이 멤버 객체가 insert문이 만들어지면서
            DB에 insert가 딱 나감.

            🌟Spring의 Test에서의 Transactional은 기본적으로 트랜잭션 COMMIT을 안하고,
            ROLLBACK을 해버린다.(@Rollback(false)하면 되긴 함. 그럼 롤백은 안됨. 테스트 목적에 부적합)
            RollBack by Spring -> JPA "insert DB에 날릴 이유 X".
            정확히 말하면, 영속선 컨텍스트가 flush를 안해버린다.
             */

        /*EntityManager로 Flush를 해주면 DB에 반영되고, 마지막에 @Transactional로 인해 Rollback됨.*/
        em.flush();

        //then
        assertEquals(member, memberRepository.findOne(savedId)); //두 개가 똑같은 것
            /*
            JPA에서 같은 Transaction 안에서 같은 Entity, 그러니까 ID값이 똑같으면, PK값이 똑같으면,
            얘는 같은 영속성 컨텍스트에서 똑같은 애가 관리가 된다.

            그러니까 2개, 3개 생기지 않고 딱 하나로만 관리가 된다.
             */
    }

    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1 = new Member();
        member1.setName("Lim");

        Member member2 = new Member();
        member2.setName("Lim");
        //when
        memberService.join(member1);
        memberService.join(member2);

        //then
        fail("예외가 발생해야 한다.");
    }
}