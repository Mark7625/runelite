/*
 * Copyright (c) 2021, Jordan Atwood <nightfirecat@protonmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.skillcalculator.skills;

import java.util.EnumSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter(onMethod_ = @Override)
public enum PrayerBonus implements SkillBonus
{
	LIT_GILDED_ALTAR("Lit Gilded Altar (350%)", 3.5f),
	ECTOFUNTUS("Ectofuntus (400%)", 4),
	CHAOS_ALTAR("Chaos Altar (700%)", 7),
	MORYTANIA_DIARY_3_SHADES("Morytania Diary 3 Shades (150%)", 1.5f),
	BONECRUSHER("Bonecrusher (50%)", 0.5f),
	SINISTER_OFFERING("Sinister Offering (300%)", 3),
	DEMONIC_OFFERING("Demonic Offering (300%)", 3),
	SACRED_BONE_BURNER("Sacred Bone Burner (300%)", 3),
	ZEALOT_ROBES("Zealot Robes (105%)", 1.05f),
	;

	private final String name;
	private final float value;

	@Override
	public Set<PrayerBonus> getCanBeStackedWith()
	{
		final Set<PrayerBonus> others = EnumSet.noneOf(PrayerBonus.class);

		switch (this)
		{
			case ECTOFUNTUS:
			case LIT_GILDED_ALTAR:
			case CHAOS_ALTAR:
				others.add(ZEALOT_ROBES);
				others.add(DEMONIC_OFFERING);
				break;
			case SACRED_BONE_BURNER:
			case BONECRUSHER:
				others.add(DEMONIC_OFFERING);
				break;
			case MORYTANIA_DIARY_3_SHADES:
				others.add(DEMONIC_OFFERING);
				others.add(SINISTER_OFFERING);
				break;
			case ZEALOT_ROBES:
				others.add(ECTOFUNTUS);
				others.add(LIT_GILDED_ALTAR);
				others.add(CHAOS_ALTAR);
				others.add(DEMONIC_OFFERING);
				break;
			case DEMONIC_OFFERING:
				return EnumSet.complementOf(EnumSet.of(DEMONIC_OFFERING));
			case SINISTER_OFFERING:
				others.add(MORYTANIA_DIARY_3_SHADES);
				others.add(DEMONIC_OFFERING);
				break;
		}

		return others;
	}
}
