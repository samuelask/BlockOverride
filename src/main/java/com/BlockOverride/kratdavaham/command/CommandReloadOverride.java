package com.BlockOverride.kratdavaham.command;

import com.BlockOverride.kratdavaham.BlockOverride;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandReloadOverride extends CommandBase {
    @Override
    public String getName() {
        return "blockoverride_reload";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/blockoverride_reload - Reloads the block override config.";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        BlockOverride.instance.reloadConfig();
        notifyCommandListener(sender, this, "BlockOverride config reloaded!");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // OP-only
    }
}