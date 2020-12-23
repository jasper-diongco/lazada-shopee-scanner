package com.jdjp.lazadashopeescanner.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.jdjp.lazadashopeescanner.AppDatabase;
import com.jdjp.lazadashopeescanner.dao.BatchDao;
import com.jdjp.lazadashopeescanner.model.Batch;
import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;

import java.util.Date;
import java.util.List;

public class BatchRepository {
    private BatchDao batchDao;

    public BatchRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        batchDao = database.batchDao();
    }

    public void insert(Batch batch) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                batch.setCreatedAt(new Date().getTime());
                long insertId =  batchDao.insert(batch);
                batch.setBatchId((int)insertId);
            }
        }).start();
    }

    public LiveData<BatchWithExtraProps> getBatchById(int batchId) {
        return batchDao.getBatchById(batchId);
    }

    public LiveData<List<Batch>> getAllBatches() {
        return batchDao.getAllBatches();
    }
}
