package com.cuzzi.reforged.server.magic;

import com.cuzzi.reforged.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RuneEntity extends Entity {

    private static final EntityDataAccessor<Float> DATA_SCALE = SynchedEntityData.defineId(RuneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SPIN_SPEED = SynchedEntityData.defineId(RuneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_LIFESPAN = SynchedEntityData.defineId(RuneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_TEXTURE = SynchedEntityData.defineId(RuneEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(RuneEntity.class, EntityDataSerializers.INT);

    // Nuevos ángulos 3D
    private static final EntityDataAccessor<Float> DATA_PITCH = SynchedEntityData.defineId(RuneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_YAW = SynchedEntityData.defineId(RuneEntity.class, EntityDataSerializers.FLOAT);

    public float currentRotation = 0;
    public float previousRotation = 0;
    private int age = 0;

    public RuneEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public RuneEntity(Level level, double x, double y, double z, float pitch, float yaw, float scale, float speed, int lifespanTicks, String texturePath, int hexColor) {
        this(ModEntities.RUNE.get(), level);
        this.setPos(x, y, z);
        this.setRunePitch(pitch);
        this.setRuneYaw(yaw);
        this.setScale(scale);
        this.setSpinSpeed(speed);
        this.setLifespan(lifespanTicks);
        this.setTexture(texturePath);
        this.setColor(hexColor);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SCALE, 1.0f);
        builder.define(DATA_SPIN_SPEED, 5.0f);
        builder.define(DATA_LIFESPAN, 100);
        builder.define(DATA_TEXTURE, "reforged:textures/entity/rune/placeholder_rune.png");
        builder.define(DATA_COLOR, 0xFFFFFF);
        builder.define(DATA_PITCH, 90.0f); // 90 = Plana en el suelo por defecto
        builder.define(DATA_YAW, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();
        this.age++;

        this.previousRotation = this.currentRotation;
        this.currentRotation += getSpinSpeed();
        if (this.currentRotation >= 360) {
            this.currentRotation -= 360;
            this.previousRotation -= 360;
        }
        if (this.currentRotation <= -360) {
            this.currentRotation += 360;
            this.previousRotation += 360;
        }

        if (!this.level().isClientSide && this.age >= getLifespan()) {
            this.discard();
        }
    }

    /**
     * Sistema avanzado de invocación múltiple (Stack).
     * Dispara una sucesión de runas en la dirección de la mirada del jugador.
     */
    public static void summonRuneStack(ServerPlayer player, int count, float spacing, float baseScale, float scaleGrowth, float baseSpeed, int lifespan, String texture, int color) {
        // Punto de inicio: Ligeramente por debajo de los ojos del jugador
        Vec3 origin = player.position().add(0, player.getEyeHeight() * 0.8, 0);
        Vec3 lookDir = player.getLookAngle(); // Vector normalizado de la mirada

        // Orientación: Las runas mirarán hacia el jugador (escudo vertical perpendicular a la mirada)
        float pitch = player.getXRot();
        float yaw = player.getYRot();

        for (int i = 0; i < count; i++) {
            // Escala progresiva
            float currentScale = baseScale + (i * scaleGrowth);
            // El giro se invierte en cada runa sucesiva para un efecto mágico brutal
            float currentSpeed = baseSpeed * (i % 2 == 0 ? 1 : -1);

            // Avanzamos en línea recta según la dirección de la mirada
            Vec3 pos = origin.add(lookDir.scale(i * spacing));

            RuneEntity rune = new RuneEntity(player.level(), pos.x, pos.y, pos.z, pitch, yaw, currentScale, currentSpeed, lifespan, texture, color);
            player.level().addFreshEntity(rune);
        }
    }

    // --- Getters y Setters ---
    public float getScale() { return this.entityData.get(DATA_SCALE); }
    public void setScale(float scale) { this.entityData.set(DATA_SCALE, scale); }

    public float getSpinSpeed() { return this.entityData.get(DATA_SPIN_SPEED); }
    public void setSpinSpeed(float speed) { this.entityData.set(DATA_SPIN_SPEED, speed); }

    public int getLifespan() { return this.entityData.get(DATA_LIFESPAN); }
    public void setLifespan(int lifespan) { this.entityData.set(DATA_LIFESPAN, lifespan); }

    public String getTexture() { return this.entityData.get(DATA_TEXTURE); }
    public void setTexture(String texture) { this.entityData.set(DATA_TEXTURE, texture); }

    public int getColor() { return this.entityData.get(DATA_COLOR); }
    public void setColor(int color) { this.entityData.set(DATA_COLOR, color); }

    public float getRunePitch() { return this.entityData.get(DATA_PITCH); }
    public void setRunePitch(float pitch) { this.entityData.set(DATA_PITCH, pitch); }

    public float getRuneYaw() { return this.entityData.get(DATA_YAW); }
    public void setRuneYaw(float yaw) { this.entityData.set(DATA_YAW, yaw); }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.age = tag.getInt("Age");
        setScale(tag.getFloat("Scale"));
        setSpinSpeed(tag.getFloat("SpinSpeed"));
        setLifespan(tag.getInt("Lifespan"));
        if (tag.contains("Texture")) setTexture(tag.getString("Texture"));
        if (tag.contains("Color")) setColor(tag.getInt("Color"));
        if (tag.contains("Pitch")) setRunePitch(tag.getFloat("Pitch"));
        if (tag.contains("Yaw")) setRuneYaw(tag.getFloat("Yaw"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.age);
        tag.putFloat("Scale", getScale());
        tag.putFloat("SpinSpeed", getSpinSpeed());
        tag.putInt("Lifespan", getLifespan());
        tag.putString("Texture", getTexture());
        tag.putInt("Color", getColor());
        tag.putFloat("Pitch", getRunePitch());
        tag.putFloat("Yaw", getRuneYaw());
    }
}