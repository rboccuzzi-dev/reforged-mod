package com.cuzzi.reforged.server;

/**
 * Implement this on the entity class of your own mobs to give them a level range from code:
 *
 *   public class MyMob extends Monster implements LevelledMob {
 *       public int minLevel() { return 20; }
 *       public int maxLevel() { return 25; }
 *   }
 *
 * A datapack entry (see MobLevelEntry) still overrides this, so modpack authors can rebalance it.
 */
public interface LevelledMob {
    int minLevel();

    int maxLevel();
}