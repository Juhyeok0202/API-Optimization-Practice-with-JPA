package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.util.ProxyUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

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

    @GetMapping("/api/v2/orders")
    public Result ordersV2() {
        //Order 조회( 2개 )
        List<OrderDto> collect = orderRepository.findAllByString(new OrderSearch()).stream()
                .map(OrderDto::new)
                .collect(toList());

        return new Result(collect);
    }

    @Data
    static class OrderDto {
        /*
        ⚠️  DTO 안에 Entity가 있으면 XXXX rapping마저도 XXXX
             이유: OrderItem이라는 엔티티의 스펙이 노출 되어버림
          */
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems; //✅
        //        private List<OrderItem> orderItems; //OrderItem 조차도 DTO로 변환 필수(엔티티 스펙)

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(toList());


//            order.getOrderItems().stream().forEach(o->o.getItem().getName()); //OrderItems는 엔티티니까 LAZY 초기화 필요
//            orderItems = order.getOrderItems();
        }
    }

    @Getter
    static class OrderItemDto {

        /*
        ITEM 엔티티 one depth가 더 있지만, 클라이언트에게 필요한 데이터만 꺼내는 방법도 고려해보자.
         */
        private String itemName;
        private int orderPrice;
        private int count;
        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

    @Getter
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }


}
