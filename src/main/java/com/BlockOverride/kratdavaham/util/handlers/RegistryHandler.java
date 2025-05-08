package com.BlockOverride.kratdavaham.util.handlers;

import com.BlockOverride.kratdavaham.init.ItemInit;
import com.BlockOverride.kratdavaham.util.interfaces.IHasModel;

import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class RegistryHandler 
{
    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(ItemInit.ITEMS.toArray(new Item[0]));
    }
    
    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event)
    {
        for(Item item : ItemInit.ITEMS)
        {
            if(item instanceof IHasModel)
            {
                ((IHasModel)item).registerModels();
            }
        }
    }
    
    public static void preInitRegistries()
    {
        
    }
    
    public static void initRegistries()
    {
        
    }
    
    public static void postInitRegistries()
    {
        
    }
    
    public static void serverRegistries(FMLServerStartingEvent event)
    {
        
    }
}
