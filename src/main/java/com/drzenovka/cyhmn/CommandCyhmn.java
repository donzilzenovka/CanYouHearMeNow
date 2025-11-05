package com.drzenovka.cyhmn;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class CommandCyhmn extends CommandBase {

    @Override
    public String getCommandName() {
        return "cyhmn";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/cyhmn reload - Reload the chat range settings from the config file";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            CanYouHearMeNow.reloadChatSettings();
            sender.addChatMessage(new ChatComponentText("CanYouHearMeNow: Chat settings reloaded."));
        } else {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
