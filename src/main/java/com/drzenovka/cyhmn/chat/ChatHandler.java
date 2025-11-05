package com.drzenovka.cyhmn.chat;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.ServerChatEvent;

import com.drzenovka.cyhmn.config.ModConfig;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ChatHandler {

    private final Random rand = new Random();

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        event.setCanceled(true);

        String raw = event.message;
        EntityPlayerMP sender = (EntityPlayerMP) event.player;
        if (sender == null) return;

        double clearRange = ModConfig.chatRange;
        double hearRange = ModConfig.hearRange;
        double whisperMultiplier = ModConfig.whisperMultiplier;
        double shoutMultiplier = ModConfig.shoutMultiplier;
        boolean showRelativeDirection = ModConfig.directionalChat;

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
                delivered = raw;
            } else {
                double dist = Math.sqrt(distSq);
                double aud = 1.0 - ((dist - clearRange) / rangeSpan);
                aud = Math.max(0.0, Math.min(1.0, aud));
                delivered = garbleByAudibility(raw, aud);
            }

            // Add direction prefix only for listeners, not the speaker
            String directionPrefix = "";
            double relativeAngle = 0;
            if (showRelativeDirection && recipient != sender) {
                double angleToSpeaker = Math.toDegrees(Math.atan2(-dx, dz));
                angleToSpeaker = (angleToSpeaker + 360) % 360;

                double listenerYaw = (recipient.rotationYaw % 360 + 360) % 360;
                relativeAngle = (angleToSpeaker - listenerYaw + 360) % 360;

                directionPrefix = "[" + getRelativeDirection(relativeAngle) + "] ";
            }

            boolean obstructed = ModConfig.muffledChat && isLineObstructed(sender, recipient, 40);
            if (obstructed && recipient != sender) {
                delivered = net.minecraft.util.EnumChatFormatting.GRAY + ""
                    + net.minecraft.util.EnumChatFormatting.ITALIC
                    + delivered;
                directionPrefix = "[muffled from " + getRelativeDirection(relativeAngle) + "] ";
            }

            String senderName = sender.getCommandSenderName();
            recipient.addChatMessage(
                new ChatComponentText(String.format("<%s> %s%s%s", senderName, directionPrefix, prefixTag, delivered)));
        }

        server.logWarning(String.format("[CanYouHearMeNow] %s: %s", sender.getCommandSenderName(), raw));
    }

    private String getRelativeDirection(double relativeAngle) {
        // 0° is straight ahead of the listener, clockwise rotation
        if (relativeAngle >= 337.5 || relativeAngle < 22.5) return "N";
        if (relativeAngle >= 22.5 && relativeAngle < 67.5) return "NE";
        if (relativeAngle >= 67.5 && relativeAngle < 112.5) return "E";
        if (relativeAngle >= 112.5 && relativeAngle < 157.5) return "SE";
        if (relativeAngle >= 157.5 && relativeAngle < 202.5) return "S";
        if (relativeAngle >= 202.5 && relativeAngle < 247.5) return "SW";
        if (relativeAngle >= 247.5 && relativeAngle < 292.5) return "W";
        return "NW";
    }

    private String garbleByAudibility(String text, double audibility) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) sb.append(c);
            else sb.append(rand.nextDouble() <= audibility ? c : randomGarbler());
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

    private boolean isLineObstructed(EntityPlayer speaker, EntityPlayer listener, int maxSteps) {
        Vec3 start = Vec3.createVectorHelper(speaker.posX, speaker.posY + speaker.getEyeHeight(), speaker.posZ);
        Vec3 end = Vec3.createVectorHelper(listener.posX, listener.posY + listener.getEyeHeight(), listener.posZ);
        Vec3 direction = end.subtract(start)
            .normalize();

        double stepSize = 0.5; // Half a block per step
        double distance = speaker.getDistanceToEntity(listener);
        int steps = Math.min((int) (distance / stepSize), maxSteps);

        for (int i = 0; i < steps; i++) {
            start = start
                .addVector(direction.xCoord * stepSize, direction.yCoord * stepSize, direction.zCoord * stepSize);
            Block block = speaker.worldObj.getBlock(
                (int) Math.floor(start.xCoord),
                (int) Math.floor(start.yCoord),
                (int) Math.floor(start.zCoord));
            if (block.isOpaqueCube()) {
                return true;
            }
        }
        return false;
    }

}
