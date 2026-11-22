package dev.mccastle91.createcontrolling.client;

import dev.mccastle91.createcontrolling.CreateControlling;
import dev.mccastle91.createcontrolling.item.KeyItem;
import dev.mccastle91.createcontrolling.registry.CCContent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = CreateControlling.MOD_ID,value = Dist.CLIENT)
public class KeyRender {
    @SubscribeEvent
    public static void keyRender(RegisterColorHandlersEvent.Item event){
        event.register((stack, tintIndex) -> {
            return KeyItem.getColor(KeyItem.getColorKey(stack));
        }, CCContent.KEY.get());
    }
}
