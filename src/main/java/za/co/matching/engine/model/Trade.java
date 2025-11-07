package za.co.matching.engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Trade implements Comparable<Trade> {
    private String buyOrderId;
    private String sellOrderId;
    private double price;
    private int quantity;
    private LocalDateTime timestamp;

    @Override
    public int compareTo(Trade o) {
        int time = this.timestamp.compareTo(o.timestamp);
        if (time != 0) {
            return time;
        }

        if (this.price != o.price) {
            return Double.compare(this.price, o.price);
        }

        if (this.quantity != o.quantity) {
            return Integer.compare(this.quantity, o.quantity);
        }

        return -1;
    }
}
