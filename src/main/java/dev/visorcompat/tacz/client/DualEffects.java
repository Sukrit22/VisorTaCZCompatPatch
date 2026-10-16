package dev.visorcompat.tacz.client;
import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.DualNetwork;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.sound.SoundPlayManager;
import net.minecraft.client.Minecraft;
public final class DualEffects {
 public static void receive(DualNetwork.Shot shot){var mc=Minecraft.getInstance();if(!DualClient.active()||mc.player==null||shot.slot()<0||shot.slot()>8||!Profiles.key(mc.player.getInventory().getItem(shot.slot())).equals(Profiles.key(shot.stack())))return;
  var gun=IGun.getIGunOrNull(shot.stack());if(gun==null)return;var display=TimelessAPI.getGunDisplay(shot.stack()).orElse(null);var index=TimelessAPI.getCommonGunIndex(gun.getGunId(shot.stack())).orElse(null);if(display==null||index==null)return;
  DualSounds.play(shot.slot(),display.getSounds(shot.silence()?com.tacz.guns.sound.SoundManager.SILENCE_SOUND:com.tacz.guns.sound.SoundManager.SHOOT_SOUND),shot.silence()?.6f:.8f,.9f+mc.player.getRandom().nextFloat()*.125f,64);
 }
}
