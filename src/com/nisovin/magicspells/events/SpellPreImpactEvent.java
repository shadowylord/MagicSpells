package com.nisovin.magicspells.events;

import java.util.Arrays;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;

public class SpellPreImpactEvent extends SpellEvent implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	
	private boolean canceled;
	
	private LivingEntity target;
	private float power;
	private boolean redirect;
	private Spell deliverySpell;
	
	public SpellPreImpactEvent(Spell spellPayload, Spell deliverySpell, Player caster, LivingEntity target, float power) {
		super(spellPayload, caster);
		this.target = target;
		this.power = power;
		redirect = false;
		this.deliverySpell = deliverySpell;
		canceled = false;
		if (DebugHandler.isSpellPreImpactEventCheckEnabled()) {
			MagicSpells.plugin.getLogger().info(this.toString());
		}
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public LivingEntity getTarget() {
		return target;
	}
	
	public boolean getRedirected() {
		return redirect;
	}
	
	public void setRedirected(boolean redirect) {
		this.redirect = redirect;
	}
	
	public float getPower() {
		return power;
	}
	
	public void setPower(float power) {
		this.power = power;
	}
	
	public Spell getDeliverySpell() {
		return deliverySpell;
	}

	@Override
	public boolean isCancelled() {
		return canceled;
	}

	@Override
	public void setCancelled(boolean canceled) {
		this.canceled = canceled;
	}
	
	@Override
	public String toString() {
		String casterLabel = "Caster: " + ((caster == null) ? "null": caster.toString());
		String targetLabel = "Target: " + ((target == null) ? "null": target.toString());
		String spellLabel = "SpellPayload: " + ((spell == null)? "null": spell.toString());
		String payloadSpellLabel = "Delivery Spell: " + ((deliverySpell == null)? "null": deliverySpell.toString());
		return Arrays.deepToString(new String[]{casterLabel, targetLabel, spellLabel, payloadSpellLabel});
	}
}
