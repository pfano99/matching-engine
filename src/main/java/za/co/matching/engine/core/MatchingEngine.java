package za.co.matching.engine.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import za.co.matching.engine.core.strategies.LimitOrder;
import za.co.matching.engine.core.strategies.MarketOrder;
import za.co.matching.engine.model.Order;
import za.co.matching.engine.model.OrderType;
import za.co.matching.engine.model.Side;
import za.co.matching.engine.model.Trade;

import java.util.Comparator;

@Slf4j
public class MatchingEngine {

    @Getter
    private final OrderBook buyOrderBook = new OrderBook(Comparator.naturalOrder());
    @Getter
    final OrderBook sellOrderBook = new OrderBook(Comparator.reverseOrder());
    @Getter
    final TradeRecord tradeRecord = new TradeRecord(Comparator.naturalOrder(), this);
    @Getter
    final OrderBook stopOrderRegistry = new OrderBook(Comparator.reverseOrder());


    public void match(Order order) {

        switch (order.getOrderType()) {
            case LIMIT:
                new LimitOrder().handleOrder(order, sellOrderBook, buyOrderBook, tradeRecord);
                break;
            case MARKET:
                new MarketOrder().handleOrder(order, sellOrderBook, buyOrderBook, tradeRecord);
                break;
            case STOP:
            case STOP_LIMIT:
                stopOrderRegistry.addOrder(order);
                break;
            default:
                log.warn("Unknown order type {}", order.getOrderType());
        }

    }

    public MarketData getMarketData() {
        return new MarketData(
                tradeRecord.trades().getLast().getPrice(),
                buyOrderBook.orders().getLast().getPrice(),
                sellOrderBook.orders().getLast().getPrice(),
                (buyOrderBook.orders().getLast().getPrice() +
                        sellOrderBook.orders().getLast().getPrice()) / 2,
                0,
                0
        );
    }


    public void triggerStop(Trade trade) {
        stopOrderRegistry.orders().forEach(order -> {
            if (order.getOrderType().equals(OrderType.STOP)) {
                if (trade.getPrice() <= order.getStopPrice() && order.getSide().equals(Side.SELL)) {
                    log.info("Executing STOP_SELL order trigger reached ==> <tigger:{}> && <market:{}>", order.getStopPrice(), trade.getPrice());
                    order.setOrderType(OrderType.MARKET);
                    match(order);
                } else if (trade.getPrice() >= order.getStopPrice() && order.getSide().equals(Side.BUY)) {
                    log.info("Executing STOP_BUY order trigger reached ==> <tigger:{}> && <market:{}>", order.getStopPrice(), trade.getPrice());
                    order.setOrderType(OrderType.MARKET);
                    match(order);
                }
            } else if (order.getOrderType().equals(OrderType.STOP_LIMIT)) {
                if (trade.getPrice() <= order.getStopPrice() && order.getSide().equals(Side.SELL)) {
                    log.info("Executing STOP_LIMIT_SELL order trigger reached ==> <tigger:{}> && <market:{}>", order.getStopPrice(), trade.getPrice());
                    order.setOrderType(OrderType.LIMIT);
                    match(order);
                } else if (trade.getPrice() >= order.getStopPrice() && order.getSide().equals(Side.BUY)) {
                    log.info("Executing STOP_LIMIT_BUY order trigger reached ==> <tigger:{}> && <market:{}>", order.getStopPrice(), trade.getPrice());
                    order.setOrderType(OrderType.LIMIT);
                    match(order);
                }
            }

        });

    }
}
