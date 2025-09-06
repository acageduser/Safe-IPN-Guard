package com.example.safeipn.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Clamp the slot id before the click packet is built/sent. */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    // slotId is the SECOND int parameter of handleInventoryMouseClick
    @ModifyVariable(
        method = "handleInventoryMouseClick",
        at = @At("HEAD"),
        ordinal = 1,      // 0 = containerId, 1 = slotId, 2 = button
        argsOnly = true
    )
    private int safeipn$clampSlotId(int slotId) {
        AbstractContainerMenu menu = Minecraft.getInstance().player != null
                ? Minecraft.getInstance().player.containerMenu : null;

        int size = (menu != null) ? menu.slots.size() : 0;
        if (size <= 0) return 0;
        if (slotId < 0) return 0;
        if (slotId >= size) return size - 1;
        return slotId;
    }
}
//end of MultiPlayerGameModeMixin.java