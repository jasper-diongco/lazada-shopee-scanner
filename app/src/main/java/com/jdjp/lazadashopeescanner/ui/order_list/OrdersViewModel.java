package com.jdjp.lazadashopeescanner.ui.order_list;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.model.ShopAccount;
import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;
import com.jdjp.lazadashopeescanner.model.pojo.StoreWithOrders;
import com.jdjp.lazadashopeescanner.repository.BatchRepository;
import com.jdjp.lazadashopeescanner.repository.OrderRepository;
import com.jdjp.lazadashopeescanner.repository.ShopAccountRepository;

import java.util.List;

public class OrdersViewModel extends AndroidViewModel {
    private OrderRepository orderRepository;
    private BatchRepository batchRepository;
    private ShopAccountRepository shopAccountRepository;

    public OrdersViewModel(@NonNull Application application) {
        super(application);
        orderRepository = new OrderRepository(application);
        batchRepository = new BatchRepository(application);
        shopAccountRepository = new ShopAccountRepository(application);
    }

    public LiveData<BatchWithExtraProps> getBatchById(int batchId) {
        return batchRepository.getBatchById(batchId);
    }

    public LiveData<List<Order>> getAllOrdersByBatchId(int batchId) {
        return orderRepository.getAllOrdersByBatchId(batchId);
    }

    public LiveData<List<Order>> getAllOrders(int batchId, String storeName) {
        return orderRepository.getAllOrders(batchId, storeName);
    }


    public LiveData<List<StoreWithOrders>> getAllStoreWithOrders(int batchId) {
        return orderRepository.getAllStoreWithOrders(batchId);
    }

    public LiveData<List<ShopAccount>> getAllShopAccounts() {
        return shopAccountRepository.getAllShopAccounts();
    }

    public void insertOrder(Order order) {
        orderRepository.insert(order);
    }



}
