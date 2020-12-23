package com.jdjp.lazadashopeescanner.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.jdjp.lazadashopeescanner.model.Batch;
import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;

import java.util.List;

@Dao
public interface BatchDao {
    @Insert
    long insert(Batch batch);

    @Query("SELECT batches.*, (SELECT COUNT(*) FROM orders WHERE batchId = batches.batchId) AS 'scanCount', (SELECT COUNT(*) FROM orders WHERE batchId = batches.batchId AND status = 'ready_to_ship') AS 'readyToShipCount' FROM batches WHERE batchId = :batchId")
    LiveData<BatchWithExtraProps> getBatchById(int batchId);


    @Query("SELECT * FROM batches ORDER BY createdAt DESC")
    LiveData<List<Batch>> getAllBatches();
}
