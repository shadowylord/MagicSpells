package com.nisovin.magicspells.spells.command;

import java.util.Collection;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.PlayerNameUtils;

// advanced perm is for listing other player's spells
public class ListSpell extends CommandSpell {
	
	private int lineLength = 60;
	private boolean onlyShowCastableSpells;
	private boolean reloadGrantedSpells;
	private List<String> spellsToHide;
	private String strNoSpells;
	private String strPrefix;

	public ListSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		onlyShowCastableSpells = getConfigBoolean("only-show-castable-spells", false);
		reloadGrantedSpells = getConfigBoolean("reload-granted-spells", true);
		spellsToHide = getConfigStringList("spells-to-hide", null);
		strNoSpells = getConfigString("str-no-spells", "You do not know any spells.");
		strPrefix = getConfigString("str-prefix", "Known spells:");
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			String extra = "";
			if (args != null && args.length > 0 && spellbook.hasAdvancedPerm("list")) {
				Player p = PlayerNameUtils.getPlayer(args[0]);
				if (p != null) {
					spellbook = MagicSpells.getSpellbook(p);
					extra = "(" + p.getDisplayName() + ") ";
				}
			}
			if (spellbook != null && reloadGrantedSpells) {
				spellbook.addGrantedSpells();
			}
			if (spellbook == null || spellbook.getSpells().size() == 0) {
				// no spells
				sendMessage(strNoSpells, player, args);
			} else {
				String s = "";
				for (Spell spell : spellbook.getSpells()) {
					if (!spell.isHelperSpell() && (!onlyShowCastableSpells || spellbook.canCast(spell)) && !(spellsToHide != null && spellsToHide.contains(spell.getInternalName()))) {
						if (s.equals("")) {
							s = spell.getName();
						} else {
							s += ", " + spell.getName();
						}
					}
				}
				s = strPrefix + " " + extra + s;
				while (s.length() > lineLength) {
					int i = s.substring(0, lineLength).lastIndexOf(' ');
					sendMessage(s.substring(0, i), player, args);
					s = s.substring(i+1);
				}
				if (s.length() > 0) {
					sendMessage(s, player, args);
				}
			}
		}		
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String partial) {
		if (sender instanceof ConsoleCommandSender) {
			if (!partial.contains(" ")) {
				return tabCompletePlayerName(sender, partial);
			}
		}
		return null;
	}

	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		StringBuilder s = new StringBuilder();
		
		// get spell list
		Collection<Spell> spells = MagicSpells.spells();
		if (args != null && args.length > 0) {
			Player p = PlayerNameUtils.getPlayer(args[0]);
			if (p == null) {
				sender.sendMessage("No such player.");
				return true;
			} else {
				spells = MagicSpells.getSpellbook(p).getSpells();
				s.append(p.getName() + "'s spells: ");
			}
		} else {
			s.append("All spells: ");
		}
		
		// create string of spells
		for (Spell spell : spells) {
			s.append(spell.getName());
			s.append(" ");
		}
		
		// send message
		sender.sendMessage(s.toString());
		
		return true;
	}

}
