package dev.visorcompat.tacz.client;
public final class FourHolsterSettings {
 private FourHolsterSettings(){}
 public static boolean enabled(){return CompatSettings.configReady()&&CompatSettings.FOUR_MOUNTS.get();}
 public static boolean emptyRequired(){return !CompatSettings.configReady()||CompatSettings.EMPTY_DRAW.get();}
 public static boolean lockSelected(){return !CompatSettings.configReady()||CompatSettings.LOCK_SELECTED.get();}
 public static int order(){return CompatSettings.configReady()?CompatSettings.MOUNT_ORDER.get():0;}
 public static int binding(int mount){return CompatSettings.configReady()?CompatSettings.MOUNT_BINDINGS[mount].get()-1:-1;}
 public static void enable(boolean value){DualClient.reset();CompatSettings.FOUR_MOUNTS.set(value);CompatSettings.saveMountSettings();}
 public static void toggleEmpty(){CompatSettings.EMPTY_DRAW.set(!emptyRequired());CompatSettings.saveMountSettings();}
 public static void toggleLock(){CompatSettings.LOCK_SELECTED.set(!lockSelected());CompatSettings.saveMountSettings();}
 public static void cycleOrder(){CompatSettings.MOUNT_ORDER.set((order()+1)%3);CompatSettings.saveMountSettings();}
 public static void cycleBinding(int i){CompatSettings.MOUNT_BINDINGS[i].set((binding(i)+2)%10);CompatSettings.saveMountSettings();}
}
