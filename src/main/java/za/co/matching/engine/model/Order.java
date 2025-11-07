package za.co.matching.engine.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Order implements Comparable<Order> {
    private String orderId;
    private String customerId;
    private String symbol;
    private double price;
    private int quantity;
    private Side side;
    private LocalDateTime timestamp;
    private int filledQuantity;
    private int remainingQuantity;
    private OrderStatus status;
    private OrderType orderType;
    private double stopPrice;
    private TimeInForce timeInForce;

    public Order(String orderId, String customerId, String symbol, double price, int quantity, Side side, OrderType orderType, double stopPrice, TimeInForce timeInForce) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.side = side;
        this.timestamp = LocalDateTime.now();
        this.filledQuantity = 0;
        this.remainingQuantity = quantity;
        this.status = OrderStatus.NEW;
        this.orderType = orderType;
        this.stopPrice = stopPrice;
        this.timeInForce = timeInForce;
    }

    public Order(String orderId, String customerId, String symbol, double price, int quantity, Side side, OrderType orderType, double stopPrice) {
        this(orderId, customerId, symbol, price, quantity, side, orderType, stopPrice, TimeInForce.GTC);
    }

    public Order(String orderId, String customerId, String symbol, double price, int quantity, Side side, OrderType orderType) {
        this(orderId, customerId, symbol, price, quantity, side, orderType, -1.0d);
    }

    public Order(String orderId, String customerId, String symbol, double price, int quantity, Side side, OrderType orderType, TimeInForce timeInForce) {
        this(orderId, customerId, symbol, price, quantity, side, orderType, -1.0d,  timeInForce);
    }

    @Override
    public int compareTo(Order o) {
        int compare = Double.compare(this.price, o.price);
        if (compare != 0) {
            return compare;
        } else {
            return this.timestamp.compareTo(o.timestamp);
        }
    }
}
