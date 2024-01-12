package jpabook.jpashop.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable //JPA 내장 타입: "어딘가에 내장될 수 있다."
@Getter // 값 타입은 변경이 되면 안되도록 설계하여야 함.
public class Address {

    private String city;
    private String street;
    private String zipcode;

    protected  Address() {
        // protected로 하여 JPA 스펙상 설정
        // 코드를 보고 기본생성자가 아닌 아래 생성자를 쓰라는 구나 알 수 있음.
        /*
        JPA가 이런 제약을 두는 이유는 JPA 구현 라이브러리가
        객체를 생성할 때 리플렉션,프록시 같은 기술을
        사용할 수 있도록 지원해야 하기 때문이다.
         */
    }

    //생성할 때만 값이 세팅 되도록 설계. (추후 변경 불가)
    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
