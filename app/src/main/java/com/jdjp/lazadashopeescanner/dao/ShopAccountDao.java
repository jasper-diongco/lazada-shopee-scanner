package com.jdjp.lazadashopeescanner.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.jdjp.lazadashopeescanner.model.ShopAccount;
import com.jdjp.lazadashopeescanner.model.pojo.StoreWithOrders;

import java.util.List;

@Dao
public interface ShopAccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ShopAccount shopAccount);

    @Delete
    void delete(ShopAccount shopAccount);

    @Query("SELECT * FROM shop_accounts")
    LiveData<List<ShopAccount>> getAllShopAccounts();



}
