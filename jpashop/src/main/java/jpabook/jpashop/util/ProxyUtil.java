package jpabook.jpashop.util;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

@Component
public class ProxyUtil {

    /**
     * LAZY Loading에서 Proxy 초기화 상태 체크
     *
     * @param Proxy 초기화 상태 확인하고자 하는 객체
     * @return boolean
     */
    public boolean isInitProxy(Object obj) {
        boolean isInitialized = Hibernate.isInitialized(obj);
        return isInitialized;
    }
}
