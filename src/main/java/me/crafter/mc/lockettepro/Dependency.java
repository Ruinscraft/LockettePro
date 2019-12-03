package me.crafter.mc.lockettepro;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyPermission.ActionType;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scoreboard.Team;

public class Dependency {
    
    protected static WorldGuardPlugin worldguard = null;
    protected static Plugin towny = null;
    protected static Plugin vault = null;
    protected static Permission permission = null;

    public Dependency(Plugin plugin){
        // WorldGuard
        Plugin worldguardplugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldguardplugin == null || !(worldguardplugin instanceof WorldGuardPlugin)) {
            worldguard = null;
        } else {
            worldguard = (WorldGuardPlugin)worldguardplugin;
        }
        // Towny
        towny = plugin.getServer().getPluginManager().getPlugin("Towny");
        // Vault
        vault = plugin.getServer().getPluginManager().getPlugin("Vault");
        if (vault != null){
            RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
            permission = rsp.getProvider();
        }
    }
    
    public static boolean isProtectedFrom(Block block, Player player){
        if (worldguard != null) {
            if (!worldguard.createProtectionQuery().testBlockPlace(player, block.getLocation(), block.getType())) {
                return true;
            }
        }
        if (towny != null){
            try {
                if (TownyUniverse.getDataSource().getWorld(block.getWorld().getName()).isUsingTowny()){
                    // In town only residents can
                    if (!PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), ActionType.BUILD)) return true;
                    // Wilderness permissions
                    if (TownyUniverse.isWilderness(block)){ // It is wilderness here
                        if (!player.hasPermission("lockettepro.towny.wilds")) return true;
                    }
                }
            } catch (Exception e) {}
        }
        return false;
    }
    
    public static boolean isTownyTownOrNationOf(String line, Player player){
        if (towny != null){
            String name = player.getName();
            try {
                Resident resident = TownyUniverse.getDataSource().getResident(name);
                Town town = resident.getTown();
                if (line.equals("[" + town.getName() + "]")) return true;
                Nation nation = town.getNation();
                if (line.equals("[" + nation.getName() + "]")) return true;
            } catch (Exception e) {}
        }
        return false;
    }
    
    public static boolean isPermissionGroupOf(String line, Player player){
        if (vault != null){
            try {
                String[] groups = permission.getPlayerGroups(player);
                for (String group : groups){
                    if (line.equals("[" + group + "]")) return true;
                }
            } catch (Exception e){}
        }
        return false;
    }
    
    public static boolean isScoreboardTeamOf(String line, Player player){
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
        if (team != null){
            if (line.equals("[" + team.getName() + "]")) return true;
        }
        return false;
    }
}
