package se.lu.ics.models;

import java.math.BigDecimal;

/**
 * Represents a product line belonging to an order in the Marvix system.
 *
 * Each order line identifies the order, its line number, the ordered product,
 * the quantity and the unit price used for the line.
 */
public class OrderLine {

    private final int orderId;
    private final int lineNumber;
    private final int productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal unitPrice;

    /**
     * Creates an order line from data retrieved from the database.
     *
     * @param orderId     ID of the order containing the line
     * @param lineNumber  line number within the order
     * @param productId   ID of the product on the line
     * @param productName name of the product on the line
     * @param quantity    ordered product quantity
     * @param unitPrice   unit price used for the order line
     */
    public OrderLine(
            int orderId,
            int lineNumber,
            int productId,
            String productName,
            int quantity,
            BigDecimal unitPrice) {

        this.orderId = orderId;
        this.lineNumber = lineNumber;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    /** @return ID of the order containing the line */
    public int getOrderId() {
        return orderId;
    }

    /** @return line number within the order */
    public int getLineNumber() {
        return lineNumber;
    }

    /** @return ID of the product on the line */
    public int getProductId() {
        return productId;
    }

    /** @return name of the product on the line */
    public String getProductName() {
        return productName;
    }

    /** @return ordered product quantity */
    public int getQuantity() {
        return quantity;
    }

    /** @return unit price used for the order line */
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /**
     * Calculates the total value of the order line.
     *
     * @return unit price multiplied by quantity
     */
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
