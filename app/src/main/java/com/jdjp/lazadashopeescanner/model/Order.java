package com.jdjp.lazadashopeescanner.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;


@Entity(tableName = "orders", primaryKeys = { "orderNumber", "batchId" })
public class Order {
    @NonNull
    private String orderNumber;
    @NonNull
    private int batchId;
    @NonNull
    private String customerFirstName;
    @NonNull
    private String customerLastName;
    @NonNull
    private double price;
    @NonNull
    private double shippingFee;
    @NonNull
    private double shippingFeeOriginal;
    @NonNull
    private String paymentMethod;
    @NonNull
    private int itemsCount;
    @NonNull
    private String status;
    @NonNull
    private String createdAt;
    @NonNull
    private String updatedAt;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getBatchId() {
        return batchId;
    }

    public void setBatchId(int batchId) {
        this.batchId = batchId;
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

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
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
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
