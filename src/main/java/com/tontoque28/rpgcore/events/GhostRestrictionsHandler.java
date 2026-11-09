package com.tontoque28.rpgcore.events;

import com.tontoque28.rpgcore.capability.PlayerLivesProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;

import java.util.Locale;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class GhostRestrictionsHandler {

    public static void register() {
        TickEvent.PlayerTickEvent.Post.BUS.addListener(GhostRestrictionsHandler::onPlayerTick);
        EntityItemPickupEvent.BUS.addListener(GhostRestrictionsHandler::onItemPickup);
        AttackEntityEvent.BUS.addListener(GhostRestrictionsHandler::onAttackEntity);
        PlayerInteractEvent.RightClickBlock.BUS.addListener(GhostRestrictionsHandler::onRightClickBlock);
        PlayerInteractEvent.RightClickItem.BUS.addListener(GhostRestrictionsHandler::onRightClickItem);
        PlayerInteractEvent.LeftClickBlock.BUS.addListener(GhostRestrictionsHandler::onLeftClickBlock);
        BlockEvent.BreakEvent.BUS.addListener(GhostRestrictionsHandler::onBlockBreak);
        LivingAttackEvent.BUS.addListener(GhostRestrictionsHandler::onGhostDamaged);
        LivingDamageEvent.BUS.addListener(GhostRestrictionsHandler::onGhostHurt);
        LivingChangeTargetEvent.BUS.addListener(GhostRestrictionsHandler::onMobTargetGhost);
    }


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

    private static final class WorldDebuffState extends SavedData {
        private final List<Integer> debuffIndices = new ArrayList<>();
        private boolean allDeadHandled;
        private boolean worldLost;

        static final Codec<WorldDebuffState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.listOf().optionalFieldOf(WORLD_DEBUFF_INDICES, List.of()).forGetter(state -> state.debuffIndices),
                Codec.INT.optionalFieldOf(WORLD_DEBUFF_INDEX).forGetter(state -> Optional.<Integer>empty()),
                Codec.BOOL.optionalFieldOf(WORLD_ALL_DEAD_HANDLED, false).forGetter(state -> state.allDeadHandled),
                Codec.BOOL.optionalFieldOf(WORLD_LOST, false).forGetter(state -> state.worldLost)
        ).apply(instance, WorldDebuffState::fromCodec));

        static final SavedDataType<WorldDebuffState> TYPE = new SavedDataType<>(
                Identifier.fromNamespaceAndPath("rpgcore", "world_debuff"),
                WorldDebuffState::new,
                CODEC,
                DataFixTypes.SAVED_DATA_COMMAND_STORAGE
        );

        static WorldDebuffState fromCodec(List<Integer> indices, Optional<Integer> legacyIndex, boolean allDeadHandled, boolean worldLost) {
            WorldDebuffState state = new WorldDebuffState();
            if (!indices.isEmpty()) {
                for (int index : indices) {
                    if (index >= 0 && index < WORLD_DEBUFFS.length && !state.debuffIndices.contains(index)) {
                        state.debuffIndices.add(index);
                    }
                }
            } else if (legacyIndex.isPresent()) {
                int legacy = legacyIndex.get();
                if (legacy >= 0 && legacy < WORLD_DEBUFFS.length) {
                    state.debuffIndices.add(legacy);
                }
            }
            state.allDeadHandled = allDeadHandled;
            state.worldLost = worldLost;
            return state;
        }

        private boolean hasDebuff(int index) {
            return debuffIndices.contains(index);
        }
    }

    private static WorldDebuffState getWorldDebuffState(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(WorldDebuffState.TYPE);
    }

    private static boolean isSpanish(ServerPlayer player) {
        String language = player.clientInformation().language();
        return language != null && language.toLowerCase(Locale.ROOT).startsWith("es");
    }

    public static boolean isGhost(Player player) {
        boolean capabilityGhost = player.getCapability(PlayerLivesProvider.PLAYER_LIVES)
                .map(lives -> lives.getLives() <= 0)
                .orElse(false);
        return capabilityGhost || player.getPersistentData().getBooleanOr(TAG_GHOST_STATE, false);
    }

    public static void applyGhostAbilities(ServerPlayer player) {
        player.setGameMode(GameType.SURVIVAL);
        player.setInvisible(true);
        player.setPermanentlyInvulnerable(true);
        player.setNoGravity(true);
        player.noPhysics = true;
        player.setBoundingBox(new AABB(player.getX() - 0.2D, player.getY(), player.getZ() - 0.2D,
                player.getX() + 0.2D, player.getY() + 1.8D, player.getZ() + 0.2D));
        player.setSilent(true);

        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.getAbilities().invulnerable = true;
        player.getAbilities().mayBuild = false;
        player.onUpdateAbilities();

        player.getCapability(PlayerLivesProvider.PLAYER_LIVES).ifPresent(lives -> lives.setLives(0));
        player.getPersistentData().putBoolean(TAG_GHOST_STATE, true);
        player.setHealth(1.0F);

        player.getPersistentData().putDouble(TAG_GHOST_ANCHOR_X, player.getX());
        player.getPersistentData().putDouble(TAG_GHOST_ANCHOR_Y, player.getY());
        player.getPersistentData().putDouble(TAG_GHOST_ANCHOR_Z, player.getZ());

        clearGhostTargets(player.level(), player);
    }

    public static void removeGhostAbilities(ServerPlayer player) {
        player.setGameMode(GameType.SURVIVAL);
        player.setInvisible(false);
        player.setPermanentlyInvulnerable(false);
        player.setNoGravity(false);
        player.noPhysics = false;
        player.setSilent(false);

        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.getAbilities().invulnerable = false;
        player.getAbilities().mayBuild = true;
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

    public static void onPlayerTick(TickEvent.PlayerTickEvent.Post event) {
        if (!(event.player() instanceof ServerPlayer player)) {
            return;
        }

        if (player.level() instanceof ServerLevel level) {
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
            player.setPermanentlyInvulnerable(true);

            if (!player.getAbilities().mayfly || !player.getAbilities().flying) {
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.onUpdateAbilities();
            }

            player.setBoundingBox(new AABB(player.getX() - 0.08D, player.getY(), player.getZ() - 0.08D,
                    player.getX() + 0.08D, player.getY() + 0.25D, player.getZ() + 0.08D));

            clearGhostTargets(player.level(), player);

            if (player.level() instanceof ServerLevel level) {
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

    private static void applyWorldDebuff(ServerLevel level, ServerPlayer player) {
        WorldDebuffState state = getWorldDebuffState(level);
        if (state.debuffIndices.isEmpty() || isGhost(player)) {
            return;
        }

        for (int debuffIndex : state.debuffIndices) {
            Holder<MobEffect> effect = getWorldDebuffEffect(debuffIndex);
            if (effect != null) {
                player.addEffect(new MobEffectInstance(effect, 80, 0, true, false));
            }
        }
    }

    public static void clearGhostTargets(ServerLevel level, Player ghostPlayer) {
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, ghostPlayer.getBoundingBox().inflate(32.0D))) {
            if (living == ghostPlayer) {
                continue;
            }

            if (living instanceof Mob mob && mob.getTarget() == ghostPlayer) {
                mob.setTarget(null);
            }

            if (living instanceof ServerPlayer serverPlayer && isGhost(serverPlayer) && serverPlayer != ghostPlayer) {
                continue;
            }
        }
    }


    public static boolean onItemPickup(EntityItemPickupEvent event) {
        return isGhost(event.getEntity());
    }

    public static boolean onAttackEntity(AttackEntityEvent event) {
        return isGhost(event.getEntity());
    }

    public static boolean onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        return isGhost(event.getEntity());
    }

    public static boolean onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        return isGhost(event.getEntity());
    }

    public static boolean onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        return isGhost(event.getEntity());
    }

    public static boolean onBlockBreak(BlockEvent.BreakEvent event) {
        return isGhost(event.getPlayer());
    }

    public static boolean onGhostDamaged(LivingAttackEvent event) {
        return event.getEntity() instanceof Player player && isGhost(player);
    }

    public static boolean onGhostHurt(LivingDamageEvent event) {
        return event.getEntity() instanceof Player player && isGhost(player);
    }

    public static boolean onMobTargetGhost(LivingChangeTargetEvent event) {
        return event.getNewTarget() instanceof Player player && isGhost(player);
    }

    public static int countLivingPlayers(ServerLevel level) {
        int count = 0;
        for (ServerPlayer serverPlayer : level.getServer().getPlayerList().getPlayers()) {
            if (!isGhost(serverPlayer)) {
                count++;
            }
        }
        return count;
    }

    public static void triggerWorldDebuff(ServerLevel level) {
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

        for (ServerPlayer serverPlayer : level.getServer().getPlayerList().getPlayers()) {
            boolean spanish = isSpanish(serverPlayer);
            serverPlayer.sendSystemMessage(Component.literal(spanish
                    ? "[MUNDO] Todos murieron. Elige una opcion:"
                    : "[WORLD] Everyone died. Choose an option:").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            serverPlayer.sendSystemMessage(choiceButton(spanish ? "[REVIVIR A TODOS]" : "[REVIVE ALL]", "/rpgcore revive_all", ChatFormatting.GREEN));
            serverPlayer.sendSystemMessage(choiceButton(spanish ? "[PERDER EL MUNDO]" : "[LOSE WORLD]", "/rpgcore lose_world", ChatFormatting.DARK_RED));
        }
    }

    public static Component getActiveWorldDebuffMessage(ServerPlayer player) {
        WorldDebuffState state = getWorldDebuffState(player.level());
        boolean spanish = isSpanish(player);
        if (state.debuffIndices.isEmpty()) {
            return Component.literal(spanish ? "[RPGCore] No hay debuffs mundiales activos." : "[RPGCore] No active world debuffs.");
        }

        return Component.literal(spanish ? "[MUNDO] Debuffs activos" : "[WORLD] Active debuffs")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    }

    public static List<Component> getActiveWorldDebuffTable(ServerPlayer player) {
        WorldDebuffState state = getWorldDebuffState(player.level());
        boolean spanish = isSpanish(player);
        List<Component> rows = new ArrayList<>();
        rows.add(Component.literal(spanish ? "┌ Debuffs mundiales ┐" : "┌ Active world debuffs ┐")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        if (state.debuffIndices.isEmpty()) {
            rows.add(Component.literal(spanish ? "└ Ninguno activo ┘" : "└ None active ┘").withStyle(ChatFormatting.GRAY));
            return rows;
        }
        for (int i = 0; i < state.debuffIndices.size(); i++) {
            WorldDebuff debuff = WORLD_DEBUFFS[state.debuffIndices.get(i)];
            String description = spanish ? debuff.spanish : debuff.english;
            String name = description.split(" — ", 2)[0];
            String effect = effectName(getWorldDebuffEffect(state.debuffIndices.get(i)), spanish);
            rows.add(Component.literal("│ " + (i + 1) + ". " + name + " -> " + effect + " │")
                    .withStyle(ChatFormatting.RED));
        }
        rows.add(Component.literal("└ " + (spanish ? "Persistentes" : "Persistent") + " ┘")
                .withStyle(ChatFormatting.DARK_GRAY));
        return rows;
    }

    public static void clearWorldDebuff(ServerLevel level) {
        WorldDebuffState state = getWorldDebuffState(level);
        if (!state.debuffIndices.isEmpty()) {
            for (int debuffIndex : state.debuffIndices) {
                Holder<MobEffect> effect = getWorldDebuffEffect(debuffIndex);
                if (effect != null) {
                    for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                        player.removeEffect(effect);
                    }
                }
            }
            state.debuffIndices.clear();
            state.setDirty();
        }
    }

    public static void reviveAll(ServerLevel level) {
        WorldDebuffState state = getWorldDebuffState(level);
        if (countLivingPlayers(level) > 0 || state.worldLost) {
            return;
        }
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (isGhost(player)) {
                removeGhostAbilities(player);
                level.getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal(player.getName().getString() + " ha sido revivido")
                                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
                for (ServerPlayer listener : level.getServer().getPlayerList().getPlayers()) {
                    listener.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.TOTEM_USE), SoundSource.PLAYERS, listener.getX(), listener.getY(), listener.getZ(), 1.0F, 1.0F, listener.level().getRandom().nextLong()));
                }
            }
        }
        state.allDeadHandled = false;
        state.setDirty();
    }

    public static void loseWorld(ServerLevel level) {
        WorldDebuffState state = getWorldDebuffState(level);
        if (state.worldLost) {
            return;
        }
        state.worldLost = true;
        state.setDirty();
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.setGameMode(GameType.SPECTATOR);
        }
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("[RPGCore] The world has been lost. This save is now locked in hardcore mode."), false);
    }

    private static Component choiceButton(String label, String command, ChatFormatting color) {
        return Component.literal(label).withStyle(style -> style
                .withColor(color)
                .withBold(true)
                .withClickEvent(new ClickEvent.RunCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to choose"))));
    }

    private static String effectName(Holder<MobEffect> effect, boolean spanish) {
        if (effect == MobEffects.DARKNESS) return spanish ? "Oscuridad" : "Darkness";
        if (effect == MobEffects.WEAKNESS) return spanish ? "Debilidad" : "Weakness";
        if (effect == MobEffects.HUNGER) return spanish ? "Hambre" : "Hunger";
        if (effect == MobEffects.SLOWNESS) return spanish ? "Lentitud" : "Slowness";
        if (effect == MobEffects.MINING_FATIGUE) return spanish ? "Fatiga minera" : "Mining Fatigue";
        return spanish ? "Efecto desconocido" : "Unknown effect";
    }

    private static Holder<MobEffect> getWorldDebuffEffect(int debuffIndex) {
        return switch (debuffIndex) {
            case 0, 4, 7, 10, 23 -> MobEffects.DARKNESS;
            case 1, 5, 14, 3, 9, 12, 18, 21 -> MobEffects.WEAKNESS;
            case 2, 8, 20, 22 -> MobEffects.HUNGER;
            case 6, 11, 15, 16, 17, 19 -> MobEffects.SLOWNESS;
            case 13 -> MobEffects.MINING_FATIGUE;
            default -> null;
        };
    }
}