package me.jellysquid.mods.sodium.client.render.chunk.vertex.format.impl;

import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexAttributeFormat;
import me.jellysquid.mods.sodium.client.gl.attribute.GlVertexFormat;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkMeshAttribute;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import me.jellysquid.mods.sodium.api.util.ColorABGR;
import me.jellysquid.mods.sodium.api.util.ColorU8;
import org.lwjgl.system.MemoryUtil;

public class CompactChunkVertex implements ChunkVertexType {
    public static final GlVertexFormat<ChunkMeshAttribute> VERTEX_FORMAT = GlVertexFormat.builder(ChunkMeshAttribute.class, 16)
            .addElement(ChunkMeshAttribute.VERTEX_DATA, 0, GlVertexAttributeFormat.UNSIGNED_INT, 4, false, true)
            .build();

    public static final int STRIDE = 16;

    private static final int POSITION_MAX_VALUE = 65536;
    private static final int TEXTURE_MAX_VALUE = 32768;

    private static final float MODEL_ORIGIN = 8.0f;
    private static final float MODEL_SCALE = 32.0f;

    @Override
    public GlVertexFormat<ChunkMeshAttribute> getVertexFormat() {
        return VERTEX_FORMAT;
    }

    @Override
    public ChunkVertexEncoder getEncoder() {
        return (ptr, material, vertex, sectionIndex) -> {
            // Positions
            int position = encodePosition(vertex.x) | (encodePosition(vertex.y) << 16);
            int drawParameters = encodeDrawParameters(material, sectionIndex);

            // Colors
            int color = encodeColor(vertex.color);
            int light = encodeLight(vertex.light);

            // Texture
            int texture = encodeTexture(vertex.u, vertex.v);

            // Write to buffer
            MemoryUtil.memPutInt(ptr, position | drawParameters);
            MemoryUtil.memPutInt(ptr + 4, color | light);
            MemoryUtil.memPutInt(ptr + 8, texture);

            return ptr + STRIDE;
        };
    }

    private static int encodePosition(float value) {
        // Pre-calculate constant
        final float scale = POSITION_MAX_VALUE / MODEL_SCALE;

        return (int) ((MODEL_ORIGIN + value) * scale);
    }

    private static int encodeDrawParameters(Material material, int sectionIndex) {
        // Combine encoding steps
        return ((sectionIndex & 0xFF) << 8) | ((material.bits() & 0xFF) << 0);
    }

    private static int encodeColor(int color) {
        // Use half-precision floats
        return ColorU8.byteToFloat(ColorABGR.unpackAlpha(color)) * 0xFFFF;
    }

    private static int encodeLight(int light) {
        // Pack color and light data into a single byte
        return ((light >> 4) & 0xF) | ((light >> 20) & 0xF);
    }

    private static int encodeTexture(float u, float v) {
        // Use integer rounding
        return ((Math.round(u * TEXTURE_MAX_VALUE) & 0xFFFF) << 0) |
                ((Math.round(v * TEXTURE_MAX_VALUE) & 0xFFFF) << 16);
    }
        }
            
