package com.example.churnpoc.dto;

import java.util.List;

public class UploadReceipt {

    private int rowsReceived;
    private int rowsLoaded;
    private int rowsSkipped;
    private List<String> sampleErrors;

    public UploadReceipt(int rowsReceived, int rowsLoaded, int rowsSkipped,
                         List<String> sampleErrors) {
        this.rowsReceived = rowsReceived;
        this.rowsLoaded = rowsLoaded;
        this.rowsSkipped = rowsSkipped;
        this.sampleErrors = sampleErrors;
    }

    public int getRowsReceived() {
        return rowsReceived;
    }

    public int getRowsLoaded() {
        return rowsLoaded;
    }

    public int getRowsSkipped() {
        return rowsSkipped;
    }

    public List<String> getSampleErrors() {
        return sampleErrors;
    }
}
