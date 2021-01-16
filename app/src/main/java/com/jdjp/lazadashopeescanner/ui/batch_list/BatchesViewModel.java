package com.jdjp.lazadashopeescanner.ui.batch_list;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.jdjp.lazadashopeescanner.model.Batch;
import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;
import com.jdjp.lazadashopeescanner.repository.BatchRepository;
import com.jdjp.lazadashopeescanner.repository.OrderRepository;

import java.util.List;

public class BatchesViewModel extends AndroidViewModel {
    private BatchRepository batchRepository;
    private OrderRepository orderRepository;

    public BatchesViewModel(@NonNull Application application) {
        super(application);

        batchRepository = new BatchRepository(application);
        orderRepository = new OrderRepository(application);
    }

    public void deleteBatch(Batch batch) {
        batchRepository.delete(batch);
    }

    public void deleteAllOrdersByBatchId(int batchId) {
        orderRepository.deleteAllByBatchId(batchId);
    }

    public LiveData<List<BatchWithExtraProps>> getAllBatches() {
        return batchRepository.getAllBatches();
    }
}
