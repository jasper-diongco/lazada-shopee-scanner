package com.jdjp.lazadashopeescanner;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.jdjp.lazadashopeescanner.dao.BatchDao;
import com.jdjp.lazadashopeescanner.dao.OrderDao;
import com.jdjp.lazadashopeescanner.dao.ShopAccountDao;
import com.jdjp.lazadashopeescanner.model.Batch;
import com.jdjp.lazadashopeescanner.model.Order;
import com.jdjp.lazadashopeescanner.model.ShopAccount;

@Database(entities =
        {ShopAccount.class, Batch.class, Order.class}
        , version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "app_database.db")
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return instance;
    }

    public abstract ShopAccountDao shopAccountDao();
    public abstract BatchDao batchDao();
    public abstract OrderDao orderDao();
}
