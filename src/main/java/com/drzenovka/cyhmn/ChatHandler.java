package com.drzenovka.cyhmn;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.ServerChatEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ChatHandler {

    private final Random rand = new Random();

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        event.setCanceled(true);

        String raw = event.message;
        EntityPlayerMP sender = (EntityPlayerMP) event.player;
        if (sender == null) return;

        double clearRange = CanYouHearMeNow.chatSettings.clearRange;
        double hearRange = CanYouHearMeNow.chatSettings.hearRange;
        double whisperMultiplier = CanYouHearMeNow.chatSettings.whisperMultiplier;
        double shoutMultiplier = CanYouHearMeNow.chatSettings.shoutMultiplier;

        String prefixTag = "";

        if (raw.startsWith("/")) {
            event.setCanceled(false);
            return;
        } else if (raw.startsWith("##")) {
            raw = raw.substring(2)
                .trim();
            clearRange *= whisperMultiplier;
            hearRange *= whisperMultiplier;
            prefixTag = "[whisper] ";
        } else if (raw.startsWith("!!")) {
            raw = raw.substring(2)
                .trim();
            clearRange *= shoutMultiplier;
            hearRange *= shoutMultiplier;
            prefixTag = "[shout] ";
        }

        if (hearRange < clearRange) hearRange = clearRange;

        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) return;

        List<EntityPlayerMP> players = server.getConfigurationManager().playerEntityList;
        double clearRangeSq = clearRange * clearRange;
        double hearRangeSq = hearRange * hearRange;
        double rangeSpan = Math.max(1.0, hearRange - clearRange);

        for (Object o : players) {
            if (!(o instanceof EntityPlayerMP recipient)) continue;

            double dx = sender.posX - recipient.posX;
            double dy = sender.posY - recipient.posY;
            double dz = sender.posZ - recipient.posZ;
            double distSq = dx * dx + dy * dy + dz * dz;

            if (distSq > hearRangeSq) continue;

            String delivered;
            if (distSq <= clearRangeSq) {
                delivered = prefixTag + raw;
            } else {
                double dist = Math.sqrt(distSq);
                double aud = 1.0 - ((dist - clearRange) / rangeSpan);
                aud = Math.max(0.0, Math.min(1.0, aud));
                delivered = prefixTag + garbleByAudibility(raw, aud);
            }

            recipient.addChatMessage(
                new ChatComponentText(String.format("<%s> %s", sender.getCommandSenderName(), delivered)));
        }

        server.logWarning(String.format("[CanYouHearMeNow] %s: %s", sender.getCommandSenderName(), raw));
    }

    private String garbleByAudibility(String text, double audibility) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                sb.append(c);
            } else {
                sb.append(rand.nextDouble() <= audibility ? c : randomGarbler());
            }
        }
        return sb.toString();
    }

    private char randomGarbler() {
        return switch (rand.nextInt(4)) {
            case 0 -> '*';
            case 1 -> '?';
            case 2 -> (char) ('a' + rand.nextInt(26));
            default -> '#';
        };
    }
}
