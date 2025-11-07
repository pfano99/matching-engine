package za.co.matching.engine.core;

import lombok.extern.slf4j.Slf4j;
import za.co.matching.engine.model.Trade;

import java.util.Comparator;
import java.util.TreeSet;

@Slf4j
public record TradeRecord(TreeSet<Trade> trades, MatchingEngine matchingEngine) {

    public TradeRecord(Comparator<Trade> tradeComparator, MatchingEngine matchingEngine) {
        this(new TreeSet<>(tradeComparator), matchingEngine);
    }

    public void addTrade(Trade trade) {
        this.trades.add(trade);

        log.info("Emitting new trade record: <{}>", trade);
        //Todo: emit new order event
        matchingEngine.triggerStop(trade);
    }

}
