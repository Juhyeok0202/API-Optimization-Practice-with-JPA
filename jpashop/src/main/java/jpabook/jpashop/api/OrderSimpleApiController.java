package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
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

    @Data
    @AllArgsConstructor
    static class Result<T> {
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
