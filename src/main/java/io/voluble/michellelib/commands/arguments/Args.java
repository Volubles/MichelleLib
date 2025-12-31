package io.voluble.michellelib.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.AxisSet;
import io.papermc.paper.command.brigadier.argument.SignedMessageResolver;
import io.papermc.paper.command.brigadier.argument.predicate.BlockInWorldPredicate;
import io.papermc.paper.command.brigadier.argument.predicate.ItemStackPredicate;
import io.papermc.paper.command.brigadier.argument.range.DoubleRangeProvider;
import io.papermc.paper.command.brigadier.argument.range.IntegerRangeProvider;
import io.papermc.paper.command.brigadier.argument.resolvers.AngleResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.ColumnBlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.ColumnFinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.RotationResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.entity.LookAnchor;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.GameMode;
import org.bukkit.HeightMap;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;

import java.time.Duration;
import java.util.UUID;

/**
 * Argument factories with sane defaults.
 *
 * <p>Purpose: reduce imports and "where do I get that argument type again?" friction.</p>
 */
public final class Args {
    private Args() {
    }

    // ---- Brigadier primitives ----

    public static ArgumentType<Boolean> bool() {
        return BoolArgumentType.bool();
    }

    public static ArgumentType<Integer> integer() {
        return IntegerArgumentType.integer();
    }

    public static ArgumentType<Integer> integer(final int min) {
        return IntegerArgumentType.integer(min);
    }

    public static ArgumentType<Integer> integer(final int min, final int max) {
        return IntegerArgumentType.integer(min, max);
    }

    public static ArgumentType<Long> longArg() {
        return LongArgumentType.longArg();
    }

    public static ArgumentType<Float> floatArg() {
        return FloatArgumentType.floatArg();
    }

    public static ArgumentType<Float> floatArg(final float min, final float max) {
        return FloatArgumentType.floatArg(min, max);
    }

    public static ArgumentType<Double> doubleArg() {
        return DoubleArgumentType.doubleArg();
    }

    public static ArgumentType<String> word() {
        return StringArgumentType.word();
    }

    public static ArgumentType<String> string() {
        return StringArgumentType.string();
    }

    public static ArgumentType<String> greedyString() {
        return StringArgumentType.greedyString();
    }

    // ---- Minecraft/Paper-native (ArgumentTypes) ----

    public static ArgumentType<PlayerSelectorArgumentResolver> player() {
        return ArgumentTypes.player();
    }

    public static ArgumentType<PlayerSelectorArgumentResolver> players() {
        return ArgumentTypes.players();
    }

    public static ArgumentType<EntitySelectorArgumentResolver> entity() {
        return ArgumentTypes.entity();
    }

    public static ArgumentType<EntitySelectorArgumentResolver> entities() {
        return ArgumentTypes.entities();
    }

    public static ArgumentType<ItemStack> itemStack() {
        return ArgumentTypes.itemStack();
    }

    public static ArgumentType<ItemStackPredicate> itemPredicate() {
        return ArgumentTypes.itemPredicate();
    }

    public static ArgumentType<BlockState> blockState() {
        return ArgumentTypes.blockState();
    }

    public static ArgumentType<BlockInWorldPredicate> blockInWorldPredicate() {
        return ArgumentTypes.blockInWorldPredicate();
    }

    public static ArgumentType<BlockPositionResolver> blockPosition() {
        return ArgumentTypes.blockPosition();
    }

    public static ArgumentType<ColumnBlockPositionResolver> columnBlockPosition() {
        return ArgumentTypes.columnBlockPosition();
    }

    public static ArgumentType<FinePositionResolver> finePosition() {
        return ArgumentTypes.finePosition();
    }

    public static ArgumentType<FinePositionResolver> finePosition(final boolean centerIntegers) {
        return ArgumentTypes.finePosition(centerIntegers);
    }

    public static ArgumentType<ColumnFinePositionResolver> columnFinePosition() {
        return ArgumentTypes.columnFinePosition();
    }

    public static ArgumentType<ColumnFinePositionResolver> columnFinePosition(final boolean centerIntegers) {
        return ArgumentTypes.columnFinePosition(centerIntegers);
    }

    public static ArgumentType<RotationResolver> rotation() {
        return ArgumentTypes.rotation();
    }

    public static ArgumentType<AngleResolver> angle() {
        return ArgumentTypes.angle();
    }

