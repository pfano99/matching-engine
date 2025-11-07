package za.co.matching.engine.core;

import org.junit.jupiter.api.Test;
import za.co.matching.engine.model.Order;
import za.co.matching.engine.model.OrderType;
import za.co.matching.engine.model.Side;

import java.util.Comparator;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderBookTest {

    @Test
    void orderShouldSortedLowestPriceToHighestPriceNaturalOrder() {

        OrderBook orderBook = new OrderBook(Comparator.naturalOrder());
        orderBook.addOrder(new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC/USD", 450, 2000, Side.BUY, OrderType.LIMIT));
        orderBook.addOrder(new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC/USD", 400, 1070, Side.BUY, OrderType.LIMIT));
        orderBook.addOrder(new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC/USD", 200, 1550, Side.BUY, OrderType.LIMIT));

        assertEquals(200, orderBook.orders().getFirst().getPrice());

    }

    @Test
    void orderShouldSortedHighestPriceToLowestPriceReversedOrder() {

        OrderBook orderBook = new OrderBook(Comparator.reverseOrder());
        orderBook.addOrder(new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC/USD", 450, 2000, Side.BUY, OrderType.LIMIT));
        orderBook.addOrder(new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC/USD", 400, 1070, Side.BUY, OrderType.LIMIT));
        orderBook.addOrder(new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC/USD", 200, 1550, Side.BUY, OrderType.LIMIT));

        assertEquals(450, orderBook.orders().getFirst().getPrice());

    }


}