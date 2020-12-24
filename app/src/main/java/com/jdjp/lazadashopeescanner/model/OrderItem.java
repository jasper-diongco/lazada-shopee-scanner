package com.jdjp.lazadashopeescanner.model;

public class OrderItem {
    private String orderItemId;
    private String trackingCode;
    private String shipmentProvider;
    private String deliveryType;

    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }

    public String getShipmentProvider() {
        return shipmentProvider;
    }

    public void setShipmentProvider(String shipmentProvider) {
        this.shipmentProvider = shipmentProvider;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId='" + orderItemId + '\'' +
                ", trackingCode='" + trackingCode + '\'' +
                ", shipmentProvider='" + shipmentProvider + '\'' +
                ", delivery_type='" + deliveryType + '\'' +
                '}';
    }
}
