package com.jdjp.lazadashopeescanner.ui.order_list;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;
import com.jdjp.lazadashopeescanner.repository.BatchRepository;
import com.jdjp.lazadashopeescanner.repository.OrderRepository;

import java.util.List;

public class OrdersViewModel extends AndroidViewModel {
    private OrderRepository orderRepository;
    private BatchRepository batchRepository;

    public OrdersViewModel(@NonNull Application application) {
        super(application);
        orderRepository = new OrderRepository(application);
        batchRepository = new BatchRepository(application);
    }

    public LiveData<BatchWithExtraProps> getBatchById(int batchId) {
        return batchRepository.getBatchById(batchId);
    }

    public LiveData<List<Order>> getAllOrdersByBatchId(int batchId) {
        return orderRepository.getAllOrdersByBatchId(batchId);
    }

}
