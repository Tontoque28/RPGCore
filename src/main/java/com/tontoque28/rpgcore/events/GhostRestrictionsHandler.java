package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "rpgcore")
public class GhostRestrictionsHandler {

    private static final String TAG_GHOST_ANCHOR_X = "rpgcore_ghost_anchor_x";
    private static final String TAG_GHOST_ANCHOR_Y = "rpgcore_ghost_anchor_y";
    private static final String TAG_GHOST_ANCHOR_Z = "rpgcore_ghost_anchor_z";
    private static final String TAG_GHOST_STATE = "rpgcore_is_ghost";
    private static final String WORLD_DEBUFF_DATA = "rpgcore_world_debuff";
    private static final String WORLD_DEBUFF_INDEX = "debuff_index";
    private static final String WORLD_DEBUFF_INDICES = "debuff_indices";
    private static final String WORLD_ALL_DEAD_HANDLED = "all_dead_handled";
    private static final String WORLD_LOST = "world_lost";

    private static final class WorldDebuff {
        final String english;
        final String spanish;

        WorldDebuff(String english, String spanish) {
            this.english = english;
            this.spanish = spanish;
        }
    }

    private static final WorldDebuff[] WORLD_DEBUFFS = {
            new WorldDebuff("Shadow Saturation — The night lasts longer and the world feels heavier.", "Saturación de sombra — La noche dura más y el mundo pesa más."),
            new WorldDebuff("Soul Hunger — Hostile mobs track you more aggressively and appear more often.", "Hambre del alma — Los mobs hostiles te siguen con más agresividad y aparecen con más frecuencia."),
            new WorldDebuff("Fading Vitality — Food restores less hunger and stamina drains faster.", "Vitalidad menguante — La comida restaura menos hambre y la resistencia se agota antes."),
            new WorldDebuff("Ashen Silence — Ambient sounds fade and the world feels eerier.", "Silencio ceniciento — Los sonidos del entorno desaparecen y el mundo se siente más tétrico."),
            new WorldDebuff("Broken Dawn — Sunrise is delayed and the sky looks sickly.", "Amanecer roto — El alba se retrasa y el cielo parece enfermo."),
            new WorldDebuff("Grim Echo — Every step sounds louder and the land feels cursed.", "Eco siniestro — Cada paso suena más fuerte y la tierra parece maldita."),
            new WorldDebuff("Worn Spirit — All players move slower and feel heavier.", "Espíritu desgastado — Todos los jugadores se mueven más lento y se sienten más pesados."),
            new WorldDebuff("Bleeding Horizon — The moonlight drains your patience and your vision feels dim.", "Horizonte sangrante — La luz lunar drena tu paciencia y la vista se vuelve tenue."),
            new WorldDebuff("Underworld Thirst — Water feels wrong and every sip leaves a bitter taste.", "Sed del inframundo — El agua sabe extraño y cada sorbo deja un gusto amargo."),
            new WorldDebuff("Rotting Bloom — Flowers wither faster and the air smells sour.", "Flor podrida — Las flores se marchitan antes y el aire huele agrio."),
            new WorldDebuff("Dreaded Fog — The fog is denser and visibility drops sharply.", "Niebla aterradora — La niebla es más densa y la visibilidad cae mucho."),
            new WorldDebuff("Cold Betrayal — The wind cuts through armor and your breath becomes visible.", "Traición helada — El viento atraviesa la armadura y tu aliento se ve."),
            new WorldDebuff("Witching Mist — Strange whispers follow the player at night.", "Niebla bruja — Susurros extraños siguen al jugador por la noche."),
            new WorldDebuff("Iron Fatigue — Tools feel heavier and mining gets slower.", "Fatiga de hierro — Las herramientas pesan más y la minería va más lenta."),
            new WorldDebuff("Cracked Resolve — Even small wounds feel worse after a fight.", "Resolución agrietada — Incluso las heridas pequeñas duelen más después del combate."),
            new WorldDebuff("Mourning Tides — Water rises faster and shores feel restless.", "Mareas de luto — El agua sube más rápido y las costas se sienten inquietas."),
            new WorldDebuff("Lunar Sickness — Your eye feels heavy and the screen becomes dimmer.", "Enfermedad lunar — El ojo pesa más y la pantalla se vuelve más oscura."),
            new WorldDebuff("Twisted Thorns — Trees drag at your clothes and movement feels clumsy.", "Espinas retorcidas — Los árboles te arrastran la ropa y te mueves torpemente."),
            new WorldDebuff("Sable Veins — The ground shakes at random and memories feel distant.", "Venas de sombra — El suelo tiembla a lo loco y los recuerdos se sienten lejanos."),
            new WorldDebuff("Fell Weight — Even light armor feels too heavy.", "Peso maldito — Incluso la armadura ligera pesa demasiado."),
            new WorldDebuff("Famine of the Hollow — Animals avoid the area and food grows scarce.", "Hambre del hueco — Los animales evitan la zona y la comida escasea."),
            new WorldDebuff("Veil of Doubt — Every path looks wrong and your aim gets unstable.", "Velo de duda — Cada camino parece incorrecto y la puntería se vuelve inestable."),
            new WorldDebuff("Sunless Hunger — The sun feels distant and hunger comes sooner.", "Hambre sin sol — El sol se siente distante y el hambre llega antes."),
            new WorldDebuff("Torn Oath — The sky refuses to feel safe and all fires burn dim.", "Juramento roto — El cielo no se siente seguro y todos los fuegos arden tenues.")
    };

