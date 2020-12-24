package com.jdjp.lazadashopeescanner.ui.batch_list;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;
import com.jdjp.lazadashopeescanner.repository.BatchRepository;

import java.util.List;

public class BatchesViewModel extends AndroidViewModel {
    private BatchRepository batchRepository;

    public BatchesViewModel(@NonNull Application application) {
        super(application);

        batchRepository = new BatchRepository(application);
    }

    public LiveData<List<BatchWithExtraProps>> getAllBatches() {
        return batchRepository.getAllBatches();
    }
}
