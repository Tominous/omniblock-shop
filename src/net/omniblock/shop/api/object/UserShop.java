package net.omniblock.shop.api.object;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;

import net.omniblock.network.handlers.base.sql.util.Resolver;
import net.omniblock.network.library.utils.TextUtil;
import net.omniblock.packets.util.Lists;
import net.omniblock.shop.ShopPlugin;
import net.omniblock.shop.api.ShopSignManager;
import net.omniblock.shop.api.config.ConfigType;
import net.omniblock.shop.api.config.variables.ItemsProtocol;
import net.omniblock.shop.api.exception.SignLoadException;
import net.omniblock.shop.api.type.ShopActionType;
import net.omniblock.shop.api.type.ShopType;
import net.omniblock.shop.utils.ItemNameUtils;

public class UserShop extends AbstractShop {
	
	public static List<Player> waitlistPlayers = Lists.newArrayList();
	
	protected ItemStack shopItem = new ItemStack(Material.ITEM_FRAME, 1);
	
	protected Hologram hologram;
	protected ItemLine itemLine;
	protected Player cachePlayer;
	
	protected UserShopStatus status = UserShopStatus.WAITING_ITEM;
	
	protected boolean destroyed = false;
	protected boolean savedShop = false;
	
	public UserShop(Sign sign, Chest chest, int price, String playerNetworkID, String uniqueID) {
		
		super(sign, chest, ShopType.PLAYER_SHOP, price, playerNetworkID, uniqueID);
		
		this.sign = sign;
		return;
		
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public ShopLoadStatus loadSign(Player player) {
		
		if(ConfigType.SHOPDATA.getConfig().isSet("usershop." + uniqueID)) {
			
			if(!ConfigType.SHOPDATA.getConfig().isSet("usershop." + uniqueID + ".shopItem"))
				throw new SignLoadException("No se ha podido cargar el cartel '" + uniqueID + "' porque hace falta el shopItem en la configuraci�n.");
			
			if(!ConfigType.SHOPDATA.getConfig().isSet("usershop." + uniqueID + ".actionType"))
				throw new SignLoadException("No se ha podido cargar el cartel '" + uniqueID + "' porque hace falta el actionType en la configuraci�n.");
			
			if(!ConfigType.SHOPDATA.getConfig().isSet("usershop." + uniqueID + ".status"))
				throw new SignLoadException("No se ha podido cargar el cartel '" + uniqueID + "' porque hace falta el status en la configuraci�n.");
			
			
			shopItem = ConfigType.SHOPDATA.getConfig().getItemStack("usershop." + uniqueID + ".shopItem");
			shopItem = ConfigType.SHOPDATA.getConfig().getItemStack("usershop." + uniqueID + ".shopItem");
			actionType = ShopActionType.valueOf(ConfigType.SHOPDATA.getConfig().getString("usershop." + uniqueID + ".actionType"));
			status = UserShopStatus.valueOf(ConfigType.SHOPDATA.getConfig().getString("usershop." + uniqueID + ".status"));
			hologram = HologramsAPI.createHologram(ShopPlugin.getInstance(), chest.getLocation().clone().add(.5, 1.8, .5));
			itemLine = hologram.appendItemLine(shopItem);
			savedShop = true;
			
			if(status == UserShopStatus.WAITING_ITEM) {
				
				this.destroySign();
				return ShopLoadStatus.CANNOT_LOAD;
				
			}
			
			sign.setLine(0, actionType.getFormattedAction());
			sign.setLine(1, Resolver.getLastNameByNetworkID(playerNetworkID));
			sign.setLine(2, TextUtil.format("&8" + ItemNameUtils.getMaterialName(shopItem.getType())));
			sign.setLine(3, TextUtil.format(actionType == ShopActionType.BUY ? "&a&l$&a" + price : "&6&l$&6" + price));
			sign.update(true);
			return ShopLoadStatus.LOADED;
			
		}
		
		if(player == null)
			return ShopLoadStatus.CANNOT_LOAD;
		
		if(!player.isOnline())
			return ShopLoadStatus.CANNOT_LOAD;
		
		if(waitlistPlayers.contains(player)) {
			this.sign.getBlock().breakNaturally();
			player.sendMessage(TextUtil.format("&8&lT&8iendas &b&l� &cDebes terminar de crear la tienda que estabas haciendo antes de hacer otra.")); 
			return ShopLoadStatus.CANNOT_LOAD;
		}
		
		this.setShopActionType(ShopActionType.getByMiddleLine(this.sign.getLine(1)));
		
		this.cachePlayer = player;
		this.shopItem = player.getItemInHand();
		this.savedShop = true;
		this.status = UserShopStatus.WAITING_ITEM;
		
		this.waitItem(player);
		this.saveSign();
		return ShopLoadStatus.LOADED;
		
	}

	@Override
	public void destroySign() {
		
		destroyed = true;
		ShopSignManager.removeShop(this);
		
		if(hologram != null)
			hologram.delete();
		
		if(ConfigType.SHOPDATA.getConfig().isSet("usershop." + uniqueID))
			ConfigType.SHOPDATA.getConfig().set("usershop." + uniqueID, null);
		
		ConfigType.SHOPDATA.getConfigObject().save();
		return;
		
	}
	
	public void waitItem(Player player) {
		
		waitlistPlayers.add(player);
		
		sign.setLine(1, TextUtil.format("Has click con el"));
		sign.setLine(2, TextUtil.format("tipo de item que"));
		sign.setLine(3, TextUtil.format(actionType == ShopActionType.BUY ? "vender�s" : "comprar�s") + ".");
		
		player.sendMessage(TextUtil.format("&8&lT&8iendas &b&l� &7Ahora debes hacer click derecho con el item que usar�s en la tienda.")); 
		
		new BukkitRunnable() {
			
			private int seconds = 60;
			
			@Override
			public void run() {
				
				seconds--;
				
				if(status == UserShopStatus.CREATED || destroyed == true) {
					
					this.cancel();
					return;
					
				}
				
				if(!(sign.getBlock().getState() instanceof Sign)) {
					
					this.cancel();
					destroySign();
					return;
					
				}
					
				
				if(seconds - 1 == 0 || !player.isOnline()) {
					
					this.cancel();
					
					if(player.isOnline())
						player.sendMessage(TextUtil.format("&8&lT&8iendas &b&l� &cTu tienda se ha eliminado porque no has colocado el item que usar�as en ella."));
					
					waitlistPlayers.remove(player);
					
					destroySign();
					sign.getBlock().breakNaturally();
					return;
					
				}
				
				sign.setLine(0, TextUtil.format("&8&lESPERANDO &c" + seconds));
				sign.update(true);
				return;
				
			}
			
		}.runTaskTimer(ShopPlugin.getInstance(), 0l, 20l);
		
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void clickEvent(PlayerInteractEvent e) {
		
		if(status == UserShopStatus.WAITING_ITEM) {
			
			if(waitlistPlayers.contains(e.getPlayer())) {
				
				Player player = e.getPlayer();
				
				if(player.getItemInHand() == null) {
					player.sendMessage(TextUtil.format("&8&lT&8iendas &b&l� &cDebes tener el item que usar�s en la tienda puesto en tu mano.")); 
					return;
				}
				
				if(ItemsProtocol.isMaterialBlocked(player.getItemInHand().getType())) {
					player.sendMessage(TextUtil.format("&8&lT&8iendas &b&l� &cEl item que intentas usar en la tienda est� bloqueado.")); 
					return;
				}
				
				shopItem = e.getPlayer().getItemInHand();
				status = UserShopStatus.CREATED;
				
				hologram = HologramsAPI.createHologram(ShopPlugin.getInstance(), chest.getLocation().clone().add(.5, 1.8, .5));
				itemLine = hologram.appendItemLine(shopItem);
				
				sign.setLine(0, actionType.getFormattedAction());
				sign.setLine(1, Resolver.getLastNameByNetworkID(playerNetworkID));
				sign.setLine(2, TextUtil.format("&8" + ItemNameUtils.getMaterialName(shopItem.getType())));
				sign.setLine(3, TextUtil.format(actionType == ShopActionType.BUY ? "&a&l$&a" + price : "&6&l$&6" + price));
				sign.update(true);
				this.saveSign();
				
				waitlistPlayers.remove(e.getPlayer());
				
				e.getPlayer().sendMessage(TextUtil.format("&8&lT&8iendas &b&l� &aHas creado una tienda correctamente!"));
				e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 10);
				return;
				
			}
			
			e.getPlayer().sendMessage(TextUtil.format("&8&lT&8iendas &b&l� &cLa tienda a la que intentas acceder se encuentra en construcci�n.")); 
			return;
			
		}
		
		e.getPlayer().sendMessage("Procesando compra :v");
		
	}

	@SuppressWarnings("serial")
	@Override
	public Map<String, Object> getConfigData() {
		return new HashMap<String, Object>(){{
			
			put("usershop." + uniqueID + ".location", sign.getWorld().getName() + "," + sign.getX() + "," + sign.getY() + "," + sign.getZ());
			put("usershop." + uniqueID + ".playerNetworkID", playerNetworkID);
			put("usershop." + uniqueID + ".price", price);
			put("usershop." + uniqueID + ".shopItem", shopItem);
			put("usershop." + uniqueID + ".savedShop", savedShop);
			put("usershop." + uniqueID + ".status", status.name());
			put("usershop." + uniqueID + ".actionType", actionType.name());
			
		}};
	}

	public Hologram getHologram() {
		return hologram;
	}
	
	public static enum UserShopStatus {
		
		WAITING_ITEM,
		CREATED,
		
		;
		
	}
	
}
