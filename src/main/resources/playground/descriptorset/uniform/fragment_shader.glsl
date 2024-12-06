#version 450

layout(location = 0) out vec4 outFragColor;

layout(set = 0, binding = 0) uniform Color {
    vec4 data;
} color;

layout(set = 0, binding = 1) uniform ColorDyn {
    vec4 data;
} colordyn;

void main() {
    outFragColor = vec4( color.data.r, colordyn.data.g, colordyn.data.b, 1.0f );
}
