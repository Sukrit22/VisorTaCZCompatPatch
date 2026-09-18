import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL43.*;
import org.lwjgl.opengl.GL;

/** Real-driver reproduction of a late stencil upgrade versus pre-frame setup. */
public class CheckDepthFormat {
    static int texture(int format) {
        int id=glGenTextures();glBindTexture(GL_TEXTURE_2D,id);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D,0,format,32,32,0,
            format==GL_DEPTH32F_STENCIL8?GL_DEPTH_STENCIL:GL_DEPTH_COMPONENT,
            format==GL_DEPTH32F_STENCIL8?GL_FLOAT_32_UNSIGNED_INT_24_8_REV:GL_FLOAT,0L);
        return id;
    }
    static void copy(int from,int to) {
        glCopyImageSubData(from,GL_TEXTURE_2D,0,0,0,0,to,GL_TEXTURE_2D,0,0,0,0,32,32,1);
    }
    public static void main(String[] args) {
        if(!glfwInit())throw new AssertionError("GLFW unavailable");
        glfwWindowHint(GLFW_VISIBLE,GLFW_FALSE);long window=glfwCreateWindow(32,32,"Depth format regression",0,0);
        if(window==0)throw new AssertionError("No GL context");
        try {
            glfwMakeContextCurrent(window);GL.createCapabilities();
            int source=texture(GL_DEPTH32F_STENCIL8),oldCopy=texture(GL_DEPTH_COMPONENT32F);
            if(glGetError()!=GL_NO_ERROR)throw new AssertionError("Setup error");
            copy(source,oldCopy);
            if(glGetError()!=GL_INVALID_OPERATION)throw new AssertionError("Did not reproduce late stencil format failure");
            int preparedCopy=texture(GL_DEPTH32F_STENCIL8);
            for(int i=0;i<100;i++)copy(source,preparedCopy);
            if(glGetError()!=GL_NO_ERROR)throw new AssertionError("Prepared depth copies failed");
            glDeleteTextures(source);glDeleteTextures(oldCopy);glDeleteTextures(preparedCopy);
            System.out.println("Reproduced GL 1282 with late stencil; 100 pre-matched depth copies passed.");
        } finally {glfwDestroyWindow(window);glfwTerminate();}
    }
}
