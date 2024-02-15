package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simpleQuery.OrderSimpleQueryRepository;
import jpabook.jpashop.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * ToMany관계는 컬렉션이기에 복잡.
 * XToOne(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        // ⚠️무한 루프에 빠지게 됨
        // : Order를 가져오기 위해 Member로 가면 Order가 있고 다시 Member가 있고 Or....
        // --> StackOverflowError (객체를 무한 루프로 뽑아냄)
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); //LAZY 강제 초기화
            order.getDelivery(); //LAZY 강제 초기화
        }
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public Result ordersV2() {
        //Order 조회 Query -> 2건 조회
        //N + 1 -> 1 + 회원 N + 배송 N
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        //2건 -> 루프 2회
        List<SimpleOrderDto> collect = orders.stream()
                .map(SimpleOrderDto::new)
                .collect(toList());

        return new Result(collect);
    }

    @GetMapping("/api/v3/simple-orders")
    public Result ordersV3() { //🌟재사용성이 좋음
        List<Order> orders = orderRepository.findAllWithMemberDelivery();

        List<SimpleOrderDto> collect = orders.stream()
                .map(SimpleOrderDto::new)
                .collect(toList());

        return new Result(collect);
    }
    /*V3와 V4는 서로 trade-off가 존재*/
    /*
    v3: Order format으로 조회하여 다양한 api에서 활용가능 + 엔티티를 반환했기에 조작 가능
    v4: 특정 Dto format으로 fit한 조회라서 해당 DTO전용임 + 쿼리최적화(생각보다 미비) + DTO반환이기에 데이터 조작 불가능(JPA가 관리 불가능) + 코드 지저분
     */
    @GetMapping("/api/v4/simple-orders")
    public Result ordersV4() { //🌟재사용성이 떨어짐.(해당 DTO전용)
        // select 절에서 원하는 것만 조회해옴. v3보다 select 절이 최적화됨.(네트워크를 덜 사용)
        // 페치조인은 동일
        return new Result(orderSimpleQueryRepository.findOrderDtos());
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        //API 스펙의 유연성을 위함
        private T data;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            // 중요하지 않은 Dto에서 Entity를 노출시키는 것은 상관 X
            orderId = order.getId();
            name = order.getMember().getName(); //LAZY 초기화시점(영속성 컨텍스트에 없으니 DB에서 끌고옴)
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //LAZY 초기화시점(영속성 컨텍스트에 없으니 DB에서 끌고옴)
        }
    }

}
