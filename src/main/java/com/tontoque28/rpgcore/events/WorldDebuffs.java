package com.tontoque28.rpgcore.events;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;

public final class WorldDebuffs {

    private static final String WORLD_DEBUFF_INDICES = "debuff_indices";
    private static final String WORLD_ALL_DEAD_HANDLED = "all_dead_handled";
    private static final String WORLD_LOST = "world_lost";

    public static final class WorldDebuff {
        public final String english;
        public final String spanish;

        WorldDebuff(String english, String spanish) {
            this.english = english;
            this.spanish = spanish;
        }
    }

    public static final WorldDebuff[] WORLD_DEBUFFS = {
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

    public static final class State extends SavedData {
        public final List<Integer> debuffIndices = new ArrayList<>();
        public boolean allDeadHandled;
        public boolean worldLost;

        public static final Codec<State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.listOf().optionalFieldOf(WORLD_DEBUFF_INDICES, List.of()).forGetter(state -> state.debuffIndices),
                Codec.BOOL.optionalFieldOf(WORLD_ALL_DEAD_HANDLED, false).forGetter(state -> state.allDeadHandled),
                Codec.BOOL.optionalFieldOf(WORLD_LOST, false).forGetter(state -> state.worldLost)
        ).apply(instance, State::new));

        public static final SavedDataType<State> TYPE = new SavedDataType<>(
                Identifier.fromNamespaceAndPath("rpgcore", "world_debuff"),
                State::new,
                CODEC,
                DataFixTypes.SAVED_DATA_WORLD_CLOCKS
        );

        public State() {
        }

        private State(List<Integer> indices, boolean allDeadHandled, boolean worldLost) {
            for (int index : indices) {
                if (index >= 0 && index < WORLD_DEBUFFS.length && !this.debuffIndices.contains(index)) {
                    this.debuffIndices.add(index);
                }
            }
            this.allDeadHandled = allDeadHandled;
            this.worldLost = worldLost;
        }

        public boolean hasDebuff(int index) {
            return debuffIndices.contains(index);
        }
    }

    private WorldDebuffs() {
    }

    public static State getState(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(State.TYPE);
    }

    public static Holder<MobEffect> getEffect(int debuffIndex) {
        return switch (debuffIndex) {
            case 0, 4, 7, 10, 23 -> MobEffects.DARKNESS;
            case 1, 5, 14, 3, 9, 12, 18, 21 -> MobEffects.WEAKNESS;
            case 2, 8, 20, 22 -> MobEffects.HUNGER;
            case 6, 11, 15, 16, 17, 19 -> MobEffects.SLOWNESS;
            case 13 -> MobEffects.MINING_FATIGUE;
            default -> null;
        };
    }

    public static String effectName(Holder<MobEffect> effect, boolean spanish) {
        if (effect == MobEffects.DARKNESS) return spanish ? "Oscuridad" : "Darkness";
        if (effect == MobEffects.WEAKNESS) return spanish ? "Debilidad" : "Weakness";
        if (effect == MobEffects.HUNGER) return spanish ? "Hambre" : "Hunger";
        if (effect == MobEffects.SLOWNESS) return spanish ? "Lentitud" : "Slowness";
        if (effect == MobEffects.MINING_FATIGUE) return spanish ? "Fatiga minera" : "Mining Fatigue";
        return spanish ? "Efecto desconocido" : "Unknown effect";
    }
}
