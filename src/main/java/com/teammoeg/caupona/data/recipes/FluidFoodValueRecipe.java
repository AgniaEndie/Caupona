/*
 * Copyright (c) 2022 TeamMoeg
 *
 * This file is part of Caupona.
 *
 * Caupona is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Caupona is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Specially, we allow this software to be used alongside with closed source software Minecraft(R) and Forge or other modloader.
 * Any mods or plugins can also use apis provided by forge or com.teammoeg.caupona.api without using GPL or open source.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caupona. If not, see <https://www.gnu.org/licenses/>.
 */

package com.teammoeg.caupona.data.recipes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teammoeg.caupona.data.IDataRecipe;
import com.teammoeg.caupona.data.SerializeUtil;
import com.teammoeg.caupona.util.Utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredHolder;

public class FluidFoodValueRecipe extends IDataRecipe {
	public static Map<ResourceLocation, RecipeHolder<FluidFoodValueRecipe>> recipes;
	public static DeferredHolder<RecipeType<?>,RecipeType<Recipe<?>>> TYPE;
	public static DeferredHolder<RecipeSerializer<?>,RecipeSerializer<?>> SERIALIZER;

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER.get();
	}

	@Override
	public RecipeType<?> getType() {
		return TYPE.get();
	}

	public int heal;
	public float sat;
	public List<Pair<MobEffectInstance, Float>> effects;
	private ItemStack repersent;
	public int parts;
	public ResourceLocation f;
	public static final Codec<FluidFoodValueRecipe> CODEC=
		RecordCodecBuilder.create(t->t.group(
			Codec.INT.fieldOf("heal").forGetter(o->o.heal),
			Codec.FLOAT.fieldOf("sat").forGetter(o->o.sat),
			Codec.optionalField("effects",Codec.list(Utils.MOB_EFFECT_FLOAT_CODEC)).forGetter(o->Optional.ofNullable(o.effects)),
			Codec.optionalField("item",Ingredient.CODEC).forGetter(o->o.repersent==null?Optional.empty():Optional.of(Ingredient.of(o.repersent))),
			Codec.INT.fieldOf("parts").forGetter(o->o.parts),
			ResourceLocation.CODEC.fieldOf("fluid").forGetter(o->o.f)
				).apply(t, FluidFoodValueRecipe::new));
	public FluidFoodValueRecipe(int heal, float sat, ItemStack repersent, int parts, Fluid f) {
		this.heal = heal;
		this.sat = sat;
		this.repersent = repersent;
		this.parts = parts;
		this.f = Utils.getRegistryName(f);
	}

	public FluidFoodValueRecipe(FriendlyByteBuf data) {
		heal = data.readVarInt();
		sat = data.readFloat();
		parts = data.readVarInt();
		f = data.readResourceLocation();
		effects = SerializeUtil.readList(data, d -> new Pair<>(MobEffectInstance.load(d.readNbt()), d.readFloat()));
		repersent = SerializeUtil.readOptional(data, d -> ItemStack.of(d.readNbt())).orElse(null);
	}

	public FluidFoodValueRecipe(int heal, float sat, Optional<List<Pair<MobEffectInstance, Float>>> effects, Optional<Ingredient> repersent, int parts, ResourceLocation f) {
		super();
		this.heal = heal;
		this.sat = sat;
		this.effects = effects.orElse(null);
		if (!repersent.isEmpty())
		this.repersent = repersent.map(o->o.getItems()[0]).orElse(null);
		this.parts = parts;
		this.f = f;
	}

	public FluidFoodValueRecipe(int heal, float sat, ItemStack repersent, int parts,
			ResourceLocation f) {
		this.heal = heal;
		this.sat = sat;
		this.repersent = repersent;
		this.parts = parts;
		this.f = f;
	}


	public void write(FriendlyByteBuf data) {
		data.writeVarInt(heal);
		data.writeFloat(sat);
		data.writeVarInt(parts);
		data.writeResourceLocation(f);
		SerializeUtil.writeList2(data, effects, (d, e) -> {
			CompoundTag nc = new CompoundTag();
			e.getFirst().save(nc);
			d.writeNbt(nc);
			d.writeFloat(e.getSecond());
		});
		SerializeUtil.writeOptional(data, repersent, (d, e) -> e.writeNbt(d.save(new CompoundTag())));
	}

	public ItemStack getRepersent() {
		return repersent;
	}

	public void setRepersent(ItemStack repersent) {
		if (repersent != null)
			this.repersent = repersent.copy();
		else
			this.repersent = null;
	}
}
