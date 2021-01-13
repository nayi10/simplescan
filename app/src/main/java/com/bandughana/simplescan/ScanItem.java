package com.bandughana.simplescan;

import java.text.DateFormat;
import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class ScanItem {

    @Id
    public long id;
    private String scannedText;
    private long dateScanned = new Date().getTime();

    public ScanItem(){}

    public ScanItem(String scannedText) {
        this.scannedText = scannedText;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

     String getScannedText() {
        return scannedText;
    }

    public void setScannedText(String scannedText) {
        this.scannedText = scannedText;
    }

    long getDateScanned() {
        return dateScanned;
    }

    public void setDateScanned(long dateScanned) {
        this.dateScanned = dateScanned;
    }

    String getFormattedDate() {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                DateFormat.SHORT).format(dateScanned);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScanItem item = (ScanItem) o;
        return dateScanned == item.dateScanned &&
                scannedText.equals(item.scannedText);
    }
}
