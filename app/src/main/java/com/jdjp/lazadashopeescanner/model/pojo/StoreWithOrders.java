package com.jdjp.lazadashopeescanner.model.pojo;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.model.ShopAccount;

import java.util.List;

public class StoreWithOrders {
    @Embedded
    private ShopAccount shopAccount;

    @Relation(
            parentColumn = "name",
            entityColumn = "storeName"
    )
    private List<Order> orders;

    public ShopAccount getShopAccount() {
        return shopAccount;
    }

    public void setShopAccount(ShopAccount shopAccount) {
        this.shopAccount = shopAccount;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
