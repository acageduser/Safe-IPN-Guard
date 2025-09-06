package com.example.safeipn.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/** Clamp any slot index used inside AbstractContainerMenu, including mixin-injected handler$ methods. */
@Mixin(value = AbstractContainerMenu.class, priority = 1) // run after most mixins (default is 1000)
public abstract class AbstractContainerMenuMixin {

    @Redirect(
        method = "*",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;"),
        require = 0
    )
    private Object safeGetNonNull(NonNullList<?> list, int index) {
        int n = list.size();
        if (n <= 0) return null;
        if (index < 0) index = 0;
        if (index >= n) index = n - 1;
        return list.get(index);
    }

    @Redirect(
        method = "*",
        at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"),
        require = 0
    )
    private Object safeGetList(List<?> list, int index) {
        int n = list.size();
        if (n <= 0) return null;
        if (index < 0) index = 0;
        if (index >= n) index = n - 1;
        return list.get(index);
    }
}

//end of AbstractContainerMenuMixin.java