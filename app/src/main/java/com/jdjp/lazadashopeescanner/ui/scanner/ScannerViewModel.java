package com.jdjp.lazadashopeescanner.ui.scanner;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.jdjp.lazadashopeescanner.model.Batch;
import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.model.ShopAccount;
import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;
import com.jdjp.lazadashopeescanner.repository.BatchRepository;
import com.jdjp.lazadashopeescanner.repository.OrderRepository;
import com.jdjp.lazadashopeescanner.repository.ShopAccountRepository;

import java.util.List;

public class ScannerViewModel extends AndroidViewModel {
    private ShopAccountRepository shopAccountRepository;
    private BatchRepository batchRepository;
    private OrderRepository orderRepository;

    public ScannerViewModel(@NonNull Application application) {
        super(application);

        shopAccountRepository = new ShopAccountRepository(application);
        batchRepository = new BatchRepository(application);
        orderRepository = new OrderRepository(application);
    }

    public void insertBatch(Batch batch) {
        batchRepository.insert(batch);
    }

    public void insertOrder(Order order) {
        orderRepository.insert(order);
    }

    public LiveData<List<ShopAccount>> getAllShopAccounts() {
        return shopAccountRepository.getAllShopAccounts();
    }

    public LiveData<BatchWithExtraProps> getBatchById(int batchId) {
        return batchRepository.getBatchById(batchId);
    }

}
