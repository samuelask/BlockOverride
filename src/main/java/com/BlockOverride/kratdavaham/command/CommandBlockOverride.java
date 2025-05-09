package com.BlockOverride.kratdavaham.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.BlockOverride.kratdavaham.BlockOverride;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.common.ModMetadata;

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
	        return getListOfStringsMatchingLastWord(args, Arrays.asList("reload", "help", "add", "remove", "get", "list", "info"));
	    }

	    if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
	        return getListOfStringsMatchingLastWord(args, Arrays.asList("hardness", "resistance"));
	    }

	    return Collections.emptyList();
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
	    if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
	        sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "Available BlockOverride commands:"));
			sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/blockoverride reload " + TextFormatting.GRAY + "- Reloads the block override config."));
			sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/blockoverride add " + TextFormatting.GRAY + "- Adds the block in your hand to the config."));
			sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/blockoverride remove " + TextFormatting.GRAY + "- Removes the block in your hand from the config."));
			sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/blockoverride get " + TextFormatting.GRAY + "- Gets the Hardness and Resistance value from the block in your hand."));
			sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/blockoverride list " + TextFormatting.GRAY + "- Lists all blocks that are currently overridden."));
			sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/blockoverride info " + TextFormatting.GRAY + "- Lists mod version."));
			sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "/blockoverride help " + TextFormatting.GRAY + "- Shows this help message."));
	    } else if (args[0].equalsIgnoreCase("reload")) {
	        BlockOverride.instance.reloadConfig();
	        notifyCommandListener(sender, this, TextFormatting.GREEN + "BlockOverride config reloaded!");
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

	        float[] existing = BlockOverride.instance.getOverride(blockKey);
	        float hardness = (existing != null) ? existing[0] : 1.0f;
	        float resistance = (existing != null) ? existing[1] : 10.0f;

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
	        notifyCommandListener(sender, this, TextFormatting.GREEN + "Updated block '" + blockKey + "' -> hardness=" + hardness + ", resistance=" + resistance);
	    } else if (args[0].equalsIgnoreCase("remove")) {
	        if (!(sender instanceof EntityPlayer)) {
	            throw new CommandException("This command must be run by a player.");
	        }

	        EntityPlayer player = (EntityPlayer) sender;
	        ItemStack held = player.getHeldItemMainhand();
	        if (held == null || !(held.getItem() instanceof ItemBlock)) {
	            String heldName = (held != null) ? held.getDisplayName() : "nothing";
	            throw new CommandException("Item '" + heldName + "' is not a placeable block.");
	        }

	        Block block = ((ItemBlock) held.getItem()).getBlock();
	        ResourceLocation id = Block.REGISTRY.getNameForObject(block);
	        if (id == null) {
	            throw new CommandException("Could not determine block ID.");
	        }

	        String blockId = id.toString();
	        
	        if (!BlockOverride.instance.isOverridden(blockId)) {
	            throw new CommandException("Block '" + blockId + "' is not currently overridden.");
	        }

	        // Remove from override map
	        BlockOverride.instance.removeOverride(blockId);

	        // Reset to original values if stored
	        float[] defaults = BlockOverride.instance.getOriginalValues(block);
	        if (defaults != null) {
	            block.setHardness(defaults[0]);
	            block.setResistance(defaults[1]);
	        }

	        // Remove from config
	        BlockOverride.instance.removeBlockFromConfig(blockId);

	        notifyCommandListener(sender, this, TextFormatting.GREEN + "Removed override for block: " + blockId);
	    } else if (args[0].equalsIgnoreCase("get")) {
	        if (!(sender instanceof EntityPlayer)) {
	            throw new CommandException("This command must be run by a player.");
	        }

	        EntityPlayer player = (EntityPlayer) sender;
	        ItemStack held = player.getHeldItemMainhand();
	        if (held == null || !(held.getItem() instanceof ItemBlock)) {
	            String heldName = (held != null) ? held.getDisplayName() : "nothing";
	            throw new CommandException("Item '" + heldName + "' is not a placeable block.");
	        }

	        Block block = ((ItemBlock) held.getItem()).getBlock();
	        IBlockState state = block.getDefaultState();
	        float hardness = block.getBlockHardness(state, null, BlockPos.ORIGIN);
	        float resistance = block.getExplosionResistance(null);
	        ResourceLocation id = Block.REGISTRY.getNameForObject(block);

	        sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Block: " + TextFormatting.WHITE + id));
	        sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Hardness: " + TextFormatting.WHITE + hardness));
	        sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Resistance: " + TextFormatting.WHITE + resistance));
	        
	        if (BlockOverride.instance.isOverridden(id.toString())) {
	            sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "This block is currently overridden."));
	        }
	    } else if (args[0].equalsIgnoreCase("list")) {
	        Map<String, float[]> overrides = BlockOverride.instance.getAllBlockOverrides();
	        if (overrides.isEmpty()) {
	            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "No blocks are currently overridden."));
	        } else {
	            sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "Overridden blocks (" + overrides.size() + "):"));
	            for (Map.Entry<String, float[]> entry : overrides.entrySet()) {
	                String blockId = entry.getKey();
	                float[] values = entry.getValue();
	                sender.sendMessage(new TextComponentString(
	                    TextFormatting.GRAY + "- " + TextFormatting.YELLOW + blockId + TextFormatting.GRAY + " -> hardness=" + values[0] + ", resistance=" + values[1]
	                ));
	            }
	        }
	    } else if (args[0].equalsIgnoreCase("info")) {
	        ModMetadata meta = BlockOverride.metadata;

	        sender.sendMessage(new TextComponentString(TextFormatting.GOLD + meta.name + TextFormatting.GRAY + " v" + meta.version));
	        if (meta.description != null && !meta.description.trim().isEmpty()) {
	            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Description: " + TextFormatting.WHITE + meta.description));
	        }
	        if (meta.authorList != null && !meta.authorList.isEmpty()) {
	            String authors = String.join(", ", meta.authorList);
	            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Authors: " + TextFormatting.WHITE + authors));
	        }
	        if (meta.url != null && !meta.url.trim().isEmpty()) {
	        	TextComponentString website = new TextComponentString(TextFormatting.YELLOW + "Website: ");
	        	TextComponentString link = new TextComponentString(TextFormatting.BLUE + meta.url);
	        	link.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, meta.url));
	        	link.getStyle().setUnderlined(true);
	        	website.appendSibling(link);

	        	sender.sendMessage(website);
	        }
	    } else {
	        throw new WrongUsageException("/blockoverride help");
	    }
	}

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // OP-only
    }
}