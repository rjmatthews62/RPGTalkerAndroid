package au.com.mithril.rpgtalker;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import java.util.Date;

public class DevHolder implements Comparable<DevHolder> {
    public String name;
    public BluetoothDevice file;
    public Date lastSeen;
    public boolean isConnected;

    public DevHolder(String name, BluetoothDevice file) {
        this.name = name;
        this.file = file;
    }

    @Override
    public String toString() {
        if (name != null && !name.isEmpty()) return name;
        if (file != null) return file.getName();
        return "(null)";
    }

    @Override
    public int compareTo(@NonNull DevHolder devHolder) {
        return toString().compareTo(devHolder.toString());
    }
}

