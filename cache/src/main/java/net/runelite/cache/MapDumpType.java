package net.runelite.cache;

import lombok.Data;
import lombok.Getter;


public enum MapDumpType {
    NORMAL("normal"),
    OBJECTS("objects"),
    HEIGHT("height");

    public String getFormattedString() {
        return formattedString;
    }

    private final String formattedString;

    MapDumpType(String formattedString) {
        this.formattedString = formattedString;
    }


}
