package com.tontoque28.rpgcore.events;

import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;

final class WorldDebuffs {

    static final class Entry {
        final String english;
        final String spanish;

        Entry(String english, String spanish) {
            this.english = english;
            this.spanish = spanish;
        }
    }

    static final Entry[] ALL = {
            new Entry("Shadow Saturation — The night lasts longer and the world feels heavier.", "Saturación de sombra — La noche dura más y el mundo pesa más."),
            new Entry("Soul Hunger — Hostile mobs track you more aggressively and appear more often.", "Hambre del alma — Los mobs hostiles te siguen con más agresividad y aparecen con más frecuencia."),
            new Entry("Fading Vitality — Food restores less hunger and stamina drains faster.", "Vitalidad menguante — La comida restaura menos hambre y la resistencia se agota antes."),
            new Entry("Ashen Silence — Ambient sounds fade and the world feels eerier.", "Silencio ceniciento — Los sonidos del entorno desaparecen y el mundo se siente más tétrico."),
            new Entry("Broken Dawn — Sunrise is delayed and the sky looks sickly.", "Amanecer roto — El alba se retrasa y el cielo parece enfermo."),
            new Entry("Grim Echo — Every step sounds louder and the land feels cursed.", "Eco siniestro — Cada paso suena más fuerte y la tierra parece maldita."),
            new Entry("Worn Spirit — All players move slower and feel heavier.", "Espíritu desgastado — Todos los jugadores se mueven más lento y se sienten más pesados."),
            new Entry("Bleeding Horizon — The moonlight drains your patience and your vision feels dim.", "Horizonte sangrante — La luz lunar drena tu paciencia y la vista se vuelve tenue."),
            new Entry("Underworld Thirst — Water feels wrong and every sip leaves a bitter taste.", "Sed del inframundo — El agua sabe extraño y cada sorbo deja un gusto amargo."),
            new Entry("Rotting Bloom — Flowers wither faster and the air smells sour.", "Flor podrida — Las flores se marchitan antes y el aire huele agrio."),
            new Entry("Dreaded Fog — The fog is denser and visibility drops sharply.", "Niebla aterradora — La niebla es más densa y la visibilidad cae mucho."),
            new Entry("Cold Betrayal — The wind cuts through armor and your breath becomes visible.", "Traición helada — El viento atraviesa la armadura y tu aliento se ve."),
            new Entry("Witching Mist — Strange whispers follow the player at night.", "Niebla bruja — Susurros extraños siguen al jugador por la noche."),
            new Entry("Iron Fatigue — Tools feel heavier and mining gets slower.", "Fatiga de hierro — Las herramientas pesan más y la minería va más lenta."),
            new Entry("Cracked Resolve — Even small wounds feel worse after a fight.", "Resolución agrietada — Incluso las heridas pequeñas duelen más después del combate."),
            new Entry("Mourning Tides — Water rises faster and shores feel restless.", "Mareas de luto — El agua sube más rápido y las costas se sienten inquietas."),
            new Entry("Lunar Sickness — Your eye feels heavy and the screen becomes dimmer.", "Enfermedad lunar — El ojo pesa más y la pantalla se vuelve más oscura."),
            new Entry("Twisted Thorns — Trees drag at your clothes and movement feels clumsy.", "Espinas retorcidas — Los árboles te arrastran la ropa y te mueves torpemente."),
            new Entry("Sable Veins — The ground shakes at random and memories feel distant.", "Venas de sombra — El suelo tiembla a lo loco y los recuerdos se sienten lejanos."),
            new Entry("Fell Weight — Even light armor feels too heavy.", "Peso maldito — Incluso la armadura ligera pesa demasiado."),
            new Entry("Famine of the Hollow — Animals avoid the area and food grows scarce.", "Hambre del hueco — Los animales evitan la zona y la comida escasea."),
            new Entry("Veil of Doubt — Every path looks wrong and your aim gets unstable.", "Velo de duda — Cada camino parece incorrecto y la puntería se vuelve inestable."),
            new Entry("Sunless Hunger — The sun feels distant and hunger comes sooner.", "Hambre sin sol — El sol se siente distante y el hambre llega antes."),
            new Entry("Torn Oath — The sky refuses to feel safe and all fires burn dim.", "Juramento roto — El cielo no se siente seguro y todos los fuegos arden tenues.")
    };

    private WorldDebuffs() {
    }

    static Potion effectFor(int debuffIndex) {
        switch (debuffIndex) {
            case 0: case 4: case 7: case 10: case 23:
                return MobEffects.BLINDNESS;
            case 1: case 5: case 14: case 3: case 9: case 12: case 18: case 21:
                return MobEffects.WEAKNESS;
            case 2: case 8: case 20: case 22:
                return MobEffects.HUNGER;
            case 6: case 11: case 15: case 16: case 17: case 19:
                return MobEffects.SLOWNESS;
            case 13:
                return MobEffects.MINING_FATIGUE;
            default:
                return null;
        }
    }

    static String effectName(Potion effect, boolean spanish) {
        if (effect == MobEffects.BLINDNESS) return spanish ? "Ceguera" : "Blindness";
        if (effect == MobEffects.WEAKNESS) return spanish ? "Debilidad" : "Weakness";
        if (effect == MobEffects.HUNGER) return spanish ? "Hambre" : "Hunger";
        if (effect == MobEffects.SLOWNESS) return spanish ? "Lentitud" : "Slowness";
        if (effect == MobEffects.MINING_FATIGUE) return spanish ? "Fatiga minera" : "Mining Fatigue";
        return spanish ? "Efecto desconocido" : "Unknown effect";
    }
}
