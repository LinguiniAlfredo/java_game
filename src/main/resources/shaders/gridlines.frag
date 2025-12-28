#version 330 core

in vec3 world_pos;
out vec4 FragColor;

uniform vec3 camera_pos;
uniform float grid_scale; // spacing between lines

void main() {
    vec2 coord = world_pos.xz / grid_scale;

    vec2 grid = abs(fract(coord - 0.5) - 0.5) / fwidth(coord);
    float line = min(grid.x, grid.y);

    float grid_color = 1.0 - clamp(line, 0.0, 1.0);

    vec3 color = mix(vec3(0.1), vec3(0.6), grid_color);
    //FragColor = vec4(color, 1.0);
    FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
