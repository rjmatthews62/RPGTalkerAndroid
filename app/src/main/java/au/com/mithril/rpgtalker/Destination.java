package au.com.mithril.rpgtalker;

import android.support.annotation.NonNull;

class Destination implements Comparable<Destination>{
    String name;
    DevHolder device;
    public boolean speaker;

    Destination(String name, DevHolder device) {
        this.name=name;
        this.device=device;
    }

    @Override
    public String toString() {
        String result = name;
        if (name==null || name.isEmpty()) {
            if (device!=null) result=device.name;
        }
        if (result==null || result.isEmpty()) result="Nowhere";
        if (device!=null) result+="->"+device.toString();
        return result;
    }


    @Override
    public int compareTo(@NonNull Destination dest) {
        return toString().compareTo(dest.toString());
    }

    public String getKey() {
        return "DEST."+name.toLowerCase().replace(" ","_");
    }

    public String getAddress() {
        if (device==null || device.file==null) return null;
        return device.file.getAddress();
    }
}
