package za.co.matching.engine.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MarketData {

    private double lastTradePrice;
    private double bestBidPrice;
    private double bestAskPrice;
    private double midPrice;
    private double openPrice;
    private double closePrice;

}
