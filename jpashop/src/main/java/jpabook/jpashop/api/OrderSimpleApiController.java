package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
