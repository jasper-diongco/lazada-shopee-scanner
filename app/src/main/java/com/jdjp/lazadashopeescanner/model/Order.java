package com.jdjp.lazadashopeescanner.model;

import java.util.Arrays;

public class Order {
    private String orderNumber;
    private String customerFirstName;
    private String customerLastName;
    private double price;
    private double shippingFee;
    private double shippingFeeOriginal;
    private String paymentMethod;
    private int itemsCount;
    private String[] statuses;
    private String createdAt;
    private String updatedAt;

    public String getStatusesString() {
        String result = "";

        for (int i = 0 ; i < statuses.length; i++) {
            result += statuses[i];

            if(i != statuses.length - 1) {
                result += ", ";
            }
        }

        return result;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public void setCustomerFirstName(String customerFirstName) {
        this.customerFirstName = customerFirstName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public double getShippingFeeOriginal() {
        return shippingFeeOriginal;
    }

    public void setShippingFeeOriginal(double shippingFeeOriginal) {
        this.shippingFeeOriginal = shippingFeeOriginal;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public int getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(int itemsCount) {
        this.itemsCount = itemsCount;
    }

    public String[] getStatuses() {
        return statuses;
    }

    public void setStatuses(String[] statuses) {
        this.statuses = statuses;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderNumber='" + orderNumber + '\'' +
                ", customerFirstName='" + customerFirstName + '\'' +
                ", customerLastName='" + customerLastName + '\'' +
                ", price=" + price +
                ", shippingFee=" + shippingFee +
                ", shippingFeeOriginal=" + shippingFeeOriginal +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", itemsCount=" + itemsCount +
                ", statuses=" + Arrays.toString(statuses) +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
