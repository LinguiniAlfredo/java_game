#version 330 core

in vec3  color;
out vec4 frag_color;

uniform bool hovered;

void main()
{
    if (hovered) {
        frag_color = vec4(color, 1.0);
    } else {
        frag_color = vec4(mix(color, vec3(1.0, 1.0, 1.0), 0.7), 1.0);
    }
}
