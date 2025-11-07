package za.co.matching.engine.core.strategies;

import lombok.extern.slf4j.Slf4j;
import za.co.matching.engine.core.OrderBook;
import za.co.matching.engine.core.TradeRecord;
import za.co.matching.engine.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
public record LimitOrder() implements OrderHandler {


    @Override
    public void handleOrder(Order order, OrderBook sellOrderBook, OrderBook buyOrderBook, TradeRecord tradeRecord) {
        if (order.getSide().equals(Side.BUY)) {
            handleBUYOrder(order, sellOrderBook, buyOrderBook, tradeRecord);
        } else {
            handleSELLOrder(order, sellOrderBook, buyOrderBook, tradeRecord);
        }
    }


    private void handleSELLOrder(Order order, OrderBook sellOrderBook, OrderBook buyOrderBook, TradeRecord tradeRecord) {
        List<Order> markedForRemoval = new ArrayList<>();
        int satisfiedFOKAmount = 0;
        for (Order buyOrder : buyOrderBook.orders()) {

            if (order.getPrice() <= buyOrder.getPrice()) {

                if (order.getTimeInForce().equals(TimeInForce.FOK)) {
                    satisfiedFOKAmount += buyOrder.getRemainingQuantity();
                    if (satisfiedFOKAmount >= order.getQuantity()) {
                        handleFOK(order, buyOrderBook.orders(), tradeRecord, markedForRemoval);
                        break;
                    }

                } else {
                    if (order.getRemainingQuantity() < buyOrder.getRemainingQuantity()) {
                        saveToTradeRecord(order, buyOrder, tradeRecord);
                        markPartiallyFulfilled(buyOrder, order.getRemainingQuantity());
                        markFulfilled(order);
                        break;
                    } else if (order.getRemainingQuantity() > buyOrder.getRemainingQuantity()) {
                        saveToTradeRecord(order, buyOrder, tradeRecord);
                        markPartiallyFulfilled(order, buyOrder.getRemainingQuantity());
                        markFulfilled(buyOrder);
                        markedForRemoval.add(buyOrder);
                    } else {
                        saveToTradeRecord(order, buyOrder, tradeRecord);
                        markFulfilled(order);
                        markFulfilled(buyOrder);
                        markedForRemoval.add(buyOrder);
                        break;
                    }
                }
            } else {
                if (!order.getTimeInForce().equals(TimeInForce.IOC) && !order.getTimeInForce().equals(TimeInForce.FOK)) {
                    saveToOrderBook(order, sellOrderBook, buyOrderBook);
                } else {
                    log.info("Marking IOC order as cancelled, failed to fulfill order: {}", order);
                    order.setStatus(OrderStatus.CANCELLED);
                }
                break;
            }
        }

        if (!order.getStatus().equals(OrderStatus.FILLED) && (!order.getTimeInForce().equals(TimeInForce.IOC) && !order.getTimeInForce().equals(TimeInForce.FOK))) {
            saveToOrderBook(order, sellOrderBook, buyOrderBook);
        }

        if (order.getStatus().equals(OrderStatus.NEW) && (order.getTimeInForce().equals(TimeInForce.IOC) || order.getTimeInForce().equals(TimeInForce.FOK))) {
            log.info("Marking {} order as cancelled, failed to fulfill order: {}", order.getTimeInForce(), order);
            order.setStatus(OrderStatus.CANCELLED);
        }

        if (!markedForRemoval.isEmpty()) {
            removeOrderFromOrderBook(markedForRemoval, sellOrderBook, buyOrderBook);
        }


    }

