package au.com.mithril.rpgtalker;

import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;

public class DocHolder implements Comparable<DocHolder> {
    public String name;
    public DocumentFile file;
    public String character;

    public DocHolder(String name, DocumentFile file) {
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
    public int compareTo(@NonNull DocHolder docHolder) {
        return toString().compareTo(docHolder.toString());
    }

    public boolean isGlobal() {
        return (character==null || character.isEmpty());
    }
}