    private static final class WorldDebuffState extends WorldSavedData {
        private final List<Integer> debuffIndices = new ArrayList<>();
        private boolean allDeadHandled;
        private boolean worldLost;

        WorldDebuffState() {
            super(WORLD_DEBUFF_DATA);
        }

        @Override
        public void load(CompoundNBT tag) {
            this.debuffIndices.clear();
            if (tag.contains(WORLD_DEBUFF_INDICES)) {
                for (int index : tag.getIntArray(WORLD_DEBUFF_INDICES)) {
                    if (index >= 0 && index < WORLD_DEBUFFS.length && !debuffIndices.contains(index)) {
                        debuffIndices.add(index);
                    }
                }
            } else if (tag.contains(WORLD_DEBUFF_INDEX)) {
                int legacyIndex = tag.getInt(WORLD_DEBUFF_INDEX);
                if (legacyIndex >= 0 && legacyIndex < WORLD_DEBUFFS.length) {
                    debuffIndices.add(legacyIndex);
                }
            }
            this.allDeadHandled = tag.getBoolean(WORLD_ALL_DEAD_HANDLED);
            this.worldLost = tag.getBoolean(WORLD_LOST);
        }

        @Override
        public CompoundNBT save(CompoundNBT tag) {
            int[] indices = new int[debuffIndices.size()];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = debuffIndices.get(i);
            }
            tag.putIntArray(WORLD_DEBUFF_INDICES, indices);
            tag.putBoolean(WORLD_ALL_DEAD_HANDLED, allDeadHandled);
            tag.putBoolean(WORLD_LOST, worldLost);
            return tag;
        }

