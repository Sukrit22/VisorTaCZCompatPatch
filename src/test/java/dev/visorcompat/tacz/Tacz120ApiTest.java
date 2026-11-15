package dev.visorcompat.tacz;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import static org.junit.jupiter.api.Assertions.*;

/** Checks bytecode without initializing Minecraft's rendering classes. */
class Tacz120ApiTest {
    private ClassNode read(String name) throws Exception {
        try (var in = getClass().getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull(in, name);
            var node = new ClassNode();
            new ClassReader(in).accept(node, 0);
            return node;
        }
    }

    @Test void stencilClearRemainsInTheHookedRenderOverload() throws Exception {
        var model = read("com/tacz/guns/client/model/BedrockGunModel");
        var render = model.methods.stream().filter(m -> m.name.equals("render") &&
                m.desc.endsWith("IIFFFFLnet/minecraft/client/renderer/MultiBufferSource;)V")).findFirst().orElseThrow();
        boolean found = false;
        for (var instruction : render.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals("com/mojang/blaze3d/systems/RenderSystem") &&
                    call.name.equals("clear") && call.desc.equals("(IZ)V")) found = true;
        }
        assertTrue(found, "TaCZ moved its stencil clear; update GunStencilMixin before releasing");
    }

    @Test void legacySoundBridgeCallsTheNewReturnDescriptor() throws Exception {
        var bridge = read("dev/visorcompat/tacz/mixin/LegacyGunSoundMixin");
        var method = bridge.methods.stream().filter(m -> m.name.equals("getSounds")).findFirst().orElseThrow();
        assertEquals("(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;", method.desc);
        boolean found = false;
        for (var instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.name.equals("getSounds") &&
                    call.desc.equals("(Ljava/lang/String;)Lcom/tacz/guns/client/resource/pojo/display/SoundData;")) found = true;
        }
        assertTrue(found, "The legacy bridge must call the SoundData overload, not recurse");
    }
}
