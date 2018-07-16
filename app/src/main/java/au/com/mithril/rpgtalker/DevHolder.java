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

    static long seconds(Date d) {
        long diff=(new Date()).getTime()-d.getTime();
        return diff/1000L;
    }
    @Override
    public String toString() {
        String result;
        if (name != null && !name.isEmpty()) result = name;
        else if (file != null) result = file.getName();
        else result = "(null)";
        if (lastSeen!=null) {
            result+=" ("+seconds(lastSeen)+")";
        }
        if (isConnected) result+="*";
        return result;
    }

    @Override
    public int compareTo(@NonNull DevHolder devHolder) {
        return toString().compareTo(devHolder.toString());
    }
}

