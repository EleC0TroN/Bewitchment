package com.bewitchment.common.core.capability.divination;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import static com.bewitchment.common.core.capability.divination.CapabilityDivination.CAPABILITY;

public class DivinationProvider implements ICapabilitySerializable<NBTBase> {

	private CapabilityDivination default_capability = CAPABILITY.getDefaultInstance();

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CAPABILITY)
			return CAPABILITY.<T>cast(default_capability);
		return null;
	}

	@Override
	public NBTBase serializeNBT() {
		return CAPABILITY.getStorage().writeNBT(CAPABILITY, default_capability, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		CAPABILITY.getStorage().readNBT(CAPABILITY, default_capability, null, nbt);
	}

}