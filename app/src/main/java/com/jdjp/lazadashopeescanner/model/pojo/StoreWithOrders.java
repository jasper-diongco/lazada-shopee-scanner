package com.jdjp.lazadashopeescanner.model.pojo;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.model.ShopAccount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoreWithOrders {
    @Embedded
    private ShopAccount shopAccount;

    @Relation(
            parentColumn = "name",
            entityColumn = "storeName"
    )
    private List<Order> orders;

    public List<Order> getSortedOrders(){
        int n = orders.size();
        Order temp;
        for(int i=0; i < n; i++){
            for(int j=1; j < (n-i); j++){
                if(Long.parseLong(orders.get(j-1).getOrderNumber()) > Long.parseLong(orders.get(j).getOrderNumber())){
                    //swap elements
                    temp = orders.get(j-1);
                    orders.set(j-1, orders.get(j));
                    orders.set(j, temp);
                }
            }
        }

        return orders;
    }

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
