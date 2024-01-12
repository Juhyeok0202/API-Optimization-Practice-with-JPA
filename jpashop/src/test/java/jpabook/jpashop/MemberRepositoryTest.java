package jpabook.jpashop;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class) // Junit에게 스프링관련 테스트 함을 알림
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired //DI
    MemberRepository memberRepository;

    @Test
    // @Transactional은 Test에서는 다 끝난 뒤, DB를 Roll-Back을 해버림
    @Transactional //Entity Manager를 통한 모든 데이터 변경은 항상 Transaction 안에서 이루어 져야 함.
    @Rollback(value = false)
    public void testMember() throws Exception{
        //given
        Member member = new Member();
        member.setUsername("memberA");

        //when
        Long savedId = memberRepository.save(member);
        Member findMember = memberRepository.find(savedId);

        //then
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(findMember).isEqualTo(member); //같을까? YES
        System.out.println("findMember == member = " + (findMember == member)); // true
        // -> 같은 트랜잭션안에서 저장하고 조회 -> 영속성 컨텐스트가 동일
        // -> 같은 영속성 컨텍스트 안 -> ID값 같으면 -> 같은 엔티티로 식별
        // 1차 캐시라 불리는 곳에서 이미 영속성 컨텍스트에 엔티티가 관리되고 있는 같은게 있기에 기존에 관리 하던게 나옴
    }
}