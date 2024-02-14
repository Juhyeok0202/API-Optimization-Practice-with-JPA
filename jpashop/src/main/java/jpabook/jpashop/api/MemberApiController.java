package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * v1: worst
 * v2: better
 */
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
        return new CreateMemberResponse(id);
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

    @GetMapping("/api/v1/members")
    public List<Member> memberV1() {
        //⚠️엔티티를 직접 노출하면 안된다. DTO를 활용
        //️️⚠️엔티티에 @JsonIgnore 하면 빠지지만, 그냥 DTO 활용(엔티티는 불변하는게 좋음)
        //⚠️ 엔티티가 변경 되면, API스펙이 변경됨.
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result memberV2() {
        //엔티티를 노출시키지 않고,
        //DTO를 사용함으로써 스펙이 서로 독립적
        //API스펙에 대한 유연성 보장
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect.size(),collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        //List는 이런식으로 한 번 감싸주어야함. -> 유연성 생김
        //Otherwise -> Json 배열 타입으로 나와 -> 유연성 떨어짐 (ex. count 추가해줘!)
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }


    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.name); // Command는 Command성으로 Query와 분리 설계.
        Member findMember = memberService.findOne(id); //Query성으로 다시 가져와서 사용
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }



    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }


    @Data
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
