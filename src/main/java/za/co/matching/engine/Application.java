package za.co.matching.engine;

import za.co.matching.engine.core.MatchingEngine;
import za.co.matching.engine.model.Order;
import za.co.matching.engine.model.OrderType;
import za.co.matching.engine.model.Side;
import za.co.matching.engine.model.TimeInForce;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Application {


    public static void generateRandomOrders(MatchingEngine matchingEngine) {
        // 1. Single, reusable Random object for better performance and randomness
        Random random = new Random();

        // 2. Control the number of orders with a constant or parameter
        final int NUM_ORDERS = 1_000_000; // Increased count for better simulation

        // Define realistic constraints for orders
        final String SYMBOL = "BTC/USD";
        final double MIN_PRICE = 75.00; // Realistic minimum price
        final double MAX_PRICE = 107.00; // Realistic maximum price
        final int MIN_QUANTITY = 100;
        final int MAX_QUANTITY = 500;

        // Predetermine common probabilities
        final double BUY_PROBABILITY = 0.55; // Slightly more Buy orders (or a desired bias)
        final double LIMIT_PROBABILITY = 0.70; // Most orders are Limit orders

        for (int i = 0; i < NUM_ORDERS; i++) {
            // --- Key Improvement 1: Price and Quantity Generation ---
            // Generates a price within a realistic range
            double price = MIN_PRICE + (MAX_PRICE - MIN_PRICE) * random.nextDouble();
            // Generates an integer quantity
            int quantity = random.nextInt(MAX_QUANTITY - MIN_QUANTITY + 1) + MIN_QUANTITY;

            // --- Key Improvement 2: Bias in Side and Type ---
            Side side = random.nextDouble() < BUY_PROBABILITY ? Side.BUY : Side.SELL;
            OrderType type = random.nextDouble() < LIMIT_PROBABILITY ? OrderType.LIMIT : OrderType.MARKET;

            // Ensure Market orders don't have a price (if your Order object handles null/optional price)
            // If your Order class requires a price, set it to 0 or MAX_VALUE for Market orders
            double orderPrice = (type == OrderType.MARKET) ? 0.0 : price;

            Order order = new Order(
                    UUID.randomUUID().toString(), // Unique ID for the Order
                    UUID.randomUUID().toString(), // Unique ID for the User/Account
                    SYMBOL,
                    orderPrice, // Use the adjusted price
                    quantity,
                    side,
                    type);

            // Optionally, print only every Nth order to avoid excessive console output
            if (i % (NUM_ORDERS / 10) == 0) {
                System.out.println("Generated Order: " + order);
            }

            matchingEngine.match(order);
        }
    }

    public static List<Order> generateTestOrders() {
        return List.of(
                // --- Normal orders to set initial market ---
                new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC", 100d, 10, Side.BUY, OrderType.LIMIT),
                new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC", 101d, 10, Side.BUY, OrderType.LIMIT),
                new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC", 102d, 15, Side.SELL, OrderType.LIMIT),
                new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC", 103d, 10, Side.SELL, OrderType.LIMIT),

                // --- Stop orders: will trigger when price moves ---
                // Stop Sell: triggers when price drops to 99 or below
                new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC", 99d, 10, Side.SELL, OrderType.STOP, 99d),

                // Stop Buy: triggers when price rises to 104 or above
                new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC", 0, 8, Side.BUY, OrderType.STOP, 104d),

                // --- Stop-Limit orders ---
                // Stop-Limit Sell: trigger 99, limit 98.5
                new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC", 98.5, 12, Side.SELL, OrderType.STOP_LIMIT, 99d),

                // Stop-Limit Buy: trigger 104, limit 104.5
                new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC", 104.5, 10, Side.BUY, OrderType.STOP_LIMIT, 104d),

                new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC", 104.5d, 5, Side.SELL, OrderType.MARKET)


        );
    }

    public static void main(String[] args) {

        MatchingEngine matchingEngine = new MatchingEngine();
        Order order1 = new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC", 102d, 5, Side.SELL, OrderType.LIMIT);
        Order order2 = new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC", 103d, 10, Side.SELL, OrderType.LIMIT);
        Order order3 = new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC", 101d, 15, Side.SELL, OrderType.LIMIT);
        Order order4 = new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC", 96d, 15, Side.BUY, OrderType.LIMIT);
        Order order5 = new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC", 104d, 120, Side.BUY, OrderType.LIMIT);
        Order order = new Order(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "BTC", 45d, 100, Side.SELL, OrderType.LIMIT, TimeInForce.FOK);

        long startTime = System.currentTimeMillis();
        matchingEngine.match(order1);
        matchingEngine.match(order2);
        matchingEngine.match(order3);
        matchingEngine.match(order4);
        matchingEngine.match(order5);
        matchingEngine.match(order);

        generateRandomOrders(matchingEngine);
        long endTime = System.currentTimeMillis();


//        generateTestOrders().forEach(matchingEngine::match);

//        System.out.println("matchingEngine.getMarketData() = " + matchingEngine.getMarketData());


        System.out.println("\n\n\n");
        System.out.println("matchingEngine.getStopOrderRegistry().orders().size() = " + matchingEngine.getStopOrderRegistry().orders().size());
        System.out.println("matchingEngine.getSellOrderBook().orders().size() = " + matchingEngine.getSellOrderBook().orders().size());
        System.out.println("matchingEngine.getBuyOrderBook().orders().size() = " + matchingEngine.getBuyOrderBook().orders().size());
        System.out.println("matchingEngine.getTradeRecord().getTrades() = " + matchingEngine.getTradeRecord().trades().size());

        System.out.println("Time = " + (endTime - startTime));

        System.out.println("\n\n\n");
        System.out.println("matchingEngine.getBuyOrderBook() = " + matchingEngine.getBuyOrderBook());
        System.out.println("matchingEngine.getSellOrderBook() = " + matchingEngine.getSellOrderBook());
        System.out.println("matchingEngine.getStopOrderRegistry() = " + matchingEngine.getStopOrderRegistry());
        System.out.println("\n\nTrade Record\n");
//        matchingEngine.getTradeRecord().trades().forEach(System.out::println);

    }
}