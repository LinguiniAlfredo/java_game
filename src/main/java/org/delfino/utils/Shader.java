package org.delfino.utils;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL30.*;

public class Shader {
    int id;

    public Shader(String vertex_path, String fragment_path) {
        String vertex_code, fragment_code;
        try {
            vertex_code   = Files.readString(Paths.get(Shader.class.getResource("/" + vertex_path).toURI()));
            fragment_code = Files.readString(Paths.get(Shader.class.getResource("/" + fragment_path).toURI()));
        } catch (IOException | URISyntaxException e) {
            System.out.println("failed to read shader files...");
            throw new RuntimeException();
        }

        int v, f;
        v = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(v, vertex_code);
        glCompileShader(v);

        f = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(f, fragment_code);
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

    public void set_int(String name, int value) {
        glUniform1i(glGetUniformLocation(this.id, name), value);
    }

    public void set_float(String name, float value) {
        glUniform1f(glGetUniformLocation(this.id, name), value);
    }

    public void set_vec3(String name, Vector3f value) {
        int location = glGetUniformLocation(this.id, name);
        FloatBuffer fb = Utils.vec3_to_fb(value);
        glUniform3fv(location, fb);
    }

    public void set_vec4(String name, Vector4f value) {
        int location = glGetUniformLocation(this.id, name);
        FloatBuffer fb = Utils.vec4_to_fb(value);
        glUniform4fv(location, fb);
    }

    public void set_mat4(String name, Matrix4f value) {
        int location = glGetUniformLocation(this.id, name);
        FloatBuffer fb = Utils.mat4_to_fb(value);
        glUniformMatrix4fv(location, false, fb);
    }
}
