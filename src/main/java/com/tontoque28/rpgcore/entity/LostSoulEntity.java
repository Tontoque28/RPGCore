package com.tontoque28.rpgcore.entity;

import com.google.common.base.Predicate;
import com.tontoque28.rpgcore.item.SpiritualVesselItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class LostSoulEntity extends EntityCreature {

    private static final DataParameter<String> DATA_PLAYER_NAME =
            EntityDataManager.createKey(LostSoulEntity.class, DataSerializers.STRING);

    private UUID playerUuid;

    public LostSoulEntity(World world) {
        super(world);
        this.setSize(0.5F, 0.5F);
        this.setNoGravity(true);
        this.setSilent(true);
        this.noClip = true;
        this.isImmuneToFire = true;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(DATA_PLAYER_NAME, "Unknown");
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 12.0F));
        this.tasks.addTask(2, new EntityAILookIdle(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
    }

    public UUID getPlayerUuid() {
        return this.playerUuid;
    }

    public void setPlayerUuid(UUID uuid) {
        this.playerUuid = uuid;
    }

    public String getPlayerName() {
        return this.dataManager.get(DATA_PLAYER_NAME);
    }

    public void setPlayerName(String name) {
        this.dataManager.set(DATA_PLAYER_NAME, name != null ? name : "Unknown");
    }

    @Override
    public void onUpdate() {
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        super.onUpdate();

        if (this.world.isRemote) {
            if (this.rand.nextInt(2) == 0) {
                double angle = (this.ticksExisted * 0.15D) + (this.rand.nextDouble() * Math.PI * 2.0D);
                double radius = 0.55D + this.rand.nextDouble() * 0.25D;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;

                this.world.spawnParticle(
                        EnumParticleTypes.SPELL_INSTANT,
                        this.posX + offsetX,
                        this.posY + 0.1D + this.rand.nextDouble() * 0.7D,
                        this.posZ + offsetZ,
                        -Math.sin(angle) * 0.015D,
                        0.02D,
                        Math.cos(angle) * 0.015D
                );
            }

            if (this.rand.nextInt(6) == 0) {
                double angle = this.rand.nextDouble() * Math.PI * 2.0D;
                double radius = 0.70D + this.rand.nextDouble() * 0.25D;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;

                this.world.spawnParticle(
                        EnumParticleTypes.END_ROD,
                        this.posX + offsetX,
                        this.posY + 0.2D + this.rand.nextDouble() * 0.5D,
                        this.posZ + offsetZ,
                        -Math.sin(angle) * 0.02D,
                        0.025D,
                        Math.cos(angle) * 0.02D
                );
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.playerUuid != null) {
            compound.setUniqueId("SoulOwnerUUID", this.playerUuid);
        }
        compound.setString("SoulOwnerName", this.getPlayerName());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasUniqueId("SoulOwnerUUID")) {
            this.playerUuid = compound.getUniqueId("SoulOwnerUUID");
        }
        if (compound.hasKey("SoulOwnerName")) {
            this.setPlayerName(compound.getString("SoulOwnerName"));
        }
    }

    public static LostSoulEntity spawn(EntityPlayer player) {
        World level = player.world;
        if (!level.isRemote) {
            final UUID owner = player.getUniqueID();
            List<LostSoulEntity> olds = level.getEntitiesWithinAABB(LostSoulEntity.class,
                    player.getEntityBoundingBox().grow(100.0D),
                    new Predicate<LostSoulEntity>() {
                        @Override
                        public boolean apply(@Nullable LostSoulEntity soul) {
                            return soul != null && owner.equals(soul.getPlayerUuid());
                        }
                    });
            for (LostSoulEntity old : olds) {
                old.setDead();
            }

            LostSoulEntity soul = new LostSoulEntity(level);
            soul.setLocationAndAngles(player.posX, player.posY + 0.6D, player.posZ, player.rotationYaw, player.rotationPitch);
            soul.setPlayerUuid(player.getUniqueID());
            soul.setPlayerName(player.getName());
            soul.enablePersistence();
            level.spawnEntity(soul);
            return soul;
        }
        return null;
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (stack.getItem() instanceof SpiritualVesselItem && !SpiritualVesselItem.isBound(stack) && this.playerUuid != null) {
            if (!player.world.isRemote) {
                SpiritualVesselItem.bindSoul(stack, this.getPlayerUuid(), this.getPlayerName());
                player.world.playSound(null, this.getPosition(), SoundEvents.ENTITY_VEX_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                player.sendMessage(new TextComponentTranslation("message.rpgcore.soul_attracted", this.getPlayerName()));
                this.setDead();
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        Entity attacker = source.getTrueSource();
        boolean creativeAttacker = attacker instanceof EntityPlayer && ((EntityPlayer) attacker).capabilities.isCreativeMode;
        return !creativeAttacker;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }
}