        private boolean hasDebuff(int index) {
            return debuffIndices.contains(index);
        }
    }

    private static WorldDebuffState getWorldDebuffState(ServerWorld level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                WorldDebuffState::new,
                WORLD_DEBUFF_DATA
        );
    }

    public static boolean isGhost(PlayerEntity player) {
        boolean capabilityGhost = player.getCapability(PlayerLivesProvider.PLAYER_LIVES)
                .map(lives -> lives.getLives() <= 0)
                .orElse(false);
        return capabilityGhost || player.getPersistentData().getBoolean(TAG_GHOST_STATE);
    }

    public static void applyGhostAbilities(ServerPlayerEntity player) {
        player.setGameMode(GameType.SURVIVAL);
        player.setInvisible(true);
        player.setInvulnerable(true);
        player.setNoGravity(true);
        player.noPhysics = true;
        player.setBoundingBox(new AxisAlignedBB(player.getX() - 0.2D, player.getY(), player.getZ() - 0.2D,
                player.getX() + 0.2D, player.getY() + 1.8D, player.getZ() + 0.2D));
        player.setSilent(true);

        player.abilities.mayfly = true;
        player.abilities.flying = true;
        player.abilities.invulnerable = true;
        player.abilities.mayBuild = false;
        player.onUpdateAbilities();

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(lives -> lives.setLives(0));
        player.getPersistentData().putBoolean(TAG_GHOST_STATE, true);
        player.setHealth(1.0F);

        player.getPersistentData().putDouble(TAG_GHOST_ANCHOR_X, player.getX());
        player.getPersistentData().putDouble(TAG_GHOST_ANCHOR_Y, player.getY());
        player.getPersistentData().putDouble(TAG_GHOST_ANCHOR_Z, player.getZ());

        clearGhostTargets(player.getLevel(), player);
    }

    public static void removeGhostAbilities(ServerPlayerEntity player) {
        player.setGameMode(GameType.SURVIVAL);
        player.setInvisible(false);
        player.setInvulnerable(false);
        player.setNoGravity(false);
        player.noPhysics = false;
        player.setSilent(false);

        player.abilities.mayfly = false;
        player.abilities.flying = false;
        player.abilities.invulnerable = false;
        player.abilities.mayBuild = true;
        player.onUpdateAbilities();
        player.getPersistentData().putBoolean(TAG_GHOST_STATE, false);

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(lives -> {
            if (lives.getLives() <= 0) {
                lives.setLives(1);
            }
        });
        GhostDeathHandler.syncMaxHealthServer(player);
        player.setHealth(player.getMaxHealth());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) event.player;

        if (player.level instanceof ServerWorld) {
            ServerWorld level = (ServerWorld) player.level;
            WorldDebuffState state = getWorldDebuffState(level);
            if (state.worldLost) {
                if (!player.isSpectator()) {
                    player.setGameMode(GameType.SPECTATOR);
                }
                return;
            }

            applyWorldDebuff(level, player);

            if (countLivingPlayers(level) > 0 && state.allDeadHandled) {
                state.allDeadHandled = false;
                state.setDirty();
            }
        }

        if (isGhost(player)) {
            player.noPhysics = true;
            player.setInvulnerable(true);

            if (!player.abilities.mayfly || !player.abilities.flying) {
                player.abilities.mayfly = true;
                player.abilities.flying = true;
                player.onUpdateAbilities();
            }

            player.setBoundingBox(new AxisAlignedBB(player.getX() - 0.08D, player.getY(), player.getZ() - 0.08D,
                    player.getX() + 0.08D, player.getY() + 0.25D, player.getZ() + 0.08D));

            clearGhostTargets(player.getLevel(), player);

            if (player.level instanceof ServerWorld) {
                ServerWorld level = (ServerWorld) player.level;
                if (level.getGameTime() % 40L == 0L) {
                    level.sendParticles(
                            ParticleTypes.SOUL,
                            player.getX(),
                            player.getY() + 1.1D,
                            player.getZ(),
                            2, 0.2D, 0.2D, 0.2D, 0.02D
                    );
                }

                if (countLivingPlayers(level) == 0) {
                    triggerWorldDebuff(level);
                }
            }
        } else {
            player.noPhysics = false;
            player.setSilent(false);
        }
    }

    private static void applyWorldDebuff(ServerWorld level, ServerPlayerEntity player) {
        WorldDebuffState state = getWorldDebuffState(level);
        if (state.debuffIndices.isEmpty() || isGhost(player)) {
            return;
        }

        for (int debuffIndex : state.debuffIndices) {
            Effect effect = getWorldDebuffEffect(debuffIndex);
            if (effect != null) {
                player.addEffect(new EffectInstance(effect, 80, 0, true, false));
            }
        }
    }

    public static void clearGhostTargets(ServerWorld level, PlayerEntity ghostPlayer) {
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, ghostPlayer.getBoundingBox().inflate(32.0D))) {
            if (living == ghostPlayer) {
                continue;
            }

            if (living instanceof MobEntity && ((MobEntity) living).getTarget() == ghostPlayer) {
                ((MobEntity) living).setTarget(null);
            }
        }
    }


    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (isGhost(event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (isGhost(event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isGhost(event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (isGhost(event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (isGhost(event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (isGhost(event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onGhostDamaged(LivingAttackEvent event) {
        if (event.getEntity() instanceof PlayerEntity && isGhost((PlayerEntity) event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onGhostHurt(LivingDamageEvent event) {
        if (event.getEntity() instanceof PlayerEntity && isGhost((PlayerEntity) event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onMobTargetGhost(LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof PlayerEntity && isGhost((PlayerEntity) event.getTarget())
                && event.getEntityLiving() instanceof MobEntity) {
            ((MobEntity) event.getEntityLiving()).setTarget(null);
        }
    }

    public static int countLivingPlayers(ServerWorld level) {
        int count = 0;
        for (ServerPlayerEntity serverPlayer : level.getServer().getPlayerList().getPlayers()) {
            if (!isGhost(serverPlayer)) {
                count++;
            }
        }
        return count;
    }

    private static boolean isSpanish(ServerPlayerEntity player) {
        String language = player.getLanguage();
        return language != null && language.toLowerCase(Locale.ROOT).startsWith("es");
    }

    private static void tell(ServerPlayerEntity player, ITextComponent component) {
        player.sendMessage(component, Util.NIL_UUID);
    }

    public static void triggerWorldDebuff(ServerWorld level) {
        WorldDebuffState state = getWorldDebuffState(level);
        if (state.worldLost || state.allDeadHandled) {
            return;
        }

        state.allDeadHandled = true;
        List<Integer> available = new ArrayList<>();
        for (int index = 0; index < WORLD_DEBUFFS.length; index++) {
            if (!state.hasDebuff(index)) {
                available.add(index);
            }
        }

        if (!available.isEmpty()) {
            int selectedIndex = available.get(new Random().nextInt(available.size()));
            state.debuffIndices.add(selectedIndex);
        }
        state.setDirty();

        for (ServerPlayerEntity serverPlayer : level.getServer().getPlayerList().getPlayers()) {
            boolean spanish = isSpanish(serverPlayer);
            tell(serverPlayer, new StringTextComponent(spanish
                    ? "[MUNDO] Todos murieron. Elige una opcion:"
                    : "[WORLD] Everyone died. Choose an option:").withStyle(TextFormatting.DARK_RED, TextFormatting.BOLD));
            tell(serverPlayer, choiceButton(spanish ? "[REVIVIR A TODOS]" : "[REVIVE ALL]", "/rpgcore revive_all", TextFormatting.GREEN));
            tell(serverPlayer, choiceButton(spanish ? "[PERDER EL MUNDO]" : "[LOSE WORLD]", "/rpgcore lose_world", TextFormatting.DARK_RED));
        }
    }

    public static ITextComponent getActiveWorldDebuffMessage(ServerPlayerEntity player) {
        WorldDebuffState state = getWorldDebuffState(player.getLevel());
        boolean spanish = isSpanish(player);
        if (state.debuffIndices.isEmpty()) {
            return new StringTextComponent(spanish ? "[RPGCore] No hay debuffs mundiales activos." : "[RPGCore] No active world debuffs.");
        }

        return new StringTextComponent(spanish ? "[MUNDO] Debuffs activos" : "[WORLD] Active debuffs")
                .withStyle(TextFormatting.GOLD, TextFormatting.BOLD);
    }

    public static List<ITextComponent> getActiveWorldDebuffTable(ServerPlayerEntity player) {
        WorldDebuffState state = getWorldDebuffState(player.getLevel());
        boolean spanish = isSpanish(player);
        List<ITextComponent> rows = new ArrayList<>();
        rows.add(new StringTextComponent(spanish ? "┌ Debuffs mundiales ┐" : "┌ Active world debuffs ┐")
                .withStyle(TextFormatting.GOLD, TextFormatting.BOLD));
        if (state.debuffIndices.isEmpty()) {
            rows.add(new StringTextComponent(spanish ? "└ Ninguno activo ┘" : "└ None active ┘").withStyle(TextFormatting.GRAY));
            return rows;
        }
        for (int i = 0; i < state.debuffIndices.size(); i++) {
            WorldDebuff debuff = WORLD_DEBUFFS[state.debuffIndices.get(i)];
            String description = spanish ? debuff.spanish : debuff.english;
            String name = description.split(" — ", 2)[0];
            String effect = effectName(getWorldDebuffEffect(state.debuffIndices.get(i)), spanish);
            rows.add(new StringTextComponent("│ " + (i + 1) + ". " + name + " -> " + effect + " │")
                    .withStyle(TextFormatting.RED));
        }
        rows.add(new StringTextComponent("└ " + (spanish ? "Persistentes" : "Persistent") + " ┘")
                .withStyle(TextFormatting.DARK_GRAY));
        return rows;
    }

    public static void clearWorldDebuff(ServerWorld level) {
        WorldDebuffState state = getWorldDebuffState(level);
        if (!state.debuffIndices.isEmpty()) {
            for (int debuffIndex : state.debuffIndices) {
                Effect effect = getWorldDebuffEffect(debuffIndex);
                if (effect != null) {
                    for (ServerPlayerEntity player : level.getServer().getPlayerList().getPlayers()) {
                        player.removeEffect(effect);
                    }
                }
            }
            state.debuffIndices.clear();
            state.setDirty();
        }
    }

    public static void reviveAll(ServerWorld level) {
        WorldDebuffState state = getWorldDebuffState(level);
        if (countLivingPlayers(level) > 0 || state.worldLost) {
            return;
        }
        for (ServerPlayerEntity player : level.getServer().getPlayerList().getPlayers()) {
            if (isGhost(player)) {
                removeGhostAbilities(player);
                level.getServer().getPlayerList().broadcastMessage(
                        new StringTextComponent(player.getName().getString() + " ha sido revivido")
                                .withStyle(TextFormatting.GOLD, TextFormatting.BOLD), ChatType.SYSTEM, Util.NIL_UUID);
                for (ServerPlayerEntity listener : level.getServer().getPlayerList().getPlayers()) {
                    listener.playNotifySound(SoundEvents.TOTEM_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
        state.allDeadHandled = false;
        state.setDirty();
    }

    public static void loseWorld(ServerWorld level) {
        WorldDebuffState state = getWorldDebuffState(level);
        if (state.worldLost) {
            return;
        }
        state.worldLost = true;
        state.setDirty();
        for (ServerPlayerEntity player : level.getServer().getPlayerList().getPlayers()) {
            player.setGameMode(GameType.SPECTATOR);
        }
        level.getServer().getPlayerList().broadcastMessage(
                new StringTextComponent("[RPGCore] The world has been lost. This save is now locked in hardcore mode."), ChatType.SYSTEM, Util.NIL_UUID);
    }

    private static ITextComponent choiceButton(String label, String command, TextFormatting color) {
        return new StringTextComponent(label).withStyle(style -> style
                .withColor(color)
                .withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click to choose"))));
    }

    private static String effectName(Effect effect, boolean spanish) {
        if (effect == Effects.BLINDNESS) return spanish ? "Ceguera" : "Blindness";
        if (effect == Effects.WEAKNESS) return spanish ? "Debilidad" : "Weakness";
        if (effect == Effects.HUNGER) return spanish ? "Hambre" : "Hunger";
        if (effect == Effects.MOVEMENT_SLOWDOWN) return spanish ? "Lentitud" : "Slowness";
        if (effect == Effects.DIG_SLOWDOWN) return spanish ? "Fatiga minera" : "Mining Fatigue";
        return spanish ? "Efecto desconocido" : "Unknown effect";
    }

    private static Effect getWorldDebuffEffect(int debuffIndex) {
        switch (debuffIndex) {
            case 0: case 4: case 7: case 10: case 23:
                return Effects.BLINDNESS;
            case 1: case 5: case 14: case 3: case 9: case 12: case 18: case 21:
                return Effects.WEAKNESS;
            case 2: case 8: case 20: case 22:
                return Effects.HUNGER;
            case 6: case 11: case 15: case 16: case 17: case 19:
                return Effects.MOVEMENT_SLOWDOWN;
            case 13:
                return Effects.DIG_SLOWDOWN;
            default:
                return null;
        }
    }
}
