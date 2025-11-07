package za.co.matching.engine.core;

import za.co.matching.engine.model.Order;

import java.util.Comparator;
import java.util.TreeSet;

public record OrderBook(TreeSet<Order> orders) {

    public OrderBook(Comparator<Order> orderComparator) {
        this(new TreeSet<>(orderComparator));
    }

    public void addOrder(Order order) {
        this.orders.add(order);
    }


}