    private void handleBUYOrder(Order order, OrderBook sellOrderBook, OrderBook buyOrderBook, TradeRecord tradeRecord) {
        List<Order> markedForRemoval = new ArrayList<>();

        int satisfiedFOKAmount = 0;

        //Go through sell order book and match
        for (Order sellOrder : sellOrderBook.orders()) {
            if (order.getPrice() >= sellOrder.getPrice()) {

                if (order.getTimeInForce().equals(TimeInForce.FOK)) {
                    satisfiedFOKAmount += sellOrder.getRemainingQuantity();
                    if (satisfiedFOKAmount >= order.getQuantity()) {
                        handleFOK(order, sellOrderBook.orders(), tradeRecord, markedForRemoval);
                        break;
                    }

                } else {
                    if (sellOrder.getRemainingQuantity() < order.getRemainingQuantity()) {
                        saveToTradeRecord(order, sellOrder, tradeRecord);
                        markPartiallyFulfilled(order, sellOrder.getRemainingQuantity());
                        markFulfilled(sellOrder);
                        markedForRemoval.add(sellOrder);

                    } else if (sellOrder.getRemainingQuantity() > order.getRemainingQuantity()) {
                        saveToTradeRecord(order, sellOrder, tradeRecord);
                        markPartiallyFulfilled(sellOrder, order.getRemainingQuantity());
                        markFulfilled(order);
                        break;
                    } else {
                        saveToTradeRecord(order, sellOrder, tradeRecord);
                        markedForRemoval.add(sellOrder);
                        markFulfilled(order);
                        markFulfilled(sellOrder);
                        break;
                    }
                }
            } else {
                if (!order.getTimeInForce().equals(TimeInForce.IOC) && !order.getTimeInForce().equals(TimeInForce.FOK)) {
                    saveToOrderBook(order, sellOrderBook, buyOrderBook);
                } else {
                    log.info("Marking IOC order as cancelled, failed to fulfill order: {}", order);
                    order.setStatus(OrderStatus.CANCELLED);
                }
                break;
            }

        }
        if (!order.getStatus().equals(OrderStatus.FILLED) && (!order.getTimeInForce().equals(TimeInForce.IOC) && !order.getTimeInForce().equals(TimeInForce.FOK))) {
            saveToOrderBook(order, sellOrderBook, buyOrderBook);
        }

        if (!markedForRemoval.isEmpty()) {
            removeOrderFromOrderBook(markedForRemoval, sellOrderBook, buyOrderBook);
        }

        if (order.getStatus().equals(OrderStatus.NEW) && (order.getTimeInForce().equals(TimeInForce.IOC) || order.getTimeInForce().equals(TimeInForce.FOK))) {
            log.info("Marking {} order as cancelled, failed to fulfill order: {}", order.getTimeInForce(), order);
            order.setStatus(OrderStatus.CANCELLED);
        }

    }

    private void saveToOrderBook(Order order, OrderBook sellOrderBook, OrderBook buyOrderBook) {
        if (!order.getStatus().equals(OrderStatus.FILLED)) {
            log.info("Failed to find match that can fulfil order requirement for order <{}>", order);
            log.info("Saving order: <{}> to <{}> order book", order, order.getSide());
            if (order.getSide().equals(Side.BUY)) {
                buyOrderBook.addOrder(order);
            } else {
                sellOrderBook.addOrder(order);
            }
        } else {
            log.warn("Cannot save order: <{}> to <{}>order book because order has already been fulfilled", order, order.getSide());
        }
    }

    private void saveToTradeRecord(Order order1, Order order2, TradeRecord tradeRecord) {
        assert order1.getSide() != order2.getSide() : "Cannot trade orders of the same side.....";
        Order sellOrder = order1.getSide().equals(Side.SELL) ? order1 : order2;
        Order buyOrder = order1.getSide().equals(Side.BUY) ? order1 : order2;

        int quantity = buyOrder.getRemainingQuantity();
        if (sellOrder.getRemainingQuantity() < buyOrder.getRemainingQuantity()) {
            quantity = sellOrder.getRemainingQuantity();
        }
        Trade newTrade = new Trade(buyOrder.getOrderId(), sellOrder.getOrderId(), sellOrder.getPrice(), quantity, LocalDateTime.now());
        log.info("Saving new trade record: <{}>", newTrade);
        tradeRecord.addTrade(newTrade);
    }


