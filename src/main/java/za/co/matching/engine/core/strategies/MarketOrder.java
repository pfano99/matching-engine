package za.co.matching.engine.core.strategies;

import lombok.extern.slf4j.Slf4j;
import za.co.matching.engine.core.OrderBook;
import za.co.matching.engine.core.TradeRecord;
import za.co.matching.engine.model.Order;
import za.co.matching.engine.model.OrderStatus;
import za.co.matching.engine.model.Side;
import za.co.matching.engine.model.Trade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public record MarketOrder() implements OrderHandler {

    @Override
    public void handleOrder(Order order, OrderBook sellOrderBook, OrderBook buyOrderBook, TradeRecord tradeRecord) {
        List<Order> markedForRemoval;
        if (order.getSide() == Side.BUY) {
            markedForRemoval = trade(order, sellOrderBook, tradeRecord);
        } else {
            markedForRemoval = trade(order, buyOrderBook, tradeRecord);
        }
        removeOrderFromOrderBook(markedForRemoval, sellOrderBook, buyOrderBook);
    }

    private List<Order> trade(Order order, OrderBook buyOrderBook, TradeRecord tradeRecord) {
        List<Order> markedForRemoval = new ArrayList<>();

        for (Order buyOrder : buyOrderBook.orders()) {

            saveToTradeRecord(order, buyOrder, tradeRecord);
            if (order.getRemainingQuantity() < buyOrder.getRemainingQuantity()) {
                markFulfilled(order);
                markPartiallyFilled(buyOrder, order.getRemainingQuantity());
                break;
            } else if (order.getRemainingQuantity() > buyOrder.getRemainingQuantity()) {
                markFulfilled(buyOrder);
                markPartiallyFilled(order, buyOrder.getRemainingQuantity());
                markedForRemoval.add(buyOrder);
            } else {
                markFulfilled(order);
                markFulfilled(buyOrder);
                markedForRemoval.add(buyOrder);
                break;
            }
        }
        return markedForRemoval;

    }


    private void saveToTradeRecord(Order order1, Order order2, TradeRecord tradeRecord) {
        assert order1.getSide() != order2.getSide() : "Cannot trade orders of the same side.....";
        Order sellOrder = order1.getSide() == Side.SELL ? order1 : order2;
        Order buyOrder = order1.getSide() == Side.BUY ? order1 : order2;

        int quantity = buyOrder.getRemainingQuantity();
        if (sellOrder.getRemainingQuantity() < buyOrder.getRemainingQuantity()) {
            quantity = sellOrder.getRemainingQuantity();
        }
        Trade newTrade = new Trade(buyOrder.getOrderId(), sellOrder.getOrderId(), sellOrder.getPrice() == 0.0D ? buyOrder.getPrice() : sellOrder.getPrice(), quantity, LocalDateTime.now());
        log.info("Saving new trade record: <{}>", newTrade);
        tradeRecord.addTrade(newTrade);
    }


    private void markFulfilled(Order order) {
        order.setStatus(OrderStatus.FILLED);
        order.setFilledQuantity(order.getQuantity());
        order.setRemainingQuantity(0);
        log.info("Order FULFILLED : <{}>", order);
    }

    private void markPartiallyFilled(Order order, int fulfilledQuantity) {
        order.setStatus(OrderStatus.PARTIALLY_FULFILLED);
        order.setFilledQuantity(order.getFilledQuantity() + fulfilledQuantity);
        order.setRemainingQuantity(order.getQuantity() - order.getFilledQuantity());
        log.info("Order PARTIALLY_FULFILLED : <{}>", order);
    }

    private void removeOrderFromOrderBook(List<Order> orders, OrderBook sellOrderBook, OrderBook buyOrderBook) {
        for (Order order : orders) {
            log.info("removing order <{}> from <{}> order book", order, order.getSide());
            if (order.getSide() == Side.BUY) {
                buyOrderBook.orders().remove(order);
            } else {
                sellOrderBook.orders().remove(order);
            }
        }
    }


}
