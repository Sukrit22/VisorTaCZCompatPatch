import java.nio.file.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import org.lwjgl.opengl.GL;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

// Standalone driver compile/link check. Creates an invisible OpenGL window, no game/VR session.
public class CheckOpticShader {
    static int compile(int type,String path) throws Exception {
        int id=glCreateShader(type);glShaderSource(id,Files.readString(Path.of(path)));glCompileShader(id);
        if(glGetShaderi(id,GL_COMPILE_STATUS)==0)throw new IllegalStateException(glGetShaderInfoLog(id));
        return id;
    }
    static void preservesUnmagnifiedGeometry(int program) {
        int vao=glGenVertexArrays(),vertices=glGenBuffers(),scene=glGenTextures();
        try {
            glBindVertexArray(vao);glBindBuffer(GL_ARRAY_BUFFER,vertices);
            glBufferData(GL_ARRAY_BUFFER,new float[]{-1,-1,0, 1,-1,0, -1,1,0, 1,1,0},GL_STATIC_DRAW);
            int position=glGetAttribLocation(program,"Position");
            glEnableVertexAttribArray(position);glVertexAttribPointer(position,3,GL_FLOAT,false,0,0L);
            glUseProgram(program);
            float[] identity={1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1};
            glUniformMatrix4fv(glGetUniformLocation(program,"ModelViewMat"),false,identity);
            glUniformMatrix4fv(glGetUniformLocation(program,"ProjMat"),false,identity);
            glUniform4f(glGetUniformLocation(program,"Viewport"),0,0,32,32);
            glUniform2f(glGetUniformLocation(program,"AimUV"),.5f,.5f);
            glUniform1f(glGetUniformLocation(program,"EyeValid"),1);
            glUniform1i(glGetUniformLocation(program,"Sampler0"),0);
            glBindTexture(GL_TEXTURE_2D,scene);
            ByteBuffer blue=ByteBuffer.allocateDirect(4);blue.put(new byte[]{0,0,(byte)255,(byte)255}).flip();
            glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA8,1,1,0,GL_RGBA,GL_UNSIGNED_BYTE,blue);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
            glViewport(0,0,32,32);glDisable(GL_DEPTH_TEST);glEnable(GL_BLEND);glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
            ByteBuffer pixel=ByteBuffer.allocateDirect(4);
            for(int scope=0;scope<=1;scope++)for(int zoom=1;zoom<=2;zoom++) {
                glClearColor(0,1,0,1);glClear(GL_COLOR_BUFFER_BIT);
                glUniform1f(glGetUniformLocation(program,"Scope"),scope);
                glUniform1f(glGetUniformLocation(program,"Zoom"),zoom);
                glDrawArrays(GL_TRIANGLE_STRIP,0,4);
                pixel.clear();glReadPixels(3,3,1,1,GL_RGBA,GL_UNSIGNED_BYTE,pixel);
                // Green stands for the already drawn gun, blue for the pre-gun scene.
                int channel=zoom==1?1:2;
                if(Byte.toUnsignedInt(pixel.get(channel))<250 || Byte.toUnsignedInt(pixel.get(3-channel))>5)
                    throw new IllegalStateException("Lens overwrote wrong geometry: scope="+scope+", zoom="+zoom);
            }
        } finally {glUseProgram(0);glDeleteTextures(scene);glDeleteBuffers(vertices);glDeleteVertexArrays(vao);}
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
            preservesUnmagnifiedGeometry(program);
            glDeleteProgram(program);glDeleteShader(vertex);glDeleteShader(fragment);
            System.out.println("Optic GLSL compile/link/uniforms and 1x geometry preservation / 2x scene sampling passed");
        } finally {glfwDestroyWindow(window);glfwTerminate();}
    }
}
