package net.meano.ServerMessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerMessageListener implements Listener {
	ServerMessageMain SMS;

	public class BlockDisplay
	{
		String NickName;
		ChatColor Color;
		public BlockDisplay(String nickName, ChatColor color) {
			NickName = nickName;
			Color = color;
		}
	}
	
	public ServerMessageListener(ServerMessageMain sms) {
		SMS = sms;
		SMS.getServer().getPluginManager().registerEvents(this, sms);
		LogBlockMap.put(Material.COAL_ORE, new BlockDisplay("煤", ChatColor.DARK_GRAY));
		LogBlockMap.put(Material.DIAMOND_ORE, new BlockDisplay("钻石", ChatColor.AQUA));
		LogBlockMap.put(Material.EMERALD_ORE, new BlockDisplay("绿宝石", ChatColor.GREEN));
		LogBlockMap.put(Material.GOLD_ORE, new BlockDisplay("金", ChatColor.GOLD));
		LogBlockMap.put(Material.IRON_ORE, new BlockDisplay("铁", ChatColor.GRAY));
		LogBlockMap.put(Material.LAPIS_ORE, new BlockDisplay("青金石", ChatColor.BLUE));
		LogBlockMap.put(Material.NETHER_QUARTZ_ORE, new BlockDisplay("石英", ChatColor.WHITE));
		LogBlockMap.put(Material.REDSTONE_ORE, new BlockDisplay("红石", ChatColor.DARK_RED));
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onTransferItem(InventoryMoveItemEvent event) {
		Inventory destInventory = event.getDestination();
		if(!destInventory.getType().equals(InventoryType.HOPPER)) {
			return;
		}
		BlockState blockState = destInventory.getLocation().getBlock().getState();
		if(!(blockState instanceof Hopper)) {
			return;
		}
		Hopper hopper = (Hopper) blockState;
		try {
			Material filterItem = Material.valueOf(hopper.getCustomName());
			if(!event.getItem().getType().equals(filterItem)) {
				event.setCancelled(true);
			}
		}
		catch(Exception e) {
		}
		//SMS.getLogger().info("hopper " + hopper.getCustomName() + " recv " + event.getItem().getType().toString() + " from " + event.getSource().getType().toString() + " filter " + filterItem);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent e) {
		String message = ChatColor.translateAlternateColorCodes('&', SMS.getConfig().getString("ClientMessages.Join")).replaceAll("%Player", e.getPlayer().getDisplayName());
		e.setJoinMessage(message);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent e) {
		String message = ChatColor.translateAlternateColorCodes('&', SMS.getConfig().getString("ClientMessages.Quit")).replaceAll("%Player", e.getPlayer().getDisplayName());
		e.setQuitMessage(message);
	}
	
	@EventHandler
	public void onPlayerMessage(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();
		if(!player.getDisplayName().equals(player.getName())) {
			e.setFormat("%s" + ChatColor.RESET + ": %s");
			if(player.isOp())
				e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));
		}
	}

	public int getTotalBlocks(Block original) {
        HashSet<Location> blocks = new HashSet<>();
        blocks.add(original.getLocation());
        cycleHorizontalFaces(original.getType(), original, blocks, true);
        return Math.min(blocks.size(), 500);
    }

    public HashSet<Location> getAllLikeBlockLocations(Block original) {
        HashSet<Location> blocks = new HashSet<Location>();
        blocks.add(original.getLocation());
        cycleHorizontalFaces(original.getType(), original, blocks, false);
        return blocks;
    }
    
    private final BlockFace[] horizontalFaces = {
		BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH,
        BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, 
        BlockFace.DOWN, BlockFace.UP
    };
    private final BlockFace[] upperFaces = {
		BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH,
        BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, 
        BlockFace.UP
    };
    private final BlockFace[] LowerFaces = {
		BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH,
        BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST,
        BlockFace.DOWN
    };

    private void cycleHorizontalFaces(Material mat, Block original, Set<Location> blocks, boolean counting) {
        if (blocks.size() >= 500) { return; }
        findLikeBlocks(horizontalFaces, original, mat, blocks, counting);
        if (blocks.size() >= 500) { return; }
        Block upper = original.getRelative(BlockFace.UP);
        findLikeBlocks(upperFaces, upper, mat, blocks, counting);
        if (blocks.size() >= 500) { return; }
        Block lower = original.getRelative(BlockFace.DOWN);
        findLikeBlocks(LowerFaces, lower, mat, blocks, counting);
    }

    private void findLikeBlocks(BlockFace[] faces, Block passed, Material originalMaterial, Set<Location> blocks, boolean counting) {
        for (BlockFace y : faces) {
            Block likeBlock = passed.getRelative(y);
            if (likeBlock.getType() == originalMaterial && !blocks.contains(likeBlock.getLocation()) && !PlacedLocations.contains(likeBlock.getLocation())) {
                if (counting) {
                	PlacedLocations.add(likeBlock.getLocation());
                }
                blocks.add(likeBlock.getLocation());
                if (blocks.size() >= 500) { return; }
                cycleHorizontalFaces(originalMaterial, likeBlock, blocks, counting);
            }
        }
    }
	
	public Map<Material,BlockDisplay> LogBlockMap = new HashMap<Material,BlockDisplay>();
	public static HashSet<Location> PlacedLocations = new HashSet<Location>();
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
		Block block = event.getBlock();
		BlockDisplay blockDisplay = LogBlockMap.get(block.getType());
		if(blockDisplay == null) {
			return;
		}
		if(PlacedLocations.contains(block.getLocation())) {
			PlacedLocations.remove(block.getLocation());
			return;
		}
		int totalBlockCount = getTotalBlocks(block);
		Bukkit.broadcastMessage(event.getPlayer().getDisplayName() + " 找到了 " + totalBlockCount + " 块 " + blockDisplay.Color + blockDisplay.NickName + ChatColor.RESET + " 矿.");
	}
	
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		BlockDisplay blockDisplay = LogBlockMap.get(block.getType());
		if(blockDisplay == null) {
			return;
		}
		if(PlacedLocations.contains(block.getLocation())) {
			return;
		}
		PlacedLocations.add(block.getLocation());
    }
	
}
