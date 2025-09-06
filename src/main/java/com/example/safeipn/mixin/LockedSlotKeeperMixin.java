package com.example.safeipn.mixin;

import org.anti_ad.mc.ipnext.event.LockedSlotKeeper; // IPN class
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.core.NonNullList; // Mojmaps

import java.util.ArrayList;
import java.util.List;

@Mixin(value = LockedSlotKeeper.class, remap = false) // mixing into a 3rd-party mod -> no remap
public class LockedSlotKeeperMixin {

    // Covers invokeinterface List#get(I)
    @Redirect(
        method = "checkNewItems",
        at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", remap = false)
    )
    private Object clampListGet(List<?> list, int index) {
        return list.isEmpty() ? null : list.get(clamp(index, list.size()));
    }

    // Covers invokevirtual ArrayList#get(I)
    @Redirect(
        method = "checkNewItems",
        at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;get(I)Ljava/lang/Object;", remap = false)
    )
    private Object clampArrayGet(ArrayList<?> list, int index) {
        return list.isEmpty() ? null : list.get(clamp(index, list.size()));
    }

    // Covers invokevirtual NonNullList#get(I) (Mojmaps)
    @Redirect(
        method = "checkNewItems",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;", remap = false)
    )
    private Object clampNonNullGet(NonNullList<?> list, int index) {
        return list.isEmpty() ? null : list.get(clamp(index, list.size()));
    }

    private static int clamp(int idx, int size) {
        if (idx < 0) return 0;
        int max = size - 1;
        return idx > max ? max : idx;
    }
}
//end of LockedSlotKeeperMixin.java