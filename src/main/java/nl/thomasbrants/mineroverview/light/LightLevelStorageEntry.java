package nl.thomasbrants.mineroverview.light;

public class LightLevelStorageEntry {
    public final int value;
    public final long sourcePos;

    public LightLevelStorageEntry(int value, long sourcePos) {
        this.value = value;
        this.sourcePos = sourcePos;
    }
}
