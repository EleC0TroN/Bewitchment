package com.bewitchment.api.ritual;

import com.bewitchment.common.lib.LibMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Ritual extends IForgeRegistryEntry.Impl<Ritual> {

	public static final IForgeRegistry<Ritual> REGISTRY = new RegistryBuilder<Ritual>().setName(new ResourceLocation(LibMod.MOD_ID, "rituals")).setType(Ritual.class).setIDRange(0, 200).create();
	private int time, circles, altarStartingPower, tickPower;
	private NonNullList<ItemStack> output;
	private NonNullList<Ingredient> input;
	private List<List<ItemStack>> jei_cache;

	/**
	 * Constructs a new ritual. To be registered within the registry
	 *
	 * @param input       a NonNullList<ItemStack> with all and every itemstack required to be a valid ritual
	 * @param output      a NonNullList<ItemStack> with all and every itemstack that should get dropped on the ground when the ritual stops
	 * @param timeInTicks the time in ticks that the ritual takes to stop. Negative values will have the ritual going on indefinitely. Zero means that the effect/crafting is applied immediately
	 * @param circles     is the byte annotation to define what circles are needed. It follows this pattern 332211TT where 33, 22, 11 are the glyph type of the nth circle, and TT the number of required circles, 0 being 1, 2 being 3. 3 (11) will always return a failed circle
	 */
	public Ritual(ResourceLocation registryName, @Nonnull NonNullList<Ingredient> input, @Nonnull NonNullList<ItemStack> output, int timeInTicks, int circles, int altarStartingPower, int powerPerTick) {
		this.time = timeInTicks;

		for (int i = 0; i < input.size(); i++) {
			Ingredient ing = input.get(i);
			if (ing.getMatchingStacks().length == 0)
				throw new IllegalArgumentException("Ritual inputs must be valid: ingredient #" + i + " for " + registryName + " has no matching items");
		}

		this.input = input;
		this.output = output;
		this.circles = circles;
		this.altarStartingPower = altarStartingPower;
		this.tickPower = powerPerTick;
		setRegistryName(registryName);
		if (input.size() == 0) throw new IllegalArgumentException("Cannot have an empty input in a ritual");
	}

	public static NonNullList<ItemStack> getItemsUsedForInput(NBTTagCompound tag) {
		NonNullList<ItemStack> list = NonNullList.create();
		NBTTagList tagList = tag.getTagList("itemsUsed", NBT.TAG_COMPOUND);
		tagList.forEach(nbt -> {
			NBTTagCompound itemTag = (NBTTagCompound) nbt;
			list.add(new ItemStack(itemTag));
		});
		return list;
	}

	// Check for extra conditions that need to be met (time of the day/phase of the moon/item hold in offhand/being in a dimension/having an amount of free space...)
	public boolean isValid(EntityPlayer player, World world, BlockPos pos, List<ItemStack> recipe) {
		return true;
	}

	// Called every tick if there is enough power
	public void onUpdate(@Nullable EntityPlayer player, IRitualHandler tile, World world, BlockPos pos, NBTTagCompound data, int ticks) {
	}

	// Called when the ritual stops because it has completed
	public void onFinish(@Nullable EntityPlayer player, IRitualHandler tile, World world, BlockPos pos, NBTTagCompound data) {
	}

	// Called when the ritual gets stopped by right-click or breaking of the glyph
	public void onStopped(@Nullable EntityPlayer player, IRitualHandler tile, World world, BlockPos pos, NBTTagCompound data) {
	}

	// Called when the ritual gets started
	public void onStarted(@Nullable EntityPlayer player, IRitualHandler tile, World world, BlockPos pos, NBTTagCompound data) {
	}

	// Called every tick if there is not enough power
	public void onLowPower(@Nullable EntityPlayer player, IRitualHandler tile, World world, BlockPos pos, NBTTagCompound data, int ticks) {
	}

	// Time required for completion
	public int getTime() {
		return time;
	}

	// Gets what shoud be spit out when the ritual finishes, data is used to allow the output of modified input items
	public NonNullList<ItemStack> getOutput(NBTTagCompound data) {
		return getOutputRaw();
	}

	// Checks circles and recipe
	public boolean isValidInput(List<ItemStack> ground, boolean circles) {
		ArrayList<ItemStack> checklist = new ArrayList<ItemStack>(ground.size());
		for (ItemStack item : ground)
			for (int j = 0; j < item.getCount(); j++) {
				ItemStack copy = item.copy();
				copy.setCount(1);
				checklist.add(copy);
			}

		if (checklist.size() != input.size()) {
			return false;
		}
		ArrayList<Ingredient> removalList = new ArrayList<Ingredient>(input);

		for (ItemStack stack_on_ground : checklist) {
			Ingredient found = null;
			for (Ingredient ingredient : removalList) {
				if (ingredient.apply(stack_on_ground)) {
					found = ingredient;
					break;
				}
			}
			if (found == null) {
				return false; //The stack on the ground doesn't belong to the rite
			}
			removalList.remove(found);
		}
		if (!removalList.isEmpty()) {
			return false;
		}
		return circles;
	}

	// Return the bit annotation of what circles are needed
	public int getCircles() {
		return circles;
	}

	public int getRequiredStartingPower() {
		return altarStartingPower;
	}

	public int getRunningPower() {
		return tickPower;
	}

	public List<Ingredient> getInput() {
		ArrayList<Ingredient> stacks = new ArrayList<Ingredient>(input.size());
		stacks.addAll(input);
		return stacks;
	}

	public List<List<ItemStack>> getJeiInput() {
		if (jei_cache == null) generateCache();
		return jei_cache;
	}

	private void generateCache() {
		jei_cache = new ArrayList<List<ItemStack>>();
		for (Ingredient i : input) jei_cache.add(Arrays.asList(i.getMatchingStacks()));
	}

	// Legacy method to check if a ritual needs to be performed on a circle or can be used through some other means
	// (Originally ritual candles)
	public boolean canBeUsedWithNoGlyphs() {
		return false;
	}
	
	/*
	 * known bugs
	 * FIXME - There might be a desync when disconnecting and reconnecting (noticed with the perception ritual)
	 */

	// Returns the static output
	// aka what should be shown by JEI (If an input gets damaged and then spit out, this should show the item at 0 damage
	public NonNullList<ItemStack> getOutputRaw() {
		NonNullList<ItemStack> copy = NonNullList.<ItemStack>create();
		for (ItemStack i : output)
			copy.add(i);
		return copy;
	}

}
