#version 450

const vec3 positions[6] = vec3[6](
vec3(-0.5f, -0.5f, 0.0f),
vec3(-0.5f,  0.5f, 0.0f),
vec3( 0.5f,  0.5f, 0.0f),
vec3(-0.5f, -0.5f, 0.0f),
vec3( 0.5f,  0.5f, 0.0f),
vec3( 0.5f, -0.5f, 0.0f)
);

const vec2 textures[6] = vec2[6](
vec2( 0.0f, 1.0f),
vec2( 0.0f, 0.0f),
vec2( 1.0f, 0.0f),
vec2( 0.0f, 1.0f),
vec2( 1.0f, 0.0f),
vec2( 1.0f, 1.0f)
);

layout(location = 0) out vec2 outTextureCoords;

void main() {
    gl_Position      = vec4(positions[gl_VertexIndex], 1.0f);
    outTextureCoords = textures[gl_VertexIndex];
}