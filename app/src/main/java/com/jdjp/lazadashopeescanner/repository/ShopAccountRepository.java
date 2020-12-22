package com.jdjp.lazadashopeescanner.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.jdjp.lazadashopeescanner.AppDatabase;
import com.jdjp.lazadashopeescanner.dao.ShopAccountDao;
import com.jdjp.lazadashopeescanner.model.ShopAccount;

import java.util.Date;
import java.util.List;

public class ShopAccountRepository {
    private ShopAccountDao shopAccountDao;

    public ShopAccountRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        shopAccountDao = database.shopAccountDao();
    }

    public void insert(ShopAccount shopAccount) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                shopAccount.setCreatedAt(new Date().getTime());
                shopAccountDao.insert(shopAccount);
            }
        }).start();
    }

    public void delete(ShopAccount shopAccount) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                shopAccountDao.delete(shopAccount);
            }
        }).start();
    }

    public LiveData<List<ShopAccount>> getAllShopAccounts() {
        return shopAccountDao.getAllShopAccounts();
    }
}
