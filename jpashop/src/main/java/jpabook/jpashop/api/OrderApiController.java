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

    @GetMapping("/api/v3/orders")
    public Result ordersV3() {
        /*
JPA 구현체로 Hibernate를 사용하는데, 스프링 부트 3버전 부터는 Hibernate 6 버전을 사용하고 있습니다 :)
Hibernate 6버전은 페치 조인 사용 시 자동으로 중복 제거를 하도록 변경되었다고 합니다.
https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#hql-distinct
         */
        List<Order> orders = orderRepository.findAllWithItem();

        for (Order order : orders) {
            //distinct 적용하지
            System.out.println("order ref = " + order + "id = " + order.getId());
        }

        List<OrderDto> result = orders.stream()
                .map(OrderDto::new)
                .collect(toList());

        return new Result(result);
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
        private List<OrderItemDto> orderItems; // 엔티티는 DTO로 다시 한번 래핑이 필요하다.
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
