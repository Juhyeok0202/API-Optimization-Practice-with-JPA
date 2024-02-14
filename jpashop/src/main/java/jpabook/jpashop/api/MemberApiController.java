package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController //@Controller @ResponseBody(data 자체를 JSON xml로 바로 보내기)
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /**
     * 회원등록 API
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        //@Valid : Validation 설정을 활성화 (SpringBoot menual - Controll advice 참고하여 에러 커스텀 가능)
        Long id = memberService.join(member);
        return new CreateMemberResponse(id)Q;
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        //🌟API 요청 들어오는 나가는 것은 절대 Entity사용금지. DTO활용
        //엔티티 스펙과 API스펙이 1:1매핑되는 문제를 DTO를 만듦으로써 서로 독립적 관계를 만들어줌.
        //API request JSON 스펙을 명시적으로 확인할 수 있음
        Member member = new Member();
        member.setName(request.name);

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class CreateMemberRequest {

        @NotEmpty //Entity가 아닌 DTO에 Validation을 함. 엔티티가 변하지 않는 것이 중요.
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class CreateMemberResponse {
        private Long id;
    }

}
