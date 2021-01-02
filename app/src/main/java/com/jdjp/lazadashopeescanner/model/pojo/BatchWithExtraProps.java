package com.jdjp.lazadashopeescanner.model.pojo;

import androidx.room.Embedded;

import com.jdjp.lazadashopeescanner.model.Batch;

public class BatchWithExtraProps {
    @Embedded
    private Batch batch;

    private int scanCount;
    private int readyToShipCount;
    private int canceledCount;

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public int getScanCount() {
        return scanCount;
    }

    public void setScanCount(int scanCount) {
        this.scanCount = scanCount;
    }

    public int getReadyToShipCount() {
        return readyToShipCount;
    }

    public void setReadyToShipCount(int readyToShipCount) {
        this.readyToShipCount = readyToShipCount;
    }

    public int getCanceledCount() {
        return canceledCount;
    }

    public void setCanceledCount(int canceledCount) {
        this.canceledCount = canceledCount;
    }


    @Override
    public String toString() {
        return "BatchWithExtraProps{" +
                "batch=" + batch +
                ", scanCount=" + scanCount +
                ", readyToShipCount=" + readyToShipCount +
                '}';
    }
}
