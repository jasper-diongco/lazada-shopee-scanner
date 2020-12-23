package com.jdjp.lazadashopeescanner.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.jdjp.lazadashopeescanner.model.Order;

import java.util.List;

@Dao
public interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Order order);

    @Query("SELECT * FROM orders WHERE batchId = :batchId")
    LiveData<List<Order>> getAllOrdersByBatchId(int batchId);
}
