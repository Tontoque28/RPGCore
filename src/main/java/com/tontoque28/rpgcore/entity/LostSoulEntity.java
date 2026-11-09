package com.tontoque28.rpgcore.entity;

import com.tontoque28.rpgcore.RpgCore;
import com.tontoque28.rpgcore.item.SpiritualVesselItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class LostSoulEntity extends PathfinderMob {

    private static final EntityDataAccessor<String> DATA_PLAYER_NAME =
            SynchedEntityData.defineId(LostSoulEntity.class, EntityDataSerializers.STRING);

    private UUID playerUuid;

    public LostSoulEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setSilent(true);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PLAYER_NAME, "Unknown");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
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
        this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        super.tick();

        if (this.level().isClientSide()) {
            if (this.random.nextInt(2) == 0) {
                double angle = (this.tickCount * 0.15D) + (this.random.nextDouble() * Math.PI * 2.0D);
                double radius = 0.55D + this.random.nextDouble() * 0.25D;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;

                this.level().addParticle(
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

                this.level().addParticle(
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
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.storeNullable("SoulOwnerUUID", UUIDUtil.CODEC, this.playerUuid);
        output.putString("SoulOwnerName", this.getPlayerName());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("SoulOwnerUUID", UUIDUtil.CODEC).ifPresent(uuid -> this.playerUuid = uuid);
        input.getString("SoulOwnerName").ifPresent(this::setPlayerName);
    }

    public static LostSoulEntity spawn(Player player) {
        Level level = player.level();
        if (!level.isClientSide()) {
            level.getEntitiesOfClass(LostSoulEntity.class, player.getBoundingBox().inflate(100.0D),
                    s -> player.getUUID().equals(s.getPlayerUuid())).forEach(LostSoulEntity::discard);

            LostSoulEntity soul = RpgCore.LOST_SOUL.get().create(level, EntitySpawnReason.TRIGGERED);
            if (soul != null) {
                soul.snapTo(player.getX(), player.getY() + 0.6D, player.getZ(), player.getYRot(), player.getXRot());
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
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof SpiritualVesselItem && !SpiritualVesselItem.isBound(stack) && this.playerUuid != null) {
            if (!player.level().isClientSide()) {
                SpiritualVesselItem.bindSoul(stack, this.getPlayerUuid(), this.getPlayerName());
                player.level().playSound(null, this.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                player.sendSystemMessage(Component.translatable("message.rpgcore.soul_attracted", this.getPlayerName()));
                this.discard();
            }
            return player.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean causeFallDamage(double pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
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