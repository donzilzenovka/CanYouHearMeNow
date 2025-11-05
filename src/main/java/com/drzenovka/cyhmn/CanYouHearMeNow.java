package com.drzenovka.cyhmn;

import static com.drzenovka.cyhmn.CanYouHearMeNow.*;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = MODID, version = VERSION, name = NAME)
public class CanYouHearMeNow {

    public static final String MODID = "cyhmn";
    public static final String VERSION = "1.0.0";
    public static final String NAME = "CanYouHearMeNow";

    public static double CLEAR_RANGE = 8.0; // within this range, chat is clear
    public static double HEAR_RANGE = 32.0; // beyond this, you hear nothing
    public static double WHISPER_MULTIPLIER = 0.5;
    public static double SHOUT_MULTIPLIER = 2.0;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File cfgFile = event.getSuggestedConfigurationFile();
        Configuration cfg = new Configuration(cfgFile);
        try {
            cfg.load();
            CLEAR_RANGE = cfg.get("ranges", "clearRange", CLEAR_RANGE, "Distance where chat is fully clear")
                .getDouble(CLEAR_RANGE);
            HEAR_RANGE = cfg.get("ranges", "hearRange", HEAR_RANGE, "Maximum distance for partially heard chat")
                .getDouble(HEAR_RANGE);
            WHISPER_MULTIPLIER = cfg
                .get("ranges", "whisperMultiplier", WHISPER_MULTIPLIER, "Range multiplier for whispers")
                .getDouble(WHISPER_MULTIPLIER);
            SHOUT_MULTIPLIER = cfg.get("ranges", "shoutMultiplier", SHOUT_MULTIPLIER, "Range multiplier for shouts")
                .getDouble(SHOUT_MULTIPLIER);

        } finally {
            if (cfg.hasChanged()) cfg.save();
        }

        // Register event handler
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new ChatHandler());
    }
}
