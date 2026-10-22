package dev.alien.client.cosmetic.mesh;

/** Read-only triangle stream in Minecraft model pixels; every three vertices form a face. */
public interface MeshView {
    int vertexCount();
    float x(int vertex);
    float y(int vertex);
    float z(int vertex);
    float nx(int vertex);
    float ny(int vertex);
    float nz(int vertex);
    int material(int vertex);
    float shade(int vertex);
    default int triangleCount() { return vertexCount() / 3; }
}
