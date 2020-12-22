package com.jdjp.lazadashopeescanner.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "shop_accounts")
public class ShopAccount {
    @NonNull
    @PrimaryKey(autoGenerate = false)
    private String account;

    @NonNull
    private String accessToken;
    @NonNull
    private String refreshToken;
    @NonNull
    private long expiresIn;
    @NonNull
    private long refreshExpiresIn;
    @NonNull
    private String shortCode;
    @NonNull
    private String sellerId;
    @NonNull
    private String userId;
    @NonNull
    private long createdAt;

    public Date getExpiryDate() {
        Date dateCreatedAt = new Date(createdAt);
        return new Date(dateCreatedAt.getTime() + expiresIn * 1000);
    }

    @Override
    public String toString() {
        return shortCode + " - " + account;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public long getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public void setRefreshExpiresIn(long refreshExpiresIn) {
        this.refreshExpiresIn = refreshExpiresIn;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
