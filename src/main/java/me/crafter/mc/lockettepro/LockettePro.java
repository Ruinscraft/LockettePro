package me.crafter.mc.lockettepro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class LockettePro extends JavaPlugin {

    private static Plugin plugin;
    private boolean debug = false;
    private static Version version = Version.UNKNOWN;
    private static boolean needcheckhand = true;

    public void onEnable(){
        // Version
        String versionname = "v" + Bukkit.getServer().getClass().getPackage().getName().split("v")[1];
        try {
            version = Version.valueOf(versionname);
        } catch (Exception ex) {
            version = Version.UNKNOWN;
            getLogger().warning("===================================");
            getLogger().warning("Unsupported server version: " + Bukkit.getBukkitVersion());
            try {
                Tag.ANVIL.getValues();
                Material.TRIDENT.isItem();
            } catch (Exception e) {
                setEnabled(false);
                getLogger().warning("This plugin is not compatible with your server version!");
            }
            getLogger().warning("===================================");
            if (!isEnabled()) {
                return;
            }
        }
        plugin = this;
        // Read config
        new Config(this);
        // Register Listeners
        // If debug mode is not on, debug listener won't register
        if (debug) getServer().getPluginManager().registerEvents(new BlockDebugListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlayerListener(), this);
        getServer().getPluginManager().registerEvents(new BlockEnvironmentListener(), this);
        getServer().getPluginManager().registerEvents(new BlockInventoryMoveListener(), this);
        // Dependency
        new Dependency(this);
        // If UUID is not enabled, UUID listener won't register
        if (Config.isUuidEnabled() || Config.isLockExpire()){
            if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null){
                DependencyProtocolLib.setUpProtocolLib(this);
            } else {
                plugin.getLogger().info("ProtocolLib is not found!");
                plugin.getLogger().info("UUID & expiracy support requires ProtocolLib, or else signs will be ugly!");
            }
        }
    }
    
    public void onDisable(){
        if (Config.isUuidEnabled() && Bukkit.getPluginManager().getPlugin("ProtocolLib") != null){
            DependencyProtocolLib.cleanUpProtocolLib(this);
        }
    }
    
    public static Plugin getPlugin(){
        return plugin;
    }
    
    public static boolean needCheckHand(){
        return needcheckhand;
    }
    
    public static Version getBukkitVersion(){
        return version;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> commands = new ArrayList<>();
        commands.add("reload");
        commands.add("version");
        commands.add("1");
        commands.add("2");
        commands.add("3");
        commands.add("4");
        commands.add("uuid");
        commands.add("update");
        commands.add("debug");
        if (args != null && args.length == 1) {
            List<String> list = new ArrayList<>();
            for (String s : commands) {
                if (s.startsWith(args[0])) {
                    list.add(s);
                }
            }
            return list;
        }
        return null;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, final String[] args){
        if (cmd.getName().equals("lockettepro")){
            if (args.length == 0){
                Utils.sendMessages(sender, Config.getLang("command-usage"));
            } else {
                // The following commands does not require player
                switch (args[0]){
                case "reload":
                    if (sender.hasPermission("lockettepro.reload")){
                        if(Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
                            DependencyProtocolLib.cleanUpProtocolLib(this);
                        }
                        Config.reload();
                        if (Config.isUuidEnabled() && Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
                            DependencyProtocolLib.setUpProtocolLib(this);
                        }
                        Utils.sendMessages(sender, Config.getLang("config-reloaded"));
                    } else {
                        Utils.sendMessages(sender, Config.getLang("no-permission"));
                    }
                    return true;
                case "version":
                    if (sender.hasPermission("lockettepro.version")){
                        sender.sendMessage(plugin.getDescription().getFullName());
                    } else {
                        Utils.sendMessages(sender, Config.getLang("no-permission"));
                    }
                    return true;
                    case "debug":
                        // This is not the author debug, this prints out info
                        if (sender.hasPermission("lockettepro.debug")) {
                            sender.sendMessage("LockettePro Debug Message");
                            // Basic
                            sender.sendMessage("LockettePro: " + getDescription().getVersion());
                            // Version
                            sender.sendMessage("Bukkit: " + "v" + Bukkit.getServer().getClass().getPackage().getName().split("v")[1] + " / LockettePro: " + version);
                            sender.sendMessage("Server version: " + Bukkit.getVersion());
                            // Config
                            sender.sendMessage("UUID: " + Config.isUuidEnabled());
                            sender.sendMessage("Expire: " + Config.isLockExpire() + " " + (Config.isLockExpire() ? Config.getLockExpireDays() : ""));
                            // ProtocolLib
                            sender.sendMessage("ProtocolLib info:");
                            if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
                                sender.sendMessage(" - ProtocolLib missing");
                            } else {
                                sender.sendMessage(" - ProtocolLib: " + Bukkit.getPluginManager().getPlugin("ProtocolLib").getDescription().getVersion());
                            }
                            // Other
                            sender.sendMessage("Linked plugins:");
                            boolean linked = false;
                            if (Dependency.worldguard != null) {
                                linked = true;
                                sender.sendMessage(" - Worldguard: " + Dependency.worldguard.getDescription().getVersion());
                            }
                            if (Dependency.towny != null) {
                                linked = true;
                                sender.sendMessage(" - Towny: " + Dependency.towny.getDescription().getVersion());
                            }
                            if (Dependency.vault != null) {
                                linked = true;
                                sender.sendMessage(" - Vault: " + Dependency.vault.getDescription().getVersion());
                            }
                            if (!linked) {
                                sender.sendMessage(" - none");
                            }
                        } else {
                            Utils.sendMessages(sender, Config.getLang("no-permission"));
                        }
                        return true;
                }
                // The following commands requires player
                if (!(sender instanceof Player)){
                    Utils.sendMessages(sender, Config.getLang("command-usage"));
                    return false;
                }
                Player player = (Player)sender;
                switch (args[0]){
                case "1":
                case "2":
                case "3":
                case "4":
                    if (player.hasPermission("lockettepro.edit")){
                        String message = "";
                        Block block = Utils.getSelectedSign(player);
                        if (block == null){
                            Utils.sendMessages(player, Config.getLang("no-sign-selected"));
                        } else if (!LocketteProAPI.isSign(block) || !(player.hasPermission("lockettepro.edit.admin") || LocketteProAPI.isOwnerOfSign(block, player))){
                            Utils.sendMessages(player, Config.getLang("sign-need-reselect"));
                        } else {
                            for (int i = 1; i < args.length; i++){
                                message += args[i];
                            }
                            message = ChatColor.translateAlternateColorCodes('&', message);
                            if (!player.hasPermission("lockettepro.admin.edit") && !debug && message.length() > 18) {
                                Utils.sendMessages(player, Config.getLang("line-is-too-long"));
                                return true;
                            }
                            if (LocketteProAPI.isLockSign(block)){
                                switch (args[0]){
                                case "1":
                                    if (!debug || !player.hasPermission("lockettepro.admin.edit")){
                                        Utils.sendMessages(player, Config.getLang("cannot-change-this-line"));
                                        break;
                                    }
                                case "2":
                                    if (!player.hasPermission("lockettepro.admin.edit")){
                                        Utils.sendMessages(player, Config.getLang("cannot-change-this-line"));
                                        break;
                                    }
                                case "3":
                                case "4":
                                    Utils.setSignLine(block, Integer.parseInt(args[0])-1, message);
                                    Utils.sendMessages(player, Config.getLang("sign-changed"));
                                    if (Config.isUuidEnabled()){
                                        Utils.updateUuidByUsername(Utils.getSelectedSign(player), Integer.parseInt(args[0])-1);
                                    }
                                    break;
                                }
                            } else if (LocketteProAPI.isAdditionalSign(block)){
                                switch (args[0]){
                                case "1":
                                    if (!debug || !player.hasPermission("lockettepro.admin.edit")){
                                        Utils.sendMessages(player, Config.getLang("cannot-change-this-line"));
                                        break;
                                    }
                                case "2":
                                case "3":
                                case "4":
                                    Utils.setSignLine(block, Integer.parseInt(args[0])-1, message);
                                    Utils.sendMessages(player, Config.getLang("sign-changed"));
                                    if (Config.isUuidEnabled()){
                                        Utils.updateUuidByUsername(Utils.getSelectedSign(player), Integer.parseInt(args[0])-1);
                                    }
                                    break;
                                }
                            } else {
                                Utils.sendMessages(player, Config.getLang("sign-need-reselect"));
                            }
                        }
                    } else {
                        Utils.sendMessages(player, Config.getLang("no-permission"));
                    }
                    break;
                case "force":
                    if (debug && player.hasPermission("lockettepro.debug")){
                        Utils.setSignLine(Utils.getSelectedSign(player), Integer.parseInt(args[1]), args[2]);
                        break;
                    }
                case "update":
                    if (debug && player.hasPermission("lockettepro.debug")){
                        Utils.updateSign(Utils.getSelectedSign(player));
                        break;
                    }
                case "uuid":
                    if (debug && player.hasPermission("lockettepro.debug")){
                        Utils.updateUuidOnSign(Utils.getSelectedSign(player));
                        break;
                    }
                default:
                    Utils.sendMessages(player, Config.getLang("command-usage"));
                    break;
                }
            }
        }
        return true;
    }
    
}
