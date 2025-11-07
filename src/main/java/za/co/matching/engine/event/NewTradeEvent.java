package za.co.matching.engine.event;

import za.co.matching.engine.model.Trade;

import java.time.LocalDateTime;

public record NewTradeEvent(Trade trade, LocalDateTime timestamp) {

    public NewTradeEvent(Trade trade) {
        this(trade, LocalDateTime.now());
    }
}
