package com.BlockOverride.kratdavaham.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.BlockOverride.kratdavaham.BlockOverride;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandBlockOverride extends CommandBase {
	@Override
    public String getName() {
        return "blockoverride";
    }

	@Override
	public String getUsage(ICommandSender sender) {
	    return "/blockoverride help";
	}
    
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
	    if (args.length == 1) {
	        return getListOfStringsMatchingLastWord(args, Arrays.asList("reload", "help", "add", "remove"));
	    }

	    if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
	        return getListOfStringsMatchingLastWord(args, Arrays.asList("hardness", "resistance"));
	    }

	    return Collections.emptyList();
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
	    if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
	        sender.sendMessage(new TextComponentString("§6Available BlockOverride commands:"));
	        sender.sendMessage(new TextComponentString("§e/blockoverride reload §7- Reloads the block override config."));
	        sender.sendMessage(new TextComponentString("§e/blockoverride add §7- Adds the block in your hand to the config."));
	        sender.sendMessage(new TextComponentString("§e/blockoverride remove §7- Removes the block in your hand from the config."));
	        sender.sendMessage(new TextComponentString("§e/blockoverride help §7- Shows this help message."));
	    } else if (args[0].equalsIgnoreCase("reload")) {
	        BlockOverride.instance.reloadConfig();
	        notifyCommandListener(sender, this, "§aBlockOverride config reloaded!");
	    } else if (args[0].equalsIgnoreCase("add")) {
	        if (!(sender instanceof EntityPlayer)) {
	            throw new CommandException("This command must be run by a player.");
	        }

	        EntityPlayer player = (EntityPlayer) sender;
	        ItemStack held = player.getHeldItemMainhand();
	        if (held == null || !(held.getItem() instanceof ItemBlock)) {
	            String heldName = (held != null) ? held.getDisplayName() : "nothing";
	            throw new CommandException("Item '" + heldName + "' is not a placeable block.");
	        }

	        if (args.length < 3) {
	            throw new WrongUsageException("/blockoverride add <hardness|resistance> <value>");
	        }

	        String type = args[1].toLowerCase();
	        float value;

	        try {
	            value = Float.parseFloat(args[2]);
	        } catch (NumberFormatException e) {
	            throw new CommandException("Value must be a number.");
	        }

	        Block block = ((ItemBlock) held.getItem()).getBlock();
	        ResourceLocation id = Block.REGISTRY.getNameForObject(block);
	        if (id == null) throw new CommandException("Could not determine block ID.");
	        String blockKey = id.toString();

	        float[] existing = BlockOverride.instance.blockOverrides.getOrDefault(blockKey, new float[]{1.0f, 10.0f});
	        float hardness = existing[0];
	        float resistance = existing[1];

	        if (type.equals("hardness")) {
	            hardness = value;
	        } else if (type.equals("resistance")) {
	            resistance = value;
	        } else {
	            throw new WrongUsageException("/blockoverride add <hardness|resistance> <value>");
	        }

	        // Update and apply
	        BlockOverride.instance.addOrUpdateBlockInConfig(blockKey, hardness, resistance);
	        BlockOverride.instance.modifyBlock(blockKey, hardness, resistance);
	        notifyCommandListener(sender, this, "§aUpdated block '" + blockKey + "' → hardness=" + hardness + ", resistance=" + resistance);
	    } else if (args[0].equalsIgnoreCase("remove")) {
	        if (args.length < 2) {
	            throw new WrongUsageException("/blockoverride remove <modid:blockid>");
	        }

	        String blockId = args[1];
	        Block block = Block.REGISTRY.getObject(new ResourceLocation(blockId));

	        if (block == null) {
	            throw new CommandException("Block '" + blockId + "' not found.");
	        }

	        // Remove from map
	        BlockOverride.instance.blockOverrides.remove(blockId);

	        // Reset to original values if stored
	        float[] defaults = BlockOverride.instance.originalValues.get(block);
	        if (defaults != null) {
	            block.setHardness(defaults[0]);
	            block.setResistance(defaults[1]);
	        }

	        // Remove from config
	        BlockOverride.instance.removeBlockFromConfig(blockId);

	        notifyCommandListener(sender, this, "§aRemoved override for block: " + blockId);
	    } else {
	        throw new WrongUsageException("/blockoverride help");
	    }
	}

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // OP-only
    }
}