package com.jdjp.lazadashopeescanner.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.jdjp.lazadashopeescanner.model.Batch;
import com.jdjp.lazadashopeescanner.model.pojo.BatchWithExtraProps;

import java.util.List;

@Dao
public interface BatchDao {
    @Insert
    long insert(Batch batch);

    @Delete
    void delete(Batch batch);

    @Query("SELECT batches.*, (SELECT COUNT(*) FROM orders WHERE batchId = batches.batchId) AS 'scanCount', (SELECT COUNT(*) FROM orders WHERE batchId = batches.batchId AND status LIKE '%ready_to_ship%') AS 'readyToShipCount', (SELECT COUNT(*) FROM orders WHERE batchId = batches.batchId AND status LIKE '%canceled%' AND status NOT LIKE '%ready_to_ship%') AS 'canceledCount' FROM batches WHERE batchId = :batchId")
    LiveData<BatchWithExtraProps> getBatchById(int batchId);


    @Query("SELECT batches.*, (SELECT COUNT(*) FROM orders WHERE batchId = batches.batchId) AS 'scanCount', (SELECT COUNT(*) FROM orders WHERE batchId = batches.batchId AND status LIKE '%ready_to_ship%') AS 'readyToShipCount', (SELECT COUNT(*) FROM orders WHERE batchId = batches.batchId AND status LIKE '%canceled%' AND status NOT LIKE '%ready_to_ship%') AS 'canceledCount' FROM batches ORDER BY createdAt DESC")
    LiveData<List<BatchWithExtraProps>> getAllBatches();
}
