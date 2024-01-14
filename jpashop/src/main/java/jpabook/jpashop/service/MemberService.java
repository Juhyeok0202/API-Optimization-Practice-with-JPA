package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; //Javax도 있지만, 스프링 것이 옵션이 더 많음(권장)

import java.util.List;

@Service // 스프링 빈 등록
@Transactional(readOnly=true) //JPA의 모든 데이터 변경이나 로직들은 가급적 트랜잭션 안에서 실행 -> LAZT loading 등이 다 됨.(public 메서드는 트랜잭션에 걸려 들어감)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository; // 생성 시점에 DI해주는 것이 좋음. 또한, Test에서 Mock 데이터 넣기 좋음.

    /**
     * 회원 가입
     */
    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member); //중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
        /*
        " 사실 뭐 이렇게까지 안해도 되고 더 간단하게 멤버 수를 세 가지고 0보다 크면
        문제가 있다거나 이런식으로 로직을 하는게 좀 더 최적화 됨.(예제니까 간단하게 이정도만)
         */
    }

    //회원 전체 조회
//    @Transactional(readOnly = true) //⚠️ readOnly로 조회 최적화 -> 영속성 컨텍스트 플러시X 더디체킹X etc.
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

//    @Transactional(readOnly = true)
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}
