package moonplex.tajln.commands;

import moonplex.tajln.ConfigFetcher;
import moonplex.tajln.GroupChat;
import moonplex.tajln.utils.CommandInfo;
import moonplex.tajln.utils.CommandManager;
import moonplex.tajln.utils.SimpleCommand;
import moonplex.tajln.utils.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CommandInfo(description="GroupChat command", usage=" /<command>", permission="groupchat.command", onlyIngame = true)
public class groupchatcommand extends SimpleCommand {

    private String prefix = "§9GroupChat> §f";

    private Map<Player, String> invites = new HashMap<>();

    public groupchatcommand(CommandManager commandManager) {
        super(commandManager, "groupchat");

    }

    @Override
    public boolean onCommand(CommandSender caller, String paramString, String[] args) {
        Player sender = (Player) caller;
        if (args.length != 0)
            switch (args[0]) {
                case "create":
                    createcommand(sender, args);
                    return true;
                case "delete":
                    deletecommand(sender, args);
                    return true;
                case "invite":
                    invitecommand(sender, args);
                    return true;
                case "leave":
                    leavecommand(sender);
                    return true;
                case "kick":
                    kickcommand(sender, args);
                    return true;
                case "enable":
                    enablecommand(sender);
                    return true;

            }
        sender.sendMessage(prefix + "/groupchat enable");
        sender.sendMessage(prefix + "/groupchat create <GROUPNAME>");
        sender.sendMessage(prefix + "/groupchat delete <GROUPNAME>");
        sender.sendMessage(prefix + "/groupchat invite <PLAYERNAME>");
        sender.sendMessage(prefix + "/groupchat kick <PLAYERNAME>");
        sender.sendMessage(prefix + "/groupchat leave");
        return true;
    }

    private void enablecommand(Player sender){

        UUID uuid = sender.getUniqueId();

        if (ConfigFetcher.getGroup(uuid).equals("NONE")) {
            sender.sendMessage(prefix + "You have to be in a group first");
            return;
        }

        if (!GroupChat.on(sender)) {
            sender.sendMessage(prefix + "You have §aenabled §fgroup chat for yourself");
            GroupChat.enable(sender);
        } else {
            sender.sendMessage(prefix + "You have §cdisabled §fgroup chat for yourself");
            GroupChat.disable(sender);
        }
    }

    private void createcommand(Player sender, String[] args){

        UUID uuid = sender.getUniqueId();

        if (!ConfigFetcher.getGroup(sender.getUniqueId()).equals("NONE")){
            sender.sendMessage(prefix + "You are already in a group");
            return;
        }

        if (args.length == 1){
            sender.sendMessage(prefix + "You have to provide a group name");
            return;
        }
        if (!ConfigFetcher.exists(args[1])){
            ConfigFetcher.setOwner(args[1], uuid);
            ConfigFetcher.setGroup(uuid, args[1]);
            sender.sendMessage(prefix + "Group §b" + args[1] + " §fwas created");
            GroupChat.getPlugin().saveconfig();
            return;
        }

        sender.sendMessage(prefix + "Group §c" + args[1] + " §falready exists");
    }

    private void deletecommand(Player sender, String[] args){

        UUID uuid = sender.getUniqueId();

        if (args.length == 1){
            sender.sendMessage(prefix + "You have to provide a group name");
            return;
        }

        String groupname = args[1];

        if (ConfigFetcher.exists(groupname)){
            if (ConfigFetcher.isOwner(groupname, uuid)) {
                ConfigFetcher.removeGroup(groupname);
                ConfigFetcher.setGroup(uuid, "NONE");
                sender.sendMessage(prefix + "Group §b" + groupname + " §fwas deleted");
                ConfigFetcher.messageOtherMembers(sender, prefix + "The group you were currently in has been deleted");

            }else {
                sender.sendMessage(prefix + "Group §c" + groupname + " §fis not owned by you");
            }
            return;
        }

        sender.sendMessage(prefix + "Group §c" + groupname + " §fdoes not exists");
    }

