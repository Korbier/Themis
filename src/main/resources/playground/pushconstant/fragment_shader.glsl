#version 450

layout(location = 0) out vec4 outFragColor;

layout(push_constant) uniform pushConstant {
    layout( offset = 64 ) vec3 color;
};

void main() {
    outFragColor = vec4(color, 1.0f);
}
