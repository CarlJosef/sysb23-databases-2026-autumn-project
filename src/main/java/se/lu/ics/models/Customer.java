package se.lu.ics.models;

import java.math.BigDecimal;

/**
 * Represents a customer in the Marvix system.
 *
 * Customer objects transfer customer and loyalty data between the data
 * access layer and the application controllers.
 */
public class Customer {

    private final int customerId;
    private final String customerName;
    private final String customerAddress;
    private final String email;
    private final int loyaltyLevelId;
    private final String loyaltyName;
    private final BigDecimal discountPercentage;

    /**
     * Creates a customer from data retrieved from the database.
     *
     * @param customerId         unique customer ID
     * @param customerName       customer name
     * @param customerAddress    customer delivery address
     * @param email              customer email address
     * @param loyaltyLevelId     loyalty level ID
     * @param loyaltyName        loyalty level name
     * @param discountPercentage loyalty discount percentage
     */
    public Customer(
            int customerId,
            String customerName,
            String customerAddress,
            String email,
            int loyaltyLevelId,
            String loyaltyName,
            BigDecimal discountPercentage) {

        this.customerId = customerId;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.email = email;
        this.loyaltyLevelId = loyaltyLevelId;
        this.loyaltyName = loyaltyName;
        this.discountPercentage = discountPercentage;
    }

    /** @return unique customer ID */
    public int getCustomerId() {
        return customerId;
    }

    /** @return customer name */
    public String getCustomerName() {
        return customerName;
    }

    /** @return customer delivery address */
    public String getCustomerAddress() {
        return customerAddress;
    }

    /** @return customer email address */
    public String getEmail() {
        return email;
    }

    /** @return loyalty level ID */
    public int getLoyaltyLevelId() {
        return loyaltyLevelId;
    }

    /** @return loyalty level name */
    public String getLoyaltyName() {
        return loyaltyName;
    }

    /** @return loyalty discount percentage */
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
}
