package com.nisovin.magicspells.spells.command;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.CastItem;
import com.nisovin.magicspells.util.HandHandler;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

public class UnbindSpell extends CommandSpell {
	
	private String strUsage;
	private String strNoSpell;
	private String strCantBindSpell;
	private String strNotBound;
	private List<String> allowedSpellsNames = null;
	private Set<Spell> allowedSpells = null;
	private String strCantUnbind;

	public UnbindSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		strUsage = getConfigString("str-usage", "You must specify a spell name.");
		strNoSpell = getConfigString("str-no-spell", "You do not know a spell by that name.");
		strCantBindSpell = getConfigString("str-cant-bind-spell", "That spell cannot be bound to an item.");
		strNotBound = getConfigString("str-not-bound", "That spell is not bound to that item.");
		strCantUnbind = getConfigString("str-cant-unbind", "You cannot unbind this spell");
		allowedSpellsNames = getConfigStringList("allowed-spells", null);
		if (allowedSpellsNames != null && !allowedSpellsNames.isEmpty()) {
			allowedSpells = new HashSet<Spell>();
			for (String n: allowedSpellsNames) {
				Spell s = MagicSpells.getSpellByInternalName(n);
				if (s != null) {
					allowedSpells.add(s);
				} else {
					MagicSpells.plugin.getLogger().warning("Invalid spell defined: " + n);
				}
			}
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args == null || args.length == 0) {
				sendMessage(strUsage, player, args);
				return PostCastAction.ALREADY_HANDLED;
			} else {
				Spell spell = MagicSpells.getSpellByInGameName(Util.arrayJoin(args, ' '));
				Spellbook spellbook = MagicSpells.getSpellbook(player);
				if (spell == null || spellbook == null) {
					// fail - no such spell, or no spellbook
					sendMessage(strNoSpell, player, args);
					return PostCastAction.ALREADY_HANDLED;
				} else if (!spellbook.hasSpell(spell)) {
					// fail - doesn't know spell
					sendMessage(strNoSpell, player, args);
					return PostCastAction.ALREADY_HANDLED;
				} else if (!spell.canCastWithItem()) {
					// fail - spell can't be bound
					sendMessage(strCantBindSpell, player, args);
					return PostCastAction.ALREADY_HANDLED;
				} else {
					if (allowedSpells != null && !allowedSpells.contains(spell)) {
						sendMessage(strCantUnbind, player, args);
						return PostCastAction.ALREADY_HANDLED;
					}
					CastItem item = new CastItem(HandHandler.getItemInMainHand(player));
					boolean removed = spellbook.removeCastItem(spell, item);
					if (!removed) {
						sendMessage(strNotBound, player, args);
						return PostCastAction.ALREADY_HANDLED;
					}
					spellbook.save();
					sendMessage(formatMessage(strCastSelf, "%s", spell.getName()), player, args);
					playSpellEffects(EffectPosition.CASTER, player);
					return PostCastAction.NO_MESSAGES;
				}
			}
		}		
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String partial) {
		if (sender instanceof Player) {
			// only one arg
			if (partial.contains(" ")) {
				return null;
			}
			
			// tab complete spellname from spellbook
			return tabCompleteSpellName(sender, partial);
		}
		return null;
	}

	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}

}
