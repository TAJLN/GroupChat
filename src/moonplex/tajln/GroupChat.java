package moonplex.tajln;


import moonplex.tajln.commands.groupchatcommand;
import moonplex.tajln.utils.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class GroupChat extends JavaPlugin implements Listener
{
    private static GroupChat instance;
    private File customConfigFile;
    private static FileConfiguration customConfig;
    private File customConfigFile2;
    private static FileConfiguration customConfig2;
    private static ArrayList<UUID> clanchatoption = new ArrayList<>();

    public void onEnable(){

        Bukkit.getConsoleSender().sendMessage("\n  " +
                "  /$$$$$$  /$$$$$$$   /$$$$$$  /$$   /$$ /$$$$$$$         /$$$$$$  /$$   /$$  /$$$$$$  /$$$$$$$$\n" +
                " /$$__  $$| $$__  $$ /$$__  $$| $$  | $$| $$__  $$       /$$__  $$| $$  | $$ /$$__  $$|__  $$__/\n" +
                "| $$  \\__/| $$  \\ $$| $$  \\ $$| $$  | $$| $$  \\ $$      | $$  \\__/| $$  | $$| $$  \\ $$   | $$   \n" +
                "| $$ /$$$$| $$$$$$$/| $$  | $$| $$  | $$| $$$$$$$/      | $$      | $$$$$$$$| $$$$$$$$   | $$   \n" +
                "| $$|_  $$| $$__  $$| $$  | $$| $$  | $$| $$____/       | $$      | $$__  $$| $$__  $$   | $$   \n" +
                "| $$  \\ $$| $$  \\ $$| $$  | $$| $$  | $$| $$            | $$    $$| $$  | $$| $$  | $$   | $$   \n" +
                "|  $$$$$$/| $$  | $$|  $$$$$$/|  $$$$$$/| $$            |  $$$$$$/| $$  | $$| $$  | $$   | $$   \n" +
                " \\______/ |__/  |__/ \\______/  \\______/ |__/             \\______/ |__/  |__/|__/  |__/   |__/   \n" +
                "                                                                                                \n" +
                "                                                                                                \n" +
                "/$$$$$$$  /$$     /$$       /$$$$$$$$ /$$$$$$     /$$$$$ /$$       /$$   /$$\n" +
                "| $$__  $$|  $$   /$$/      |__  $$__//$$__  $$   |__  $$| $$      | $$$ | $$\n" +
                "| $$  \\ $$ \\  $$ /$$/          | $$  | $$  \\ $$      | $$| $$      | $$$$| $$\n" +
                "| $$$$$$$   \\  $$$$/           | $$  | $$$$$$$$      | $$| $$      | $$ $$ $$\n" +
                "| $$__  $$   \\  $$/            | $$  | $$__  $$ /$$  | $$| $$      | $$  $$$$\n" +
                "| $$  \\ $$    | $$             | $$  | $$  | $$| $$  | $$| $$      | $$\\  $$$\n" +
                "| $$$$$$$/    | $$             | $$  | $$  | $$|  $$$$$$/| $$$$$$$$| $$ \\  $$\n" +
                "|_______/     |__/             |__/  |__/  |__/ \\______/ |________/|__/  \\__/\n" +
                "                                                                             \n" +
                "                                                                             \n" +
                "                                                                             ");

        instance = this;

        customConfigFile = new File(getDataFolder(), "groups.yml");
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

        customConfigFile2 = new File(getDataFolder(), "players.yml");
        customConfig2 = YamlConfiguration.loadConfiguration(customConfigFile2);

        saveconfig();

        CommandManager cm = new CommandManager(this);
        cm.registerCommand(new groupchatcommand(cm));

        getServer().getPluginManager().registerEvents(this, this);

    }

    public void saveconfig(){
        try {
            customConfig.save(customConfigFile);
            customConfig2.save(customConfigFile2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GroupChat getPlugin() {
        return instance;
    }

    public static FileConfiguration getPlayers(){
        return customConfig2;
    }

    public static FileConfiguration getGroups(){
        return customConfig;
    }



    public static boolean on (Player p){
        return clanchatoption.contains(p.getUniqueId());
    }

    public static void enable(Player p){
        clanchatoption.add(p.getUniqueId());
    }

    public static void disable(Player p){
        clanchatoption.remove(p.getUniqueId());
    }


    @EventHandler
    public void joinEvent(PlayerJoinEvent e){
        if (!customConfig2.isSet(e.getPlayer().getUniqueId().toString())){
            customConfig2.set(e.getPlayer().getUniqueId().toString(), "NONE");
            saveconfig();
        }
        if (on(e.getPlayer()))
            disable(e.getPlayer());
    }

    @EventHandler
    public void chatFormat(AsyncPlayerChatEvent event){
        if (customConfig2.getString(event.getPlayer().getUniqueId().toString()).equals("NONE"))
            disable(event.getPlayer());

        if (on(event.getPlayer())) {
            event.setCancelled(true);
            String clanname = customConfig2.getString(event.getPlayer().getUniqueId().toString());
            Map<String, Object> values = customConfig2.getValues(true);
            for (String uuid : values.keySet()){
                Player p = Bukkit.getPlayer(UUID.fromString(uuid));
                if (p != null && customConfig2.getString(uuid).equals(clanname)){
                    p.sendMessage("§9GroupChat> §f" + event.getPlayer().getName() + ": " + event.getMessage());
                }
            }
        }
    }
}

