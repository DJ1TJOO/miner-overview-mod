/**
 * Miner Overview © 2023 by Thomas (DJ1TJOO) is licensed under CC BY-NC 4.0. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc/4.0/
 */

package nl.thomasbrants.mineroverview.light;

public class QueueEntry {
    public final long pos;
    public final LightLevelStorageEntry value;

    public QueueEntry(long pos, LightLevelStorageEntry value) {
        this.pos = pos;
        this.value = value;
    }
}
