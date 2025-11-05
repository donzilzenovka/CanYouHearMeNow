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
        // Cancel the default broadcast
        event.setCanceled(true);

        String raw = event.message;
        EntityPlayerMP sender = (EntityPlayerMP) event.player;
        if (sender == null) return;

        // Determine mode by prefix
        double clearRange = CanYouHearMeNow.CLEAR_RANGE;
        double hearRange = CanYouHearMeNow.HEAR_RANGE;
        String prefixTag = ""; // will be prepended to the message visible to recipients

        if (raw.startsWith("/")) {
            event.setCanceled(false);
            return;
        } else if (raw.startsWith("##")) { // whisper
            raw = raw.substring(2)
                .trim();
            clearRange *= CanYouHearMeNow.WHISPER_MULTIPLIER;
            hearRange *= CanYouHearMeNow.WHISPER_MULTIPLIER;
            prefixTag = "[whisper] ";
        } else if (raw.startsWith("!!")) { // shout
            raw = raw.substring(2)
                .trim();
            clearRange *= CanYouHearMeNow.SHOUT_MULTIPLIER;
            hearRange *= CanYouHearMeNow.SHOUT_MULTIPLIER;
            prefixTag = "[shout] ";
        } else {
            // normal chat uses defaults
        }

        // clamp
        if (hearRange < clearRange) hearRange = clearRange;

        // Get all players on the server
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) return;

        List<EntityPlayerMP> players = server.getConfigurationManager().playerEntityList;

        double clearRangeSq = clearRange * clearRange;
        double hearRangeSq = hearRange * hearRange;
        double rangeSpan = Math.max(1.0, hearRange - clearRange);

        for (Object o : players) {
            if (!(o instanceof EntityPlayerMP recipient)) continue;

            // distance squared (fast)
            double dx = sender.posX - recipient.posX;
            double dy = sender.posY - recipient.posY;
            double dz = sender.posZ - recipient.posZ;
            double distSq = dx * dx + dy * dy + dz * dz;

            if (distSq > hearRangeSq) {
                // out of range: do not deliver
                continue;
            }

            String delivered;
            if (distSq <= clearRangeSq) {
                // clear message
                delivered = prefixTag + raw;
            } else {
                // falloff: compute audibility fraction in [0,1]
                double dist = Math.sqrt(distSq);
                double aud = 1.0 - ((dist - clearRange) / rangeSpan); // linear
                aud = Math.max(0.0, Math.min(1.0, aud));

                // garble text according to audibility
                delivered = prefixTag + garbleByAudibility(raw, aud);
            }

            // Add player's name as sender (optional style)
            String senderName = sender.getCommandSenderName();
            String msg = String.format("<%s> %s", senderName, delivered);
            recipient.addChatMessage(new ChatComponentText(msg));
        }

        // Log to server console for admins
        server.logWarning(String.format("[CanYouHearMeNow] %s: %s", sender.getCommandSenderName(), raw));
    }

    private String garbleByAudibility(String text, double audibility) {
        // audibility 0.0 => almost all garbled; 1.0 => none garbled.
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                sb.append(c);
            } else {
                double keep = rand.nextDouble();
                if (keep <= audibility) {
                    sb.append(c);
                } else {
                    // replace with a placeholder or random letter
                    // use slightly varied glyphs to feel organic
                    char replacement = randomGarbler();
                    sb.append(replacement);
                }
            }
        }
        return sb.toString();
    }

    private char randomGarbler() {
        int r = rand.nextInt(4);
        return switch (r) {
            case 0 -> '*';
            case 1 -> '?';
            case 2 -> (char) ('a' + rand.nextInt(26));
            default -> '#';
        };
    }
}
