package com.drzenovka.cyhmn;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class ChatRangeSettings {

    private final File configFile;
    private Configuration cfg;

    public double clearRange = 20.0;
    public double hearRange = 32.0;
    public double whisperMultiplier = 0.2;
    public double shoutMultiplier = 2.0;

    public ChatRangeSettings(File configFile) {
        this.configFile = configFile;
        load();
    }

    public File getConfigFile() {
        return configFile;
    }

    public void load() {
        cfg = new Configuration(configFile);
        try {
            cfg.load();
            clearRange = cfg.get("ranges", "clearRange", clearRange, "Distance where chat is fully clear")
                .getDouble(clearRange);
            hearRange = cfg.get("ranges", "hearRange", hearRange, "Maximum distance for partially heard chat")
                .getDouble(hearRange);
            whisperMultiplier = cfg
                .get("ranges", "whisperMultiplier", whisperMultiplier, "Range multiplier for whispers")
                .getDouble(whisperMultiplier);
            shoutMultiplier = cfg.get("ranges", "shoutMultiplier", shoutMultiplier, "Range multiplier for shouts")
                .getDouble(shoutMultiplier);
        } finally {
            if (cfg.hasChanged()) cfg.save();
        }
    }
}
