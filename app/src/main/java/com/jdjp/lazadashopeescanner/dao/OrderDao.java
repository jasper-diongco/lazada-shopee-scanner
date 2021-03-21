package com.jdjp.lazadashopeescanner.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;


import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.model.pojo.StoreWithOrders;

import java.util.List;

@Dao
public interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Order order);

    @Query("UPDATE orders SET status = :status WHERE orderNumber = :orderNumber")
    void update(String orderNumber, String status);

    @Query("DELETE FROM orders WHERE batchId = :batchId")
    void deleteAllByBatchId(int batchId);

    @Query("SELECT * FROM orders WHERE orderNumber = :orderNumber AND batchId = :batchId LIMIT 1")
    LiveData<Order> findOrder(String orderNumber, int batchId);

    @Query("SELECT * FROM orders WHERE batchId = :batchId ORDER BY orderNumber")
    LiveData<List<Order>> getAllOrdersByBatchId(int batchId);

    @Query("SELECT * FROM orders WHERE batchId = :batchId AND storeName = :storeName ORDER BY orderNumber")
    LiveData<List<Order>> getAllOrders(int batchId, String storeName);

    @Transaction
    @Query("SELECT * FROM shop_accounts")
    LiveData<List<StoreWithOrders>> getAllStoreWithOrders();
}