    private void invitecommand(Player sender, String[] args){
        if (args.length == 1){
            sender.sendMessage(prefix + "/groupchat invite <PLAYERNAME>");
            return;
        }

        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();


        if (args[1].equals("accept")){
            if (!invites.containsKey(sender)) {
                sender.sendMessage(prefix + "You currently have no active group invites");
            } else{
                scheduler.cancelTasks(getPlugin());
                sender.sendMessage(prefix + "You have joined group §b" + invites.get(sender));
                ConfigFetcher.setGroup(sender.getUniqueId(), invites.get(sender));
                invites.remove(sender);
                ConfigFetcher.messageOtherMembers(sender,prefix + "§a" + sender.getName() + " §fhas joined your group");
            }
            return;

        }

        if (ConfigFetcher.getGroup(sender.getUniqueId()).equals("NONE")) {
            sender.sendMessage(prefix + "You have to be in a group first");
            return;
        }

        Player p = Bukkit.getPlayer(args[1]);

        if (p == null){
            sender.sendMessage(prefix + "You have to provide a name of an online player");
            return;
        }

        if (!ConfigFetcher.getGroup(p.getUniqueId()).equals("NONE")){
            sender.sendMessage(prefix + "This player is already in a group");
            return;
        }

        if (invites.containsKey(p)){
            sender.sendMessage(prefix + "This player already has a pending invite");
            return;
        }

        invites.put(p, ConfigFetcher.getGroup(p.getUniqueId()));
        sender.sendMessage(prefix + "You have invited §a" + p.getName() + " §fto the group §b" + ConfigFetcher.getGroup(sender.getUniqueId()));
        p.sendMessage(prefix + "§a" + sender.getName() + " §fhas invited you to the group §b" + ConfigFetcher.getGroup(sender.getUniqueId()));
        p.sendMessage(prefix + "You have 60 seconds to accept the invite by typing §a§l/groupchat invite accept");


        scheduler.scheduleSyncDelayedTask(getPlugin(), () -> {
            if (invites.containsKey(p)) {
                sender.sendMessage(prefix + "Invite timer ran out");
                p.sendMessage(prefix + "Invite timer ran out");
                invites.remove(p);
            }
        }, 1200);
    }

    private void leavecommand(Player sender){
        if (ConfigFetcher.getGroup(sender.getUniqueId()).equals("NONE")) {
            sender.sendMessage(prefix + "You have to be in a group first");
            return;
        }

        String groupname = ConfigFetcher.getGroup(sender.getUniqueId());

        if (ConfigFetcher.isOwner(groupname, sender.getUniqueId())) {
            sender.sendMessage(prefix + "You can't the group as owner");
            sender.sendMessage(prefix + "Delete it with /groupchat delete §b" + groupname);
            return;
        }

        ConfigFetcher.messageOtherMembers(sender, prefix + "§a" + sender.getName() + " §fhas left your group");

        sender.sendMessage(prefix + "You have left the group §b" + groupname);
        ConfigFetcher.setGroup(sender.getUniqueId(), "NONE");

    }

    private void kickcommand(Player sender, String[] args){
        if (ConfigFetcher.getGroup(sender.getUniqueId()).equals("NONE")) {
            sender.sendMessage(prefix + "You have to be in a group first");
            return;
        }

        if (args.length == 1){
            sender.sendMessage(prefix + "You have to provide the name of a player you want to kick");
            return;
        }

        String groupname = ConfigFetcher.getGroup(sender.getUniqueId());

        if (ConfigFetcher.exists(groupname)){
            if (ConfigFetcher.isOwner(groupname, sender.getUniqueId())){

                boolean ismember = ConfigFetcher.ismember(UUIDFetcher.getUUID(args[1]), groupname);

                if (!ismember){
                    sender.sendMessage(prefix + "Player §a" + args[1] + " §f is not a member of your group");
                    return;
                }

                if (UUIDFetcher.getUUID(args[1]).equals(sender.getUniqueId())){
                    sender.sendMessage(prefix + "You can't kick yourself out of the group");
                    return;
                }


                Player p = Bukkit.getPlayer(args[1]);
                if (p != null)
                    p.sendMessage(prefix + "You have been kicked out the group §b" + groupname);
                ConfigFetcher.setGroup(UUIDFetcher.getUUID(args[1]), "NONE");
                GroupChat.getPlugin().saveconfig();

                ConfigFetcher.messageMembers(groupname, prefix + "§a" + args[1] + " §fhas been kicked out of your group");

            } else {
                sender.sendMessage(prefix + "Group §c" + args[1] + " §fis not owned by you");
            }
        }
    }



}
