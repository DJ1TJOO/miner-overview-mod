package nl.thomasbrants.mineroverview.light;

public class QueueEntry {
    public final long pos;
    public final LightLevelStorageEntry value;

    public QueueEntry(long pos, LightLevelStorageEntry value) {
        this.pos = pos;
        this.value = value;
    }
}
