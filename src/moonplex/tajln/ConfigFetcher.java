package moonplex.tajln;

import moonplex.tajln.utils.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ConfigFetcher {
    private static FileConfiguration groups = GroupChat.getGroups();

    private static FileConfiguration players = GroupChat.getPlayers();


    public static String getGroup(UUID uuid) {
        return players.getString(uuid.toString());
    }

    public static boolean exists(String groupname) {
        return groups.isSet(groupname + ".owner");
    }

    public static void setOwner(String groupname, UUID uuid) {
        groups.set(groupname + ".owner", uuid.toString());
        GroupChat.getPlugin().saveconfig();
    }

    public static void setGroup(UUID uuid, String groupname) {
        players.set(uuid.toString(), groupname);
        GroupChat.getPlugin().saveconfig();
    }

    public static boolean isOwner(String groupname, UUID uuid) {
        return groups.getString(groupname + ".owner").equals(uuid.toString());
    }

    public static void removeGroup(String groupname) {
        groups.set(groupname, null);
        GroupChat.getPlugin().saveconfig();
    }

    public static void messageOtherMembers(Player sender, String message){
        Map<String, Object> values = players.getValues(true);
        for (String uuids : values.keySet()){
            if (getGroup(UUID.fromString(uuids)).equals(getGroup(sender.getUniqueId()))) {
                players.set(uuids, "NONE");
                Player p = Bukkit.getPlayer(UUID.fromString(uuids));
                if (p != null && !uuids.equals(sender.getUniqueId().toString()))
                    p.sendMessage(message);
            }
        }
    }

    public static boolean ismember(UUID uuid, String groupname){
        Map<String, Object> values = players.getValues(true);
        for (String uuids : values.keySet()){
            if (uuid.toString().equals(uuids)){
                if (players.getString(uuids).equals(groupname))
                    return true;
            }
        }
        return false;
    }

    public static void messageMembers(String groupname, String message){
        Map<String, Object> values = players.getValues(true);
        for (String uuid : values.keySet()){
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));
            if (p != null && players.getString(uuid).equals(groupname)){
                p.sendMessage(message);
            }
        }
    }
}
