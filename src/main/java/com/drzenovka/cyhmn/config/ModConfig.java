package com.drzenovka.cyhmn.config;

import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ModConfig {

    private static Configuration cfg;

    // === Chat distance and range multipliers ===
    public static double chatRange = 20.0;
    public static double hearRange = 32.0;
    public static double whisperMultiplier = 0.2;
    public static double shoutMultiplier = 2.0;

    // === Features ===
    public static boolean directionalChat = true;
    public static boolean muffledChat = true;

    // === Initialization ===
    public static void load(FMLPreInitializationEvent event) {
        cfg = new Configuration(event.getSuggestedConfigurationFile());
        try {
            cfg.load();

            chatRange = cfg.get("ranges", "clearRange", chatRange, "Distance where chat is fully clear")
                .getDouble(chatRange);
            hearRange = cfg.get("ranges", "hearRange", hearRange, "Maximum distance for partially heard chat")
                .getDouble(hearRange);
            whisperMultiplier = cfg
                .get("ranges", "whisperMultiplier", whisperMultiplier, "Range multiplier for whispers")
                .getDouble(whisperMultiplier);
            shoutMultiplier = cfg.get("ranges", "shoutMultiplier", shoutMultiplier, "Range multiplier for shouts")
                .getDouble(shoutMultiplier);

            directionalChat = cfg
                .get("features", "showRelativeDirection", directionalChat, "Show chat direction relative to listener")
                .getBoolean(directionalChat);
            muffledChat = cfg
                .get("features", "muffledChat", muffledChat, "If true, messages blocked by terrain will appear muffled")
                .getBoolean(muffledChat);

        } finally {
            if (cfg.hasChanged()) cfg.save();
        }
    }

    public static void reload() {
        if (cfg != null) {
            cfg.load();
            if (cfg.hasChanged()) cfg.save();
        }
    }
}
