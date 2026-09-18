import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import org.lwjgl.opengl.GL;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.visorcompat.tacz.client.StencilScope;
import java.util.Arrays;

public class CheckStencilScope {
    static int[] snapshot(){return new int[]{glIsEnabled(GL_STENCIL_TEST)?1:0,glGetInteger(GL_STENCIL_FUNC),glGetInteger(GL_STENCIL_REF),
        glGetInteger(GL_STENCIL_VALUE_MASK),glGetInteger(GL_STENCIL_WRITEMASK),glGetInteger(GL_STENCIL_FAIL),glGetInteger(GL_STENCIL_PASS_DEPTH_FAIL),glGetInteger(GL_STENCIL_PASS_DEPTH_PASS),
        glGetInteger(GL_STENCIL_BACK_FUNC),glGetInteger(GL_STENCIL_BACK_REF),glGetInteger(GL_STENCIL_BACK_VALUE_MASK),glGetInteger(GL_STENCIL_BACK_WRITEMASK),
        glGetInteger(GL_STENCIL_BACK_FAIL),glGetInteger(GL_STENCIL_BACK_PASS_DEPTH_FAIL),glGetInteger(GL_STENCIL_BACK_PASS_DEPTH_PASS),glGetInteger(GL_STENCIL_CLEAR_VALUE)};}
    public static void main(String[] args){
        if(!glfwInit())throw new IllegalStateException("GLFW unavailable");
        glfwWindowHint(GLFW_VISIBLE,GLFW_FALSE);long window=glfwCreateWindow(32,32,"Stencil validation",0,0);
        if(window==0)throw new IllegalStateException("No GL context");
        try {
            glfwMakeContextCurrent(window);GL.createCapabilities();RenderSystem.initRenderThread();
            glEnable(GL_STENCIL_TEST);RenderSystem.stencilFunc(GL_EQUAL,7,127);RenderSystem.stencilMask(63);
            RenderSystem.stencilOp(GL_KEEP,GL_INCR,GL_REPLACE);RenderSystem.clearStencil(3);
            glStencilFuncSeparate(GL_BACK,GL_NOTEQUAL,2,31);glStencilMaskSeparate(GL_BACK,15);
            int[] original=snapshot();StencilScope.begin(true);
            if(glIsEnabled(GL_STENCIL_TEST))throw new AssertionError("Scope did not disable stencil");
            RenderSystem.stencilFunc(GL_GREATER,127,255);RenderSystem.stencilOp(GL_KEEP,GL_KEEP,GL_KEEP);RenderSystem.stencilMask(255);RenderSystem.clearStencil(0);
            StencilScope.begin(true);RenderSystem.stencilFunc(GL_ALWAYS,0,255);StencilScope.end();StencilScope.end();
            if(!Arrays.equals(original,snapshot()))throw new AssertionError("State mismatch "+Arrays.toString(snapshot()));
            StencilScope.begin(false);StencilScope.end();
            if(!Arrays.equals(original,snapshot()) || StencilScope.active() || glGetError()!=GL_NO_ERROR)throw new AssertionError("Scope/state/GL error");
            System.out.println("Stencil state restoration, nesting, inactive path and GL error checks passed");
        } finally {glfwDestroyWindow(window);glfwTerminate();}
    }
}
