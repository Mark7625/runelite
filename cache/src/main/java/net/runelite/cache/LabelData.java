package net.runelite.cache;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LabelData {
    String name;
    int[] coords;
    String size;
}