    public static ArgumentType<AxisSet> axes() {
        return ArgumentTypes.axes();
    }

    public static ArgumentType<World> world() {
        return ArgumentTypes.world();
    }

    public static ArgumentType<GameMode> gameMode() {
        return ArgumentTypes.gameMode();
    }

    public static ArgumentType<HeightMap> heightMap() {
        return ArgumentTypes.heightMap();
    }

    public static ArgumentType<NamespacedKey> namespacedKey() {
        return ArgumentTypes.namespacedKey();
    }

    public static ArgumentType<Key> key() {
        return ArgumentTypes.key();
    }

    public static ArgumentType<UUID> uuid() {
        return ArgumentTypes.uuid();
    }

    public static ArgumentType<Component> component() {
        return ArgumentTypes.component();
    }

    public static ArgumentType<NamedTextColor> namedColor() {
        return ArgumentTypes.namedColor();
    }

    public static ArgumentType<TextColor> hexColor() {
        return ArgumentTypes.hexColor();
    }

    public static ArgumentType<Style> style() {
        return ArgumentTypes.style();
    }

    public static ArgumentType<SignedMessageResolver> signedMessage() {
        return ArgumentTypes.signedMessage();
    }

    public static ArgumentType<DisplaySlot> scoreboardDisplaySlot() {
        return ArgumentTypes.scoreboardDisplaySlot();
    }

    public static ArgumentType<Criteria> objectiveCriteria() {
        return ArgumentTypes.objectiveCriteria();
    }

    public static ArgumentType<LookAnchor> entityAnchor() {
        return ArgumentTypes.entityAnchor();
    }

    public static ArgumentType<IntegerRangeProvider> integerRange() {
        return ArgumentTypes.integerRange();
    }

    public static ArgumentType<DoubleRangeProvider> doubleRange() {
        return ArgumentTypes.doubleRange();
    }

    public static ArgumentType<Integer> time() {
        return ArgumentTypes.time();
    }

    public static ArgumentType<Integer> time(final int minTime) {
        return ArgumentTypes.time(minTime);
    }

    public static ArgumentType<Mirror> templateMirror() {
        return ArgumentTypes.templateMirror();
    }

    public static ArgumentType<StructureRotation> templateRotation() {
        return ArgumentTypes.templateRotation();
    }

    public static ArgumentType<PlayerProfileListResolver> playerProfiles() {
        return ArgumentTypes.playerProfiles();
    }

    public static <T> ArgumentType<T> resource(final RegistryKey<T> registryKey) {
        return ArgumentTypes.resource(registryKey);
    }

    public static <T> ArgumentType<TypedKey<T>> resourceKey(final RegistryKey<T> registryKey) {
        return ArgumentTypes.resourceKey(registryKey);
    }

    // ---- Convenience methods for common registries ----

    /**
     * Argument type for item types.
     */
    public static ArgumentType<org.bukkit.inventory.ItemType> itemType() {
        return resource(RegistryKey.ITEM);
    }

    /**
     * Argument type for block types.
     */
    public static ArgumentType<org.bukkit.block.BlockType> blockType() {
        return resource(RegistryKey.BLOCK);
    }

    /**
     * Argument type for enchantments.
     */
    public static ArgumentType<org.bukkit.enchantments.Enchantment> enchantment() {
        return resource(RegistryKey.ENCHANTMENT);
    }

    /**
     * Argument type for potion effect types.
     */
    public static ArgumentType<org.bukkit.potion.PotionEffectType> potionEffect() {
        return resource(RegistryKey.MOB_EFFECT);
    }

    /**
     * Argument type for sound events.
     */
    public static ArgumentType<org.bukkit.Sound> sound() {
        return resource(RegistryKey.SOUND_EVENT);
    }

    /**
     * Argument type for entity types.
     */
    public static ArgumentType<org.bukkit.entity.EntityType> entityType() {
        return resource(RegistryKey.ENTITY_TYPE);
    }

    /**
     * Argument type for biomes.
     */
    public static ArgumentType<org.bukkit.block.Biome> biome() {
        return resource(RegistryKey.BIOME);
    }

    /**
     * Argument type for parsing human-readable time durations.
     *
     * <p>Supports formats like "1 second", "5 minutes", "1 hour 30 minutes", "2d 3h 15m", etc.</p>
     *
     * @return a Duration argument type
     */
    public static ArgumentType<Duration> timeDuration() {
        return TimeDurationArgument.INSTANCE;
    }
}

