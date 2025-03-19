#version 450

layout(location = 0) in vec2 inTextureCoords;
layout(location = 0) out vec4 outFragColor;

layout(set = 0, binding = 0) uniform sampler2D textureSampler;

void main() {
    outFragColor = texture(textureSampler, inTextureCoords);
}
