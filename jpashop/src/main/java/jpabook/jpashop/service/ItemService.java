package jpabook.jpashop.service;

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

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long id) {
        return itemRepository.findOne(id);
    }
}
