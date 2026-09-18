import java.nio.file.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import org.lwjgl.opengl.GL;

// Standalone driver compile/link check. Creates an invisible OpenGL window, no game/VR session.
public class CheckOpticShader {
    static int compile(int type,String path) throws Exception {
        int id=glCreateShader(type);glShaderSource(id,Files.readString(Path.of(path)));glCompileShader(id);
        if(glGetShaderi(id,GL_COMPILE_STATUS)==0)throw new IllegalStateException(glGetShaderInfoLog(id));
        return id;
    }
    public static void main(String[] args) throws Exception {
        if(!glfwInit())throw new IllegalStateException("GLFW initialization failed");
        glfwWindowHint(GLFW_VISIBLE,GLFW_FALSE);
        long window=glfwCreateWindow(32,32,"Optic validation",0,0);
        if(window==0)throw new IllegalStateException("OpenGL context unavailable");
        try {
            glfwMakeContextCurrent(window);GL.createCapabilities();
            String base="src/main/resources/assets/visor_tacz/shaders/core/optic";
            int vertex=compile(GL_VERTEX_SHADER,base+".vsh"),fragment=compile(GL_FRAGMENT_SHADER,base+".fsh");
            int program=glCreateProgram();glAttachShader(program,vertex);glAttachShader(program,fragment);glLinkProgram(program);
            if(glGetProgrami(program,GL_LINK_STATUS)==0)throw new IllegalStateException(glGetProgramInfoLog(program));
            for(String uniform:new String[]{"ModelViewMat","ProjMat","Sampler0","Viewport","AimUV","Zoom","Scope","EyeValid"})
                if(glGetUniformLocation(program,uniform)<0)throw new IllegalStateException("Missing uniform "+uniform);
            glDeleteProgram(program);glDeleteShader(vertex);glDeleteShader(fragment);
            System.out.println("Optic GLSL compile, link and uniforms passed");
        } finally {glfwDestroyWindow(window);glfwTerminate();}
    }
}
