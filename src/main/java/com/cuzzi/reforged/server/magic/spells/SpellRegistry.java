package com.cuzzi.reforged.server.magic.spells;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SpellRegistry {
    private static final Map<String, Spell> SPELLS = new HashMap<>();

    public static void register(Spell spell) {
        SPELLS.put(spell.getId(), spell);
    }

    public static Spell get(String id) {
        return SPELLS.get(id);
    }

    public static Collection<Spell> getAll() {
        return SPELLS.values();
    }
}