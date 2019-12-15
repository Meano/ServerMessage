package net.meano.ServerMessage;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

/**
 * @author Meano
 *
 */
public class ServerMessageMain extends JavaPlugin {
	
	private int BroadcastTaskId = -1;
	
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveConfig();

		if(getConfig().getBoolean("ClientMessages.Enable")){
			new ServerMessageListener(this);
			getLogger().info("Enable Client Messages");
		}

		if(getConfig().getBoolean("Broadcast.Enable")){
			int BroadcastPeriodSecond = getConfig().getInt("Broadcast.PeriodSeconds");
			BroadcastTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new BroadcastTask(this), new Random().nextInt(BroadcastPeriodSecond) * 20, BroadcastPeriodSecond * 20);
			if(BroadcastTaskId == -1) {
				getLogger().info("Create Broadcast Failed!!!");
			}
			getLogger().info("Enable Broadcast Messages");
		}
	}

	public void onDisable() {
		
	}
}
