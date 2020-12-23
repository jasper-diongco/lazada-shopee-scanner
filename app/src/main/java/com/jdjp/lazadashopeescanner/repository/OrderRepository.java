package com.jdjp.lazadashopeescanner.repository;

import android.app.Application;

import com.jdjp.lazadashopeescanner.AppDatabase;
import com.jdjp.lazadashopeescanner.dao.OrderDao;
import com.jdjp.lazadashopeescanner.model.Order;


public class OrderRepository {
    private OrderDao orderDao;

    public OrderRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        orderDao = database.orderDao();
    }

    public void insert(Order order) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                orderDao.insert(order);
            }
        }).start();
    }
}
