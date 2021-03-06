package com.bewitchment.common.item.food;

import com.bewitchment.common.core.BewitchmentCreativeTabs;
import com.bewitchment.common.lib.LibItemName;
import net.minecraft.init.MobEffects;

/**
 * This class was created by Joseph on 02/03/2017.
 * It's distributed as part of Bewitchment under
 * the MIT license.
 */
public class ItemThistle extends ItemCrop {

	public ItemThistle() {
		super(LibItemName.THISTLE, 4, 0.8F, false);
		addPotion(MobEffects.STRENGTH);
		setCreativeTab(BewitchmentCreativeTabs.PLANTS_CREATIVE_TAB);
	}
}
