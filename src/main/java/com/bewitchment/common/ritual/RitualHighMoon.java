package com.bewitchment.common.ritual;

import com.bewitchment.api.ritual.IRitualHandler;
import com.bewitchment.api.ritual.Ritual;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class RitualHighMoon extends Ritual {

	public RitualHighMoon(ResourceLocation registryName, NonNullList<Ingredient> input, NonNullList<ItemStack> output, int timeInTicks, int circles, int altarPower, int costPerTick) {
		super(registryName, input, output, timeInTicks, circles, altarPower, costPerTick);
	}

	@Override
	public void onFinish(EntityPlayer player, IRitualHandler tile, World world, BlockPos pos, NBTTagCompound tag) {
		if (!world.isRemote) world.setWorldTime(17600);
	}

	@Override
	public boolean isValid(EntityPlayer player, World world, BlockPos pos, List<ItemStack> recipe) {
		return world.isDaytime();
	}

}
