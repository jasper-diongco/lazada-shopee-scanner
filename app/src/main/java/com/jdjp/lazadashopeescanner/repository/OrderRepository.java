package com.jdjp.lazadashopeescanner.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.jdjp.lazadashopeescanner.AppDatabase;
import com.jdjp.lazadashopeescanner.dao.OrderDao;
import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.model.pojo.StoreWithOrders;

import java.util.Date;
import java.util.List;


public class OrderRepository {
    private OrderDao orderDao;

    public OrderRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        orderDao = database.orderDao();
    }

    public LiveData<List<Order>> getAllOrdersByBatchId(int batchId) {
        return orderDao.getAllOrdersByBatchId(batchId);
    }

    public LiveData<Order> findOrder(String orderNumber, int batchId) {
        return orderDao.findOrder(orderNumber, batchId);
    }

    public LiveData<List<StoreWithOrders>> getAllStoreWithOrders(int batchId) {
        return orderDao.getAllStoreWithOrders();
    }

    public LiveData<List<Order>> getAllOrders(int batchId, String storeName) {
        return orderDao.getAllOrders(batchId, storeName);
    }

    public void insert(Order order) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                order.setFetchedAt(new Date().getTime());
                orderDao.insert(order);
            }
        }).start();
    }

    public void update(String orderNumber, String status) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                orderDao.update(orderNumber, status);
            }
        }).start();
    }

    public void deleteAllByBatchId(int batchId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                orderDao.deleteAllByBatchId(batchId);
            }
        }).start();
    }
}
