package com.bewitchment.common.core.event;

import com.bewitchment.api.capability.EnumTransformationType;
import com.bewitchment.api.capability.ITransformationData;
import com.bewitchment.api.event.HotbarActionCollectionEvent;
import com.bewitchment.api.event.HotbarActionTriggeredEvent;
import com.bewitchment.common.abilities.ModAbilities;
import com.bewitchment.common.core.capability.transformation.CapabilityTransformationData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class VampireAbilityHandler {

	public static final DamageSource SUN_DAMAGE = new DamageSource("sun_on_vampire").setDamageBypassesArmor().setDamageIsAbsolute().setFireDamage();

	public static final String NIGHT_VISION_TAG = "ability_night_vision";

	/**
	 * Modifies damage depending on the type. Fire and explosion make it 150%of the original,
	 * all the other types make it 10% of the original provided there's blood in the pool
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void onDamageReceived(LivingHurtEvent evt) {
		if (!evt.getEntity().world.isRemote && evt.getEntityLiving() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) evt.getEntityLiving();
			ITransformationData data = player.getCapability(CapabilityTransformationData.CAPABILITY, null);
			if (data.getType() == EnumTransformationType.VAMPIRE) {
				if (evt.getSource() == SUN_DAMAGE)
					return;
				if (evt.getSource().isFireDamage() || evt.getSource().isExplosion()) {
					evt.setCanceled(false);
					evt.setAmount(evt.getAmount() * 1.5f);
				} else if (data.getBlood() > 0) { // Don't mitigate damage when there is no blood in the pool
					evt.setAmount(evt.getAmount() * 0.1f);
					if (evt.getAmount() > 5f) {
						evt.setAmount(5f);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void checkSun(PlayerTickEvent evt) {
		if (evt.side.isServer()) {
			ITransformationData data = evt.player.getCapability(CapabilityTransformationData.CAPABILITY, null);
			if (data.getType() == EnumTransformationType.VAMPIRE && evt.player.world.getTotalWorldTime() % 40 == 0) {
				if (evt.player.world.canBlockSeeSky(evt.player.getPosition()) && evt.player.world.isDaytime() && !evt.player.world.isRainingAt(evt.player.getPosition())) {
					if (data.getLevel() < 5 || !data.addVampireBlood(-11 + data.getLevel())) {
						evt.player.attackEntityFrom(SUN_DAMAGE, 11 - data.getLevel());
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void attachAbilities(HotbarActionCollectionEvent evt) {
		ITransformationData data = evt.player.getCapability(CapabilityTransformationData.CAPABILITY, null);
		if (data.getType() == EnumTransformationType.VAMPIRE && data.getLevel() > 5) {
			evt.getList().add(ModAbilities.NIGHT_VISION);
		} else {
			data.getMiscDataTag().setBoolean(NIGHT_VISION_TAG, false);
		}
	}

	@SubscribeEvent
	public void onAbilityToggled(HotbarActionTriggeredEvent evt) {
		if (evt.action == ModAbilities.NIGHT_VISION) {
			ITransformationData data = evt.player.getCapability(CapabilityTransformationData.CAPABILITY, null);
			data.getMiscDataTag().setBoolean(NIGHT_VISION_TAG, !data.getMiscDataTag().getBoolean(NIGHT_VISION_TAG));
		}
	}

	@SubscribeEvent
	public void abilityHandler(PlayerTickEvent evt) {
		if (evt.phase == Phase.START) {
			PotionEffect nv = evt.player.getActivePotionEffect(MobEffects.NIGHT_VISION);
			if ((nv == null || nv.getDuration() <= 200) && evt.player.getCapability(CapabilityTransformationData.CAPABILITY, null).getMiscDataTag().getBoolean(NIGHT_VISION_TAG)) {
				evt.player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 300, 0, true, false));
			}
		}
	}

}
