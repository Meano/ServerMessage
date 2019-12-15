package net.meano.ServerMessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class BroadcastTask implements Runnable {
	
	public ServerMessageMain SMM;
	public boolean IsRandom = true;
	public int BroadcastCount = 0;
	
	public int MessagesIndex = 0;
	public int MessagesFrequence = 0;
	public int MessagesMinPlayer = 0;
	
	public int NotificationsIndex = 0;
	public int NotificationsFrequence = 0;
	
	public Random RandomSelect;
	
	public BroadcastTask(ServerMessageMain smm){
		SMM = smm;
		IsRandom = SMM.getConfig().getBoolean("Broadcast.Random");
		MessagesFrequence = SMM.getConfig().getInt("Broadcast.MessagesFrequence");
		MessagesMinPlayer = SMM.getConfig().getInt("Broadcast.MessagesMinPlayer");
		NotificationsFrequence = SMM.getConfig().getInt("Broadcast.NotificationsFrequence");
		RandomSelect = new Random();
	}

	public static Date getDateFromeString(String DateString) {
	   SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	   Date date = null;
		try {
			date = dateformat.parse(DateString);
		} catch (ParseException e) {
			return new Date();
		}
	   return date;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		String BroadcastMessage = null;
		if(BroadcastCount % NotificationsFrequence == 0) {
			List<LinkedHashMap<String, String>> Notifications = (List<LinkedHashMap<String, String>>) SMM.getConfig().getList("Broadcast.Notifications");
			if(Notifications.size() > 0) {
				NotificationsIndex = IsRandom ? RandomSelect.nextInt(Notifications.size()) : (NotificationsIndex++ % Notifications.size());
				LinkedHashMap<String, String> SelectNotification = Notifications.get(NotificationsIndex);
				BroadcastMessage = SelectNotification.get("Message");
				if((getDateFromeString(SelectNotification.get("Expire")).getTime() - new Date().getTime()) < 1000) {
					List<LinkedHashMap<String, String>> ExpireNotifications = (List<LinkedHashMap<String, String>>) SMM.getConfig().getList("Broadcast.ExpireNotifications");
					ExpireNotifications.add(SelectNotification);
					Notifications.remove(NotificationsIndex);
					SMM.getConfig().set("Broadcast.Notifications", Notifications);
					SMM.getConfig().set("Broadcast.ExpireNotifications", ExpireNotifications);
					SMM.saveConfig();
				}
			}
		}

		if(BroadcastMessage == null && BroadcastCount % MessagesFrequence == 0) {
			List<String> BufferMessages = SMM.getConfig().getStringList("Broadcast.Messages");
			if(BufferMessages.size() > 0) {
				MessagesIndex = IsRandom ? RandomSelect.nextInt(BufferMessages.size()) : (MessagesIndex++ % BufferMessages.size());
				BroadcastMessage = BufferMessages.get(MessagesIndex);
			}
		}

		if(BroadcastMessage != null && Bukkit.getOnlinePlayers().size() >= MessagesMinPlayer) {
			String[] MessageLines = BroadcastMessage.split("\\\\");
			for(String MessageLine : MessageLines) {
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', MessageLine));
			}
		}
		
		BroadcastCount++;
	}
}
