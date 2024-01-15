package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.ItemRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest
public class ItemServiceTest {

    @Autowired private ItemRepository itemRepository;
    @Autowired private ItemService itemService;

    @Test
    public void 상품_저장() throws Exception {
        //given
        Item item = new Book();
        item.setName("book");

        //when
        itemService.save(item);
        Item findItem = itemRepository.findOne(item.getId());

        //then
        assertThat(findItem.getName()).isEqualTo("book");
        /*
        🌟지금 save한 book을 저장하는 영속성 컨텍스트와 itemService에서 조회하는 영속성 컨텍스트가 서로 다릅니다.
         */
    }

    @Test
    public void 수량_증가() throws Exception {
        //given
        Item item1 = new Book();
        Item item2 = new Book();
        Item item3 = new Book();

        //when

        //item1.getStockQuantity == 7
        item1.addStock(5);
        item1.addStock(2);
        //item2.getStockQuantity == 4
        item2.addStock(1);
        item2.addStock(3);
        //item2.getStockQuantity == 10
        item3.addStock(5);
        item3.addStock(5);

        //then
        assertThat(item1.getStockQuantity()).isEqualTo(7);
        assertThat(item2.getStockQuantity()).isEqualTo(4);
        assertThat(item3.getStockQuantity()).isEqualTo(10);
    }

    @Test
    public void 수량_감소() throws Exception {
        //given
        Item item1 = new Book();
        Item item2 = new Book();

        //when
        item1.addStock(10);
        item1.removeStock(5);
        item2.addStock(20);
        item2.removeStock(20);

        //then
        assertThat(item1.getStockQuantity()).isEqualTo(5);
        assertThat(item2.getStockQuantity()).isEqualTo(0);
    }

    @Test(expected = NotEnoughStockException.class) //예외를 감지하는 것이 좋음(💡AssertThatThrownBy 공부해볼 💡)
    public void 수량_감소_예외_발생() throws Exception {
        //given
        Item item = new Book();

        //when
        item.addStock(1);
        item.removeStock(2);
        //then
        Assertions.fail("예외가 생성되어야 합니다.");

    }
}