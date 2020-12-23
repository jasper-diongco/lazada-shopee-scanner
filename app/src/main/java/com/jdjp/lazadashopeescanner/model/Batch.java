package com.jdjp.lazadashopeescanner.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "batches")
public class Batch {
    @PrimaryKey(autoGenerate = true)
    private int batchId;
    @NonNull
    private long createdAt;

    public int getBatchId() {
        return batchId;
    }

    public void setBatchId(int batchId) {
        this.batchId = batchId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Batch{" +
                "batchId=" + batchId +
                ", createdAt=" + createdAt +
                '}';
    }
}
