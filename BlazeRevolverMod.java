package com.example.blazerevolver;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlazeRevolverMod implements ModInitializer {

    @Override
    public void onInitialize() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide() || player.isSpectator()) {
                return InteractionResult.PASS;
            }

            ItemStack heldItem = player.getItemInHand(hand);

            if (heldItem.is(Items.BLAZE_ROD)) {
                /*
                Component customName = heldItem.get(DataComponents.CUSTOM_NAME);
                if (customName == null || !customName.getString().equalsIgnoreCase("Revolver")) {
                    return InteractionResult.PASS;
                }
                */

                if (entity instanceof Player targetPlayer) {
                    shufflePlayerHotbarAndOffhand(targetPlayer);
                    playRevolverEffects(world, player, targetPlayer);
                } else if (entity instanceof LivingEntity targetMob) {
                    swapMobHands(targetMob);
                    playRevolverEffects(world, player, targetMob);
                }
            }

            return InteractionResult.PASS;
        });
    }

    private void shufflePlayerHotbarAndOffhand(Player target) {
        Inventory inv = target.getInventory();
        List<ItemStack> pool = new ArrayList<>(10);

        for (int i = 0; i < 9; i++) {
            pool.add(inv.getItem(i).copy());
        }

        pool.add(target.getOffhandItem().copy());

        Collections.shuffle(pool);

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, pool.get(i));
        }

        target.setItemSlot(EquipmentSlot.OFFHAND, pool.get(9));

        inv.setChanged();
        if (target instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.broadcastChanges();
        }
    }

    private void swapMobHands(LivingEntity mob) {
        ItemStack mainHand = mob.getMainHandItem().copy();
        ItemStack offHand = mob.getOffhandItem().copy();
        mob.setItemSlot(EquipmentSlot.MAINHAND, offHand);
        mob.setItemSlot(EquipmentSlot.OFFHAND, mainHand);
    }

    private void playRevolverEffects(net.minecraft.world.level.Level world, Player attacker, LivingEntity target) {
        // Sharp metallic revolver click + mystical shuffle chime
        world.playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.DISPENSER_DISPENSE,
                SoundSource.PLAYERS,
                1.0F,
                1.6F
        );
        world.playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                0.7F,
                1.8F
        );

        // Burst of tiny purple particles (PORTAL + WITCH) around the hit target
        if (world instanceof ServerLevel serverLevel) {
            double x = target.getX();
            double y = target.getY() + target.getBbHeight() * 0.5;
            double z = target.getZ();

            serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    x, y, z,
                    35,    // particle count
                    0.35,  // spread X
                    0.45,  // spread Y
                    0.35,  // spread Z
                    0.15   // speed
            );
            serverLevel.sendParticles(
                    ParticleTypes.WITCH,
                    x, y, z,
                    15,
                    0.3,
                    0.4,
                    0.3,
                    0.05
            );
        }
    }
}
