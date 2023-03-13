/**
 * Miner Overview © 2023 by Thomas (DJ1TJOO) is licensed under CC BY-NC 4.0. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc/4.0/
 */

package nl.thomasbrants.mineroverview.light;

public class LightLevelStorageEntry {
    public final int value;
    public final long sourcePos;

    public LightLevelStorageEntry(int value, long sourcePos) {
        this.value = value;
        this.sourcePos = sourcePos;
    }
}
