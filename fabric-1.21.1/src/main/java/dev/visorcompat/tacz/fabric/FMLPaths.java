package dev.visorcompat.tacz.fabric;
public enum FMLPaths {
 CONFIGDIR;
 public java.nio.file.Path get(){
  var loader=net.fabricmc.loader.api.FabricLoader.getInstance();
  // Like Forge's uninitialized path, return null before the loader starts.
  // Pure profile tests can then read bundled defaults without a game instance.
  try { loader.getGameDir(); }
  catch(IllegalStateException notStarted){ return null; }
  return loader.getConfigDir();
 }
}
