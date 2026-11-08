package se.lu.ics.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents order summary information used by the Marvix application.
 */
public class Order {

    private final int orderId;
    private final LocalDateTime orderDate;
    private final int customerId;
    private final String customerName;
    private final String loyaltyName;
    private final BigDecimal discountPercentage;
    private final BigDecimal subtotal;
    private final BigDecimal orderTotal;

    /**
     * Creates an order from data retrieved from the database.
     *
     * @param orderId            unique order ID
     * @param orderDate          date and time when the order was created
     * @param customerId         ID of the customer who owns the order
     * @param customerName       name of the customer who owns the order
     * @param loyaltyName        customer's loyalty level name
     * @param discountPercentage discount percentage applied to the order
     * @param subtotal           order subtotal before discount
     * @param orderTotal         final order total after discount
     */
    public Order(
            int orderId,
            LocalDateTime orderDate,
            int customerId,
            String customerName,
            String loyaltyName,
            BigDecimal discountPercentage,
            BigDecimal subtotal,
            BigDecimal orderTotal) {

        this.orderId = orderId;
        this.orderDate = orderDate;
        this.customerId = customerId;
        this.customerName = customerName;
        this.loyaltyName = loyaltyName;
        this.discountPercentage = discountPercentage;
        this.subtotal = subtotal;
        this.orderTotal = orderTotal;
    }

    /** @return unique order ID */
    public int getOrderId() {
        return orderId;
    }

    /** @return date and time when the order was created */
    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    /** @return ID of the customer who owns the order */
    public int getCustomerId() {
        return customerId;
    }

    /** @return name of the customer who owns the order */
    public String getCustomerName() {
        return customerName;
    }

    /** @return customer's loyalty level name */
    public String getLoyaltyName() {
        return loyaltyName;
    }

    /** @return discount percentage applied to the order */
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    /** @return order subtotal before discount */
    public BigDecimal getSubtotal() {
        return subtotal;
    }

    /** @return final order total after discount */
    public BigDecimal getOrderTotal() {
        return orderTotal;
    }
}
