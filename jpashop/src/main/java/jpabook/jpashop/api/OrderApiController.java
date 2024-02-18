package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.util.ProxyUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
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
        List<OrderDto> result = orders.stream()
                .map(OrderDto::new)
                .collect(toList());

        return new Result(result);
        //결국 SQL이 나가기 때문에 네트워크를 많이 쓰긴 하는 것이 단점
    }

    @GetMapping("/api/v3.1/orders")
    public Result ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit)
    {   //페이징을 위해 BatchSize를 설정하여 LAZY초기화로 인한 단건 조회를 In절로 최적화한다.
        //1 N M을 1 1 1로 만들어버리는 엄청난 최적화
        //고객 실시간 정보를 빠르게 하려면 캐시 Redis or DB에 정규화해서 놓거나(플랫하게 한 줄로)

        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset,limit);
        List<OrderDto> result = orders.stream()
                .map(OrderDto::new)
                .collect(toList());

        return new Result(result);
        //V3 보다 네트워크 호출은 많지만,
        //V3 보다 네트워크 전송량 자체는 적다. 또 Paging도 가능(paging은 선택권이 없음)
        //극단적으로 10,000건 조회 정도로 생각해보면, V3는 중복 데이터때문에 V3.1이 오히려 최적화 되었다고 볼 수 있다.
        //트레이드 오프 관계를 잘 생각하고, Fetch Join OR Batch_Size 중 해결방법을 결정
    }

    @GetMapping("/api/v4/orders")
    public Result ordersV4() { //List<OrderQueryDto>
        // N+1 문제 존재
        return new Result(orderQueryRepository.findOrderQueryDtos());
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
