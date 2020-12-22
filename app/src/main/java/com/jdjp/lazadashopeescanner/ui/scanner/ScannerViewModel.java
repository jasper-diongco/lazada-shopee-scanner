package com.jdjp.lazadashopeescanner.ui.scanner;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.jdjp.lazadashopeescanner.model.ShopAccount;
import com.jdjp.lazadashopeescanner.repository.ShopAccountRepository;

import java.util.List;

public class ScannerViewModel extends AndroidViewModel {
    private ShopAccountRepository repository;

    public ScannerViewModel(@NonNull Application application) {
        super(application);

        repository = new ShopAccountRepository(application);
    }

    public LiveData<List<ShopAccount>> getAllShopAccounts() {
        return repository.getAllShopAccounts();
    }

}
