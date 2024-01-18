package jpabook.jpashop.controller;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * View 열기
     */
    @GetMapping("/members/new")
    public String createForm(Model model) {
        /*
        Model?
        Controller에서 View로 넘어갈 때 Model에 넣은 데이터를 실어서 넘긴다.
         */
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    /**
     * 실제 회원 등록
     */
    @PostMapping("members/new")
    public String create(@Valid MemberForm form, BindingResult result) {
        // @Valid 사용 시, Validation 했던 것을 사용할 수 있게 된다.
        // BindingResult : 오류 시, 튕기지 않고 여기에 담겨서 이 코드가 실행된다.

        if (result.hasErrors()) {
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/"; //홈으로 리디렉션
    }

    /**
     * 전체 회원 조회
     */
    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members); //key:value
        return "members/memberList";
    }
}
