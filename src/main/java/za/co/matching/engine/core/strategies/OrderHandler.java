package za.co.matching.engine.core.strategies;

import za.co.matching.engine.core.OrderBook;
import za.co.matching.engine.core.TradeRecord;
import za.co.matching.engine.model.Order;

public interface OrderHandler {

    void handleOrder(Order order, OrderBook sellOrderBook, OrderBook buyOrderBook, TradeRecord tradeRecord);

}
