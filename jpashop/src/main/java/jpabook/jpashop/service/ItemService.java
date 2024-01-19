package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * ItemService는 단순하게 ItemRepository에 단순하게 위임만 하는 클래스
 *
 * Question: 굳이 위임만 하는 클래스를 굳이 구현해야할까?
 * Answer: 컨트롤러에서 ItemRepository 바로 접근해서 써도 큰 문제 X
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional /*(readOnly = false)*/
    public void save(Item item) {
        itemRepository.save(item);
    }

    @Transactional
    public void updateItem(Long itemId, String name, int price, int stockQuantity) {
        Item findItem = itemRepository.findOne(itemId); // ID 기반으로 실제 DB에 있는 '영속상태 엔티티'르 찾아옴.
        //findItem.change(..,...,..); 와 같이 엔티티에 의미있는 비즈니스 로직으로 하세요.

        findItem.setName(name);
        findItem.setPrice(price);
        findItem.setStockQuantity(stockQuantity);
        /* Dirty Checking으로 자동으로 JPA가 업데이트 해줌 (좋은 방법) */
        /* Merge와 달리 업데이트 칠 필드들만 변경 감지하여 선택저으로 변경 가능 */
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long id) {
        return itemRepository.findOne(id);
    }
}
