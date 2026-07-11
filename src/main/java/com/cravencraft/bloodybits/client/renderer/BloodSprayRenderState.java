package com.cravencraft.bloodybits.client.renderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

public class BloodSprayRenderState extends EntityRenderState {
    public Direction entityDirection;
    public float yRotO;
    public float yRot;
    public float xRotO;
    public float xRot;
    public int life;
    public float xMin;
    public float xMax;
    public float yMin;
    public float yMax;
    public float zMin;
    public float zMax;
    public int red;
    public int green;
    public int blue;
    public float drip;
    
    public Identifier textureLocation;
}
