#version 450

layout ( input_attachment_index = 0, set = 0, binding = 0 ) uniform subpassInput identifier;

layout ( std140, set = 1, binding = 0 ) buffer Storage {
    vec4 identifier;
} selection;

void main() {
    selection.identifier = subpassLoad(identifier);
}
