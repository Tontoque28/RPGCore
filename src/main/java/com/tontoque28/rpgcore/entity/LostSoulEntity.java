package com.tontoque28.rpgcore.entity;

import com.tontoque28.rpgcore.RpgCore;
import com.tontoque28.rpgcore.item.SpiritualVesselItem;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public class LostSoulEntity extends CreatureEntity {

    private static final DataParameter<String> DATA_PLAYER_NAME =
            EntityDataManager.defineId(LostSoulEntity.class, DataSerializers.STRING);

    private UUID playerUuid;

    public LostSoulEntity(EntityType<? extends CreatureEntity> entityType, World level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setSilent(true);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PLAYER_NAME, "Unknown");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtGoal(this, PlayerEntity.class, 12.0F));
        this.goalSelector.addGoal(2, new LookRandomlyGoal(this));
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return CreatureEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.FLYING_SPEED, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    public UUID getPlayerUuid() { return this.playerUuid; }
    public void setPlayerUuid(UUID uuid) { this.playerUuid = uuid; }

    public String getPlayerName() { return this.entityData.get(DATA_PLAYER_NAME); }
    public void setPlayerName(String name) { this.entityData.set(DATA_PLAYER_NAME, name != null ? name : "Unknown"); }

    @Override
    public void tick() {
        this.setDeltaMovement(Vector3d.ZERO);
        super.tick();

        if (this.level.isClientSide) {
            if (this.random.nextInt(2) == 0) {
                double angle = (this.tickCount * 0.15D) + (this.random.nextDouble() * Math.PI * 2.0D);
                double radius = 0.55D + this.random.nextDouble() * 0.25D;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;

                this.level.addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + offsetX,
                        this.getY() + 0.1D + this.random.nextDouble() * 0.7D,
                        this.getZ() + offsetZ,
                        -Math.sin(angle) * 0.015D,
                        0.02D,
                        Math.cos(angle) * 0.015D
                );
            }

            if (this.random.nextInt(6) == 0) {
                double angle = this.random.nextDouble() * Math.PI * 2.0D;
                double radius = 0.70D + this.random.nextDouble() * 0.25D;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;

                this.level.addParticle(
                        ParticleTypes.SOUL,
                        this.getX() + offsetX,
                        this.getY() + 0.2D + this.random.nextDouble() * 0.5D,
                        this.getZ() + offsetZ,
                        -Math.sin(angle) * 0.02D,
                        0.025D,
                        Math.cos(angle) * 0.02D
                );
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        if (this.playerUuid != null) {
            compound.putUUID("SoulOwnerUUID", this.playerUuid);
        }
        compound.putString("SoulOwnerName", this.getPlayerName());
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("SoulOwnerUUID")) {
            this.playerUuid = compound.getUUID("SoulOwnerUUID");
        }
        if (compound.contains("SoulOwnerName")) {
            this.setPlayerName(compound.getString("SoulOwnerName"));
        }
    }

    public static LostSoulEntity spawn(PlayerEntity player) {
        World level = player.level;
        if (!level.isClientSide()) {
            for (LostSoulEntity old : level.getEntitiesOfClass(LostSoulEntity.class, player.getBoundingBox().inflate(100.0D),
                    s -> player.getUUID().equals(s.getPlayerUuid()))) {
                old.remove();
            }

            LostSoulEntity soul = RpgCore.LOST_SOUL.get().create(level);
            if (soul != null) {
                soul.moveTo(player.getX(), player.getY() + 0.6D, player.getZ(), player.yRot, player.xRot);
                soul.setPlayerUuid(player.getUUID());
                soul.setPlayerName(player.getName().getString());
                soul.setPersistenceRequired();
                level.addFreshEntity(soul);
                return soul;
            }
        }
        return null;
    }

    @Override
    protected ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof SpiritualVesselItem && !SpiritualVesselItem.isBound(stack) && this.playerUuid != null) {
            if (!player.level.isClientSide) {
                SpiritualVesselItem.bindSoul(stack, this.getPlayerUuid(), this.getPlayerName());
                player.level.playSound(null, this.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                player.sendMessage(new TranslationTextComponent("message.rpgcore.soul_attracted", this.getPlayerName()), Util.NIL_UUID);
                this.remove();
            }
            return ActionResultType.sidedSuccess(player.level.isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return !source.isCreativePlayer();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return null; }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) { return null; }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() { return null; }
}
