package net.omniblock.shop.api.type;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.npc.NPC;
import net.omniblock.shop.api.object.npc.NPCShop.NPCAction;

public enum NPCShopType {

	DEFAULT("Ciudadano", " ", new String[] {

			"�Woow, que hermosas vistas tengo aqu�, nunca me ir�a de esta ciudad!",
			"�Todos los d�as aumentas esos est�pidos impuestos, parece que quieren dejar al pueblo pobre!",
			"�Est� muy tranquila la ciudad hoy en d�a!" },

			Material.EMERALD, null),

	SHOP_FOOD("Alex", "PANADERO", " ", new String[] {

			"�Bienvenido! compre los m�s ricos panes de la ciudad!",
			"�Panes frescos y crujientes solo en este local!" },

			Material.BREAD, new NPCAction() {

				@Override
				public void clickEvent(NPC npc, Player player) {
					
					
				}
			}),

	SHOP_MATERIAL("Juan", "HERRERO", " ", new String[] {

			"�Piedras preciosas a buen precio!", 
			"�Ac�rcate y mira nuestra galer�a de cosas �nicas para ti!" },

			Material.DIAMOND_PICKAXE, new NPCAction() {

				@Override
				public void clickEvent(NPC npc, Player player) {

				}
			});

	;

	private String name;
	private String professionName;
	private String skin;
	private String[] npcDialogs;

	private Material material;
	private NPCAction action;

	/**
	 * NPCShopType > Tipos de NPC
	 * 
	 * @param name
	 *            Nombre que tendra el NPC.
	 * @param professionName
	 *            Nombre de la profesi�n que tendr� el NPC.
	 * @param skin
	 *            Nombre de la skin que se va a utilizar.
	 * @param npcDialogs
	 *            Dialogos de los NPCs.
	 * @param material
	 *            Material que representa su profesi�n.
	 * @param action
	 *            Acci�n que se realizara al hundir click al NPC.
	 * 
	 */

	NPCShopType(String name, String professionName, String skin, String[] npcDialogs, Material material,
			NPCAction action) {

		this.name = name;
		this.professionName = professionName;
		this.skin = skin;
		this.npcDialogs = npcDialogs;
		this.material = material;
		this.action = action;

	}

	NPCShopType(String name, String skin, String[] npcDialogs, Material material, NPCAction action) {

		this.name = name;
		this.skin = skin;
		this.npcDialogs = npcDialogs;
		this.material = material;
		this.action = action;

	}

	public String getName() {
		return name;
	}

	public String getProfessionName() {
		return professionName;
	}

	public String getSkin() {
		return skin;
	}

	public String[] getNpcDialogs() {
		return npcDialogs;
	}

	public Material getMaterial() {
		return material;
	}

	public NPCAction getAction() {
		return action;
	}
}
