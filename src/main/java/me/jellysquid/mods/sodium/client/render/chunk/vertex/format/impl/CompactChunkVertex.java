package me.jellysquid.mods.sodium.client.render.chunk.vertex.format.impl;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkMeshAttribute;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;

public class CompactChunkVertex implements ChunkVertexType {
    public static final VertexFormat<ChunkMeshAttribute> VERTEX_FORMAT = VertexFormat.builder(ChunkMeshAttribute.class)
            .addElement(ChunkMeshAttribute.VERTEX_DATA, 0, VertexFormatElement.Type.FLOAT, 4, false, true)
            .build();

    private static final int STRIDE = 16;

    private static final float MODEL_ORIGIN = 8.0f;
    private static final float MODEL_SCALE = 32.0f;

    @Override
    public VertexFormat<ChunkMeshAttribute> getVertexFormat() {
        return VERTEX_FORMAT;
    }

    @Override
    public ChunkVertexEncoder getEncoder() {
        return (ptr, material, vertex, sectionIndex) -> {
            // Positions
            int position = (int) ((vertex.x - MODEL_ORIGIN) * MODEL_SCALE);
            int drawParameters = (sectionIndex & 0xFF) << 8 | (material.bits() & 0xFF);

            // Colors
            int color = ColorABGR.pack(vertex.color);
            int light = (vertex.light >> 4) & 0xF;

            // Texture
            int texture = (int) (vertex.u * 32768.0f) | ((int) (vertex.v * 32768.0f) << 16);

            // Write to buffer
            MemoryUtil.memPutInt(ptr, position | drawParameters);
            MemoryUtil.memPutInt(ptr + 4, color | light);
            MemoryUtil.memPutInt(ptr + 8, texture);

            return ptr + STRIDE;
        };
    }

    private static int encodePosition(float value) {
        return (int) (value * MODEL_SCALE);
    }

    private static int encodeDrawParameters(Material material, int sectionIndex) {
        return (sectionIndex & 0xFF) << 8 | (material.bits() & 0xFF);
    }

    private static int encodeColor(int color) {
        return ColorABGR.pack(color);
    }

    private static int encodeLight(int light) {
        return (light >> 4) & 0xF;
    }

    private static int encodeTexture(float u, float v) {
        return (int) (u * 32768.0f) | ((int) (v * 32768.0f) << 16);
    }
    }
                                 
