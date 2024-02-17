package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.util.ProxyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final ProxyUtil proxyUtil;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); //Member LAZY 초기화
            order.getDelivery().getAddress(); //Delivery LAZY 초기화

            List<OrderItem> orderItems = order.getOrderItems();

            log.info("before OrderItem Initialized :{}", proxyUtil.isInitProxy(orderItems));
            // 필요한 경우, 여기에서 Hibernate.initialize(o.getItem()); 을 호출하여 강제 초기화할 수 있음

            orderItems.stream().forEach(o -> o.getItem().getName()); // Item 초기화
            boolean isInitialized_after = Hibernate.isInitialized(orderItems);
            log.info("after OrderItem Initialized :{}", proxyUtil.isInitProxy(orderItems));
        }
        return all;
    }
}
