package org.delfino.utils;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL30.*;

public class Shader {
    int id;

    public Shader(String vertexPath, String fragmentPath) {
        String vertexCode, fragmentCode;
        try {
            vertexCode   = Files.readString(Paths.get(Shader.class.getResource("/" + vertexPath).toURI()));
            fragmentCode = Files.readString(Paths.get(Shader.class.getResource("/" + fragmentPath).toURI()));

        } catch (IOException | URISyntaxException e) {
            System.out.println("failed to read shader files...");
            throw new RuntimeException();
        }

        int v, f;
        v = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(v, vertexCode);
        glCompileShader(v);

        f = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(f, fragmentCode);
        glCompileShader(f);

        this.id = glCreateProgram();
        glAttachShader(this.id, v);
        glAttachShader(this.id, f);
        glLinkProgram(this.id);

        if (glGetShaderi(v, GL_COMPILE_STATUS) == GL_FALSE) {
            System.out.println("ERROR: Vertex Shader compilation failed!");
            System.out.println(glGetShaderInfoLog(v));
        }
        if (glGetShaderi(f, GL_COMPILE_STATUS) == GL_FALSE) {
            System.out.println("ERROR: Fragment Shader compilation failed!");
            System.out.println(glGetShaderInfoLog(f));
        }

        if (glGetProgrami(this.id, GL_LINK_STATUS) == GL_FALSE) {
            System.out.println("ERROR: Shader linking failed!");
            System.out.println(glGetShaderInfoLog(this.id));
        }

        glDeleteShader(v);
        glDeleteShader(f);
    }

    public void delete() {
        glDeleteProgram(this.id);
    }

    public void use() {
        glUseProgram(this.id);
    }

    public void setInt(String name, int value) {
        glUniform1i(glGetUniformLocation(this.id, name), value);
    }

    public void set_float(String name, float value) {
        glUniform1f(glGetUniformLocation(this.id, name), value);
    }

    public void setVec3(String name, Vector3f value) {
        int location = glGetUniformLocation(this.id, name);
        FloatBuffer fb;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            fb = stack.mallocFloat(3);
            fb.put(value.x).put(value.y).put(value.z);
            fb.flip();
            glUniform3fv(location, fb);
        }
    }

    public void set_vec4(String name, Vector4f v) {
        int location = glGetUniformLocation(this.id, name);
        FloatBuffer fb;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            fb = stack.mallocFloat(4);
            fb.put(v.x).put(v.y).put(v.z).put(v.w);
            fb.flip();
            glUniform4fv(location, fb);
        }
    }

    public void setMat4(String name, Matrix4f m) {
        int location = glGetUniformLocation(this.id, name);
        FloatBuffer fb;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            fb = stack.mallocFloat(16);
            m.get(fb);
            glUniformMatrix4fv(location, false, fb);
        }
    }
}
