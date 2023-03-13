/**
 * Miner Overview © 2023 by Thomas (DJ1TJOO) is licensed under CC BY-NC 4.0. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc/4.0/
 */

package nl.thomasbrants.mineroverview.light;

import java.util.HashMap;
import java.util.Map;

public class LightLevelStorage {
    public static final Map<Long, LightLevelStorageEntry> LIGHT_LEVELS = new HashMap<>();
    public static boolean PLAYER_MOVED = false;

}
