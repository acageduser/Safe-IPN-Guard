# Safe IPN Guard

A tiny **client-side** Fabric compatibility shim that prevents an `IndexOutOfBoundsException` crash when using **Inventory Profiles Next** (IPN) together with **Tiny Item Animations** (TIA).
It clamps out-of-range slot indices before Minecraft or other mods read them, so automated moves (like the **UP** arrow action) don’t crash the client.

> Confirmed working on Minecraft **1.21.8** with Fabric Loader **0.16.14**, IPN **2.1.11**, TIA **1.2.3**.

---

## Why this exists

When pressing **UP** (from *Too Many Shortcuts*) to move matching items into a nearby container, IPN issues synthetic container clicks.
With TIA present, its mixin (`tia$injectOnSlotClick`) reads the menu’s slot list with the **raw** slot id. In some cases the id points past the end of the slot list (e.g., `slotId=64` while the menu has only `46` slots), which throws:

```
java.lang.IndexOutOfBoundsException: Index 64 out of bounds for length 46
  at net.minecraft.core.NonNullList.get
  at net.minecraft.world.inventory.AbstractContainerMenu.slotClick(...)
  at ... inventoryprofilesnext ...
  at ... tia$injectOnSlotClick ...
```

Removing TIA avoids the crash, but you lose its item animations. **Safe IPN Guard** keeps both mods working together.

---

## What this mod does (the fix)

We add small, defensive redirects/clamps around slot access on the client:

- **Clamp slot id on reads** inside `AbstractContainerMenu#getSlot(int)` so any out-of-range id maps to the nearest valid slot instead of crashing.
- **Clamp the id passed to clicks** on the way in (`MultiPlayerGameMode#handleInventoryMouseClick`), reducing the chance that downstream code sees a bogus index.
- **Guard IPN’s own list reads** in `LockedSlotKeeper#checkNewItems` (via no-remap redirects for `List#get`, `ArrayList#get`, and `NonNullList#get`).

No game logic is changed; we only prevent illegal indices from blowing up the client. Worst case, a bad index resolves to the last valid slot instead of crashing.

---

## Reproduction (original issue)

1. Open any container (e.g., a chest) that already contains an item you also have in your inventory.  
2. Press **UP** to “move matching items to container”.  
3. Client crashes with `Index ... out of bounds for length ...`.  
4. Disable **Tiny Item Animations** → the crash disappears.  
5. Re-enable **Tiny Item Animations** + install **Safe IPN Guard** → action works and no crash.

---

## Installation

- Fabric Loader and Fabric API required.  
- Drop `safeipn-<version>.jar` into your **client**’s `mods` folder.  
- Keep your usual mods:
  - Inventory Profiles Next
  - Tiny Item Animations
  - *(Optional)* Too Many Shortcuts, Stack to Nearby Chests, etc.

> **Client-side only.** Not required on servers.

---

## Compatibility & scope

- Targeted at **Minecraft 1.21.8 / Fabric**.  
- Designed specifically to smooth the **IPN + TIA** interaction; it’s conservative and should be harmless alongside other QoL/container mods.  
- If another mod throws a *different* exception path, open an issue with your crash log.

---

## Building from source

```bash
# Java 21 toolchain
# Optional: put the IPN jar into ./libs for compileOnly hints (not bundled)

./gradlew clean build
# resulting jar: build/libs/safeipn-<version>.jar
```

---

## Known limitations

- Clamping means a completely bogus slot id may resolve to the last valid slot rather than “doing nothing”.  
  In practice this avoids the crash and has no observable side-effects for UP/quick-stack workflows.

---

## Credits / upstream projects

Please support the original authors and projects:

- **Inventory Profiles Next (IPN)** — by blackd & contributors  
  Modrinth: https://modrinth.com/mod/inventory-profiles-next

- **libIPN** (IPN’s shared library)  
  Modrinth: https://modrinth.com/mod/libipn

- **Tiny Item Animations (TIA)** — by Trivaxy  
  Modrinth: https://modrinth.com/mod/tiny-item-animations

- **Stack to Nearby Chests** — by xiaocihua1337 *(not the cause of this crash, but commonly co-installed)*  
  CurseForge: https://www.curseforge.com/minecraft/mc-mods/stack-to-nearby-chests

- **Too Many Shortcuts** — by KingTux *(not the cause of this crash, but commonly co-installed)*  
  Modrinth: https://modrinth.com/mod/too-many-shortcuts

---

## License

MIT (same spirit as many small Fabric shims). See `LICENSE` in the repo.

---

## FAQ

**Q: Is this a fork of IPN or TIA?**  
A: No. It’s a tiny, independent guard that only adds mixins to clamp indices.

**Q: Server needs it?**  
A: No — client-side only.

**Q: Can you upstream this?**  
A: This repo documents the fix; feel free to open PRs/issues with IPN/TIA upstreams referencing this approach.

---

## Changelog

- **1.0.0** — Initial release. Adds clamping mixins for `AbstractContainerMenu`, `MultiPlayerGameMode`, and IPN’s `LockedSlotKeeper`.
