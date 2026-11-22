package com.example.saitamamod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraft.util.RandomSource;


@Mod(SaitamaMod.ID)
public class SaitamaMod {
    public static final String ID = "saitamamod";
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ID);
    // Progression is stored on the player's persisted NBT, server-side.
    private static final String HERO = "OnePunchHero";
    private static final String ACTIVE = "active";
    private static final String LEVEL = "level";
    private static final String XP = "xp";
    public static final RegistryObject<Item> HERO_FRAGMENT = ITEMS.register("hero_fragment", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HERO_CORE = ITEMS.register("hero_core", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> BROKEN_LIMITER = ITEMS.register("broken_limiter", () -> new Item(new Item.Properties().stacksTo(1)) {
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide) {
                CompoundTag hero = heroData(player);
                if (hero.getBoolean(ACTIVE)) {
                    player.sendSystemMessage(Component.literal("Ya has roto tu limitador."));
                } else {
                    hero.putBoolean(ACTIVE, true);
                    hero.putInt(LEVEL, 1);
                    hero.putInt(XP, 0);
                    player.sendSystemMessage(Component.literal("¡Limitador roto! Nivel 1. Normal Punch desbloqueado."));
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
    });
    private static CompoundTag heroData(Player player) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!player.getPersistentData().contains(Player.PERSISTED_NBT_TAG))
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        CompoundTag hero = persisted.getCompound(HERO);
        if (!persisted.contains(HERO)) persisted.put(HERO, hero);
        return hero;
    }
    private static int heroLevel(Player player) { return heroData(player).getBoolean(ACTIVE) ? Math.max(1, heroData(player).getInt(LEVEL)) : 0; }
    private static int requiredXp(int level) { return 40 + level * 15; }
    private static void grantHeroXp(Player player, int amount) {
        CompoundTag hero = heroData(player);
        if (!hero.getBoolean(ACTIVE)) return;
        int level = heroLevel(player);
        int xp = hero.getInt(XP) + amount;
        while (level < 100 && xp >= requiredXp(level)) {
            xp -= requiredXp(level);
            level++;
            player.sendSystemMessage(Component.literal("¡Nivel de héroe " + level + "!"));
        }
        hero.putInt(LEVEL, level);
        hero.putInt(XP, level >= 100 ? 0 : xp);
    }
    private static int requiredLevel(Item item) {
        if (item == PUNCH.get()) return 1;
        if (item == COMBO.get()) return 10;
        if (item == SIDEWAYS.get()) return 20;
        if (item == SERIOUS.get()) return 35;
        if (item == TABLE_FLIP.get()) return 50;
        if (item == SNEEZE.get()) return 75;
        return 0;
    }
    private static boolean canUsePower(Player player, Item item) {
        if (player.getAbilities().instabuild) return true; // Creative testing remains available.
        int needed = requiredLevel(item);
        if (heroLevel(player) >= needed) return true;
        player.sendSystemMessage(Component.literal("Poder bloqueado. Nivel de héroe necesario: " + needed));
        return false;
    }
    @SubscribeEvent public void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag old = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, old.copy());
    }
    @SubscribeEvent public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || event.getEntity() == player) return;
        LivingEntity dead = event.getEntity();
        grantHeroXp(player, dead instanceof Boros ? 1500 : dead instanceof net.minecraft.world.entity.monster.Monster ? 25 : 5);
        if (dead instanceof net.minecraft.world.entity.monster.Monster && dead.getRandom().nextInt(dead instanceof Boros ? 1 : 12) == 0)
            dead.spawnAtLocation(new ItemStack(HERO_FRAGMENT.get(), dead instanceof Boros ? 8 : 1));
    }
    public static final RegistryObject<Item> HELMET = ITEMS.register("saitama_helmet", () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> CHEST = ITEMS.register("saitama_chestplate", () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> LEGS = ITEMS.register("saitama_leggings", () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> BOOTS = ITEMS.register("saitama_boots", () -> new ArmorItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, new Item.Properties()));
    public static final RegistryObject<Item> PUNCH = ITEMS.register("normal_punch", () -> new PowerItem(false));
    public static final RegistryObject<Item> SERIOUS = ITEMS.register("serious_punch", () -> new PowerItem(true));
    public static final RegistryObject<Item> COMBO = ITEMS.register("consecutive_punches", () -> new AdvancedPowerItem(0));
    public static final RegistryObject<Item> TABLE_FLIP = ITEMS.register("serious_table_flip", () -> new AdvancedPowerItem(1));
    public static final RegistryObject<Item> SNEEZE = ITEMS.register("serious_sneeze", () -> new AdvancedPowerItem(2));
    public static final RegistryObject<Item> SIDEWAYS = ITEMS.register("serious_sideways_jumps", () -> new AdvancedPowerItem(3));
    public static final RegistryObject<Item> SUMMON = ITEMS.register("boros_summoner", () -> new Item(new Item.Properties().stacksTo(16)) {
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide) {
                Boros boss = BOROS.get().create(level);
                if (boss != null) {
                    boss.moveTo(player.getX()+3, player.getY(), player.getZ()+3, player.getYRot(), 0);
                    level.addFreshEntity(boss);
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
    });
    public static final RegistryObject<EntityType<Boros>> BOROS = ENTITIES.register("boros", () -> EntityType.Builder.of(Boros::new, MobCategory.MONSTER).sized(0.8f, 2.6f).build("saitamamod:boros"));

    public SaitamaMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus); ENTITIES.register(bus);
        bus.addListener(this::attributes);
        MinecraftForge.EVENT_BUS.register(this);
    }
    private void attributes(EntityAttributeCreationEvent event) { event.put(BOROS.get(), Boros.createAttributes().build()); }
    private static boolean fullSuit(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(HELMET.get()) && player.getItemBySlot(EquipmentSlot.CHEST).is(CHEST.get()) && player.getItemBySlot(EquipmentSlot.LEGS).is(LEGS.get()) && player.getItemBySlot(EquipmentSlot.FEET).is(BOOTS.get());
    }
    @SubscribeEvent public void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END || event.player.level().isClientSide || event.player.tickCount % 20 != 0) return;
        int hero = heroLevel(event.player);
        if (hero > 0) {
            int boost = Math.min(3, (hero - 1) / 25);
            event.player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 45, boost, true, false));
            event.player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 45, Math.min(4, 1 + hero / 25), true, false));
            event.player.addEffect(new MobEffectInstance(MobEffects.JUMP, 45, Math.min(5, 1 + hero / 20), true, false));
            event.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 45, Math.min(9, 1 + hero / 12), true, false));
            event.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 45, Math.min(3, hero / 30), true, false));
        }
        if (fullSuit(event.player)) {
            event.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 50, 3, true, false));
            event.player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50, 2, true, false));
            event.player.addEffect(new MobEffectInstance(MobEffects.JUMP, 50, 3, true, false));
            event.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 50, 4, true, false));
        }
    }
    public static class PowerItem extends Item {
        private final boolean serious;
        public PowerItem(boolean serious) { super(new Item.Properties().stacksTo(1)); this.serious = serious; }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide) {
                if (!canUsePower(player, this)) return InteractionResultHolder.fail(stack);
                int cooldown = serious ? 200 : 30;
                player.getCooldowns().addCooldown(this, cooldown);
                double range = serious ? 18 : 5;
                Vec3 look = player.getLookAngle();
                Vec3 eye = player.getEyePosition();
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range), e -> e != player && e.isAlive());
                for (LivingEntity target : targets) {
                    Vec3 delta = target.getBoundingBox().getCenter().subtract(eye);
                    if (delta.length() <= range && delta.normalize().dot(look) > (serious ? 0.75 : 0.82)) {
                        target.hurt(player.damageSources().playerAttack(player), serious ? 150f : 35f);
                        target.push(look.x * (serious ? 5 : 2), serious ? 1.5 : 0.5, look.z * (serious ? 5 : 2));
                    }
                }
                if (serious && level instanceof net.minecraft.server.level.ServerLevel server
                        && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(level, player)) {
                    int destroyed = 0;
                    for (int distance = 2; distance <= 28 && destroyed < 320; distance++) {
                        BlockPos center = BlockPos.containing(eye.add(look.scale(distance)));
                        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2,-2,-2), center.offset(2,2,2))) {
                            if (destroyed >= 320) break;
                            BlockPos at = pos.immutable();
                            var state = server.getBlockState(at);
                            if (!state.isAir() && state.getDestroySpeed(server, at) >= 0 && server.getBlockEntity(at) == null
                                    && server.destroyBlock(at, false, player)) destroyed++;
                        }
                    }
                    server.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                            player.getX()+look.x*6, player.getY()+1, player.getZ()+look.z*6, 45, 3, 3, 3, .2);
                }
                player.sendSystemMessage(Component.literal(serious ? "¡SERIOUS PUNCH!" : "¡Puñetazo normal!"));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
    }
    // The destruction budget deliberately caps block edits per ability to protect servers.
    public static class AdvancedPowerItem extends Item {
        private final int power;
        public AdvancedPowerItem(int power) { super(new Item.Properties().stacksTo(1)); this.power = power; }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide && level instanceof net.minecraft.server.level.ServerLevel server) {
                if (!canUsePower(player, this)) return InteractionResultHolder.fail(stack);
                int[] cooldowns = {70, 300, 240, 100};
                player.getCooldowns().addCooldown(this, cooldowns[power]);
                Vec3 origin = player.getEyePosition(), direction = player.getLookAngle();
                int radius = power == 1 ? 9 : power == 2 ? 17 : 12;
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(38),
                        e -> e != player && e.isAlive())) {
                    Vec3 delta = target.position().subtract(origin);
                    boolean inCone = delta.length() < (power == 2 ? 36 : 24) && delta.normalize().dot(direction) > (power == 2 ? .55 : .7);
                    if (power == 1) inCone = target.distanceTo(player) < 15;
                    if (power == 3) inCone = target.distanceTo(player) < 13;
                    if (inCone) {
                        target.hurt(player.damageSources().playerAttack(player), new float[]{65, 110, 95, 70}[power]);
                        target.push(direction.x * 3.5, power == 1 ? 2.8 : .7, direction.z * 3.5);
                    }
                }
                if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(level, player)) {
                    int budget = power == 1 ? 400 : power == 2 ? 300 : 180;
                    int removed = 0;
                    BlockPos center = BlockPos.containing(player.position().add(direction.scale(power == 1 ? 5 : 9)));
                    for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -3, -radius), center.offset(radius, power == 1 ? 6 : 3, radius))) {
                        if (removed >= budget) break;
                        double dx = pos.getX() - center.getX(), dy = pos.getY() - center.getY(), dz = pos.getZ() - center.getZ();
                        if (dx*dx + dz*dz > radius*radius || (power != 1 && Math.abs(dy) > 3)) continue;
                        BlockPos at = pos.immutable();
                        var state = server.getBlockState(at);
                        if (state.isAir() || state.getDestroySpeed(server, at) < 0 || server.getBlockEntity(at) != null) continue;
                        if (server.destroyBlock(at, false, player)) removed++;
                    }
                }
                if (power == 3) player.setDeltaMovement(direction.x * 3.8, .55, direction.z * 3.8);
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, player.getX()+direction.x*4,
                        player.getY()+1, player.getZ()+direction.z*4, 30, 3, 2, 3, .15);
                player.sendSystemMessage(Component.literal(new String[]{"Consecutive Normal Punches!", "Serious Table Flip!", "Serious Sneeze!", "Serious Sideways Jumps!"}[power]));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
    }
    public static class Boros extends Zombie {
        public Boros(EntityType<? extends Zombie> type, Level level) { super(type, level); setCustomName(Component.literal("Boros")); setCustomNameVisible(true); }
        public static AttributeSupplier.Builder createAttributes() {
            return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 350).add(Attributes.ATTACK_DAMAGE, 18).add(Attributes.MOVEMENT_SPEED, 0.32).add(Attributes.ARMOR, 15).add(Attributes.FOLLOW_RANGE, 48);
        }
        @Override protected void registerGoals() { super.registerGoals(); }
        @Override public void tick() {
            super.tick();
            if (!level().isClientSide && tickCount % 100 == 0 && getTarget() != null && getTarget().distanceTo(this) < 15) {
                LivingEntity target = getTarget();
                target.hurt(damageSources().mobAttack(this), getHealth() < getMaxHealth()/2 ? 22 : 12);
                target.push(target.getX()-getX(), 0.7, target.getZ()-getZ());
            }
            if (!level().isClientSide && tickCount % 40 == 0 && getHealth() < getMaxHealth()/2) addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 2));
        }
        @Override protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource source, int looting, boolean recentlyHit) {
            super.dropCustomDeathLoot(source, looting, recentlyHit);
            spawnAtLocation(new ItemStack(Items.NETHER_STAR));
        }
    }
}
