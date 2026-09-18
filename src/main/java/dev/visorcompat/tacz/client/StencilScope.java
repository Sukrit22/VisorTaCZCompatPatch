package dev.visorcompat.tacz.client;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import java.util.ArrayDeque;

/** Save both stencil faces; never clear or resize a framebuffer owned by Visor. */
public final class StencilScope {
    private record Face(int function,int reference,int mask,int fail,int depthFail,int pass,int writeMask){}
    private record State(boolean active,boolean enabled,Face front,Face back,int clear){}
    private static final ArrayDeque<State> STACK=new ArrayDeque<>();
    public static boolean active(){return !STACK.isEmpty() && STACK.peek().active();}
    public static void begin(boolean active){
        if(!active){STACK.push(new State(false,false,null,null,0));return;}
        var front=new Face(GL11.glGetInteger(GL11.GL_STENCIL_FUNC),GL11.glGetInteger(GL11.GL_STENCIL_REF),GL11.glGetInteger(GL11.GL_STENCIL_VALUE_MASK),
            GL11.glGetInteger(GL11.GL_STENCIL_FAIL),GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_FAIL),GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_PASS),GL11.glGetInteger(GL11.GL_STENCIL_WRITEMASK));
        var back=new Face(GL11.glGetInteger(GL20.GL_STENCIL_BACK_FUNC),GL11.glGetInteger(GL20.GL_STENCIL_BACK_REF),GL11.glGetInteger(GL20.GL_STENCIL_BACK_VALUE_MASK),
            GL11.glGetInteger(GL20.GL_STENCIL_BACK_FAIL),GL11.glGetInteger(GL20.GL_STENCIL_BACK_PASS_DEPTH_FAIL),GL11.glGetInteger(GL20.GL_STENCIL_BACK_PASS_DEPTH_PASS),GL11.glGetInteger(GL20.GL_STENCIL_BACK_WRITEMASK));
        STACK.push(new State(true,GL11.glIsEnabled(GL11.GL_STENCIL_TEST),front,back,GL11.glGetInteger(GL11.GL_STENCIL_CLEAR_VALUE)));GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
    public static void end(){
        if(STACK.isEmpty())return;var state=STACK.pop();if(!state.active())return;
        var f=state.front();
        com.mojang.blaze3d.systems.RenderSystem.stencilFunc(f.function(),f.reference(),f.mask());
        com.mojang.blaze3d.systems.RenderSystem.stencilOp(f.fail(),f.depthFail(),f.pass());
        com.mojang.blaze3d.systems.RenderSystem.stencilMask(f.writeMask());
        restore(GL11.GL_BACK,state.back());
        com.mojang.blaze3d.systems.RenderSystem.clearStencil(state.clear());
        if(state.enabled())GL11.glEnable(GL11.GL_STENCIL_TEST);else GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
    private static void restore(int face,Face s){
        GL20.glStencilFuncSeparate(face,s.function(),s.reference(),s.mask());
        GL20.glStencilOpSeparate(face,s.fail(),s.depthFail(),s.pass());GL20.glStencilMaskSeparate(face,s.writeMask());
    }
}