    private void removeOrderFromOrderBook(List<Order> orders, OrderBook sellOrderBook, OrderBook buyOrderBook) {
        for (Order order : orders) {
            log.info("removing order <{}> from <{}> order book", order, order.getSide());
            if (order.getSide().equals(Side.BUY)) {
                buyOrderBook.orders().remove(order);
            } else {
                sellOrderBook.orders().remove(order);
            }
        }
    }

    private void markFulfilled(Order order) {
        order.setStatus(OrderStatus.FILLED);
        order.setFilledQuantity(order.getQuantity());
        order.setRemainingQuantity(0);
        log.info("Order FULFILLED : <{}>", order);
    }

    private void markPartiallyFulfilled(Order order, int fulfilledQuantity) {
        order.setStatus(OrderStatus.PARTIALLY_FULFILLED);
        order.setFilledQuantity(order.getFilledQuantity() + fulfilledQuantity);
        order.setRemainingQuantity(order.getQuantity() - order.getFilledQuantity());
        log.info("Order PARTIALLY_FULFILLED : <{}>", order);
    }

    private void handleFOK(Order fokOrder, TreeSet<Order> orders, TradeRecord tradeRecord, List<Order> markedForRemoval) {
        // TODO: Handle this better:
        // TODO: Logic is the same as the one from both handle buy and handle sell order
        // TODO: Filter the order make sure only orders with correct price range
        log.info("Handling FOK order: {}", fokOrder);

        if (fokOrder.getSide().equals(Side.BUY)) {
            for (Order otherOrder : orders) {
                if (otherOrder.getRemainingQuantity() < fokOrder.getRemainingQuantity()) {
                    saveToTradeRecord(fokOrder, otherOrder, tradeRecord);
                    markPartiallyFulfilled(fokOrder, otherOrder.getRemainingQuantity());
                    markFulfilled(otherOrder);
                    markedForRemoval.add(otherOrder);

                } else if (otherOrder.getRemainingQuantity() > fokOrder.getRemainingQuantity()) {
                    saveToTradeRecord(fokOrder, otherOrder, tradeRecord);
                    markPartiallyFulfilled(otherOrder, fokOrder.getRemainingQuantity());
                    markFulfilled(fokOrder);
                    break;
                } else {
                    saveToTradeRecord(fokOrder, otherOrder, tradeRecord);
                    markedForRemoval.add(otherOrder);
                    markFulfilled(fokOrder);
                    markFulfilled(otherOrder);
                    break;
                }
            }
        } else {
            for (Order otherOrder : orders) {
                if (fokOrder.getRemainingQuantity() < otherOrder.getRemainingQuantity()) {
                    saveToTradeRecord(fokOrder, otherOrder, tradeRecord);
                    markPartiallyFulfilled(otherOrder, fokOrder.getRemainingQuantity());
                    markFulfilled(fokOrder);
                    break;
                } else if (fokOrder.getRemainingQuantity() > otherOrder.getRemainingQuantity()) {
                    saveToTradeRecord(fokOrder, otherOrder, tradeRecord);
                    markPartiallyFulfilled(fokOrder, otherOrder.getRemainingQuantity());
                    markFulfilled(otherOrder);
                    markedForRemoval.add(otherOrder);
                } else {
                    saveToTradeRecord(fokOrder, otherOrder, tradeRecord);
                    markFulfilled(fokOrder);
                    markFulfilled(otherOrder);
                    markedForRemoval.add(otherOrder);
                    break;
                }
            }
        }
        if (fokOrder.getStatus().equals(OrderStatus.FILLED)) {
            log.info("FOK order successfully traded");
        } else {
            log.warn("something terribly happened FOK not traded successfully, FOK order: {}", fokOrder);
        }

    }

}
