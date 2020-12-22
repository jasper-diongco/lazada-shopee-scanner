package com.jdjp.lazadashopeescanner.ui.shop_accounts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.jdjp.lazadashopeescanner.model.ShopAccount;
import com.jdjp.lazadashopeescanner.repository.ShopAccountRepository;

import java.util.List;

public class ShopAccountListViewModel extends AndroidViewModel {


    private ShopAccountRepository repository;

    public ShopAccountListViewModel(@NonNull Application application) {
        super(application);

        repository = new ShopAccountRepository(application);
    }

    public void insertShopAccount(ShopAccount shopAccount) {
        repository.insert(shopAccount);
    }

    public void deleteShopAccount(ShopAccount shopAccount) {
        repository.delete(shopAccount);
    }

    public LiveData<List<ShopAccount>> getAllShopAccounts() {
        return repository.getAllShopAccounts();
    }
}
