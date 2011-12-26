// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.commandbook.commands;

import com.sk89q.commandbook.CommandBook;
import com.sk89q.commandbook.locations.NamedLocation;
import com.sk89q.commandbook.util.LocationUtil;
import com.sk89q.commandbook.util.PlayerUtil;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HomeManagementCommands {

    private CommandBook plugin;
    
    public HomeManagementCommands(CommandBook plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = {"del", "delete", "remove", "rem"},
             usage = "[homename] [world]", desc = "Remove a home", min = 0, max = 2 )
    @CommandPermissions({"commandbook.home.remove"})
    public void remove(CommandContext args, CommandSender sender) throws CommandException {
        World world;
        String homeName = sender.getName();
        if (args.argsLength() == 2) {
            world = LocationUtil.matchWorld(sender, args.getString(1));
        } else {
            world = PlayerUtil.checkPlayer(sender).getWorld();
        }
        if (args.argsLength() > 0) homeName = args.getString(0);
        NamedLocation home = plugin.getHomesManager().get(world, homeName);
        if (home == null) {
            throw new CommandException("No home found for " + homeName + " in world " + world.getName());
        }
        if (!home.getCreatorName().equals(sender.getName())) {
            plugin.checkPermission(sender, "commandbook.home.remove.other");
        }

        plugin.getHomesManager().remove(world, homeName);
        sender.sendMessage(ChatColor.YELLOW + "Home for " + homeName + " removed.");
    }

    private static final int PER_PAGE = 5;
    @Command(aliases = {"list", "show"}, usage = "[-w world] [page]", desc = "List homes",
            flags = "w:", min = 0, max = 1 )
    @CommandPermissions({"commandbook.home.list"})
    public void list(CommandContext args, CommandSender sender) throws CommandException {
        World world = null;
        int page = args.getInteger(0, 1) - 1;
        if (plugin.getWarpsManager().isPerWorld()) {
            if (args.hasFlag('w')) {
                world = LocationUtil.matchWorld(sender, args.getFlag('w'));
            } else {
                world = PlayerUtil.checkPlayer(sender).getWorld();
            }
            if (world == null) throw new CommandException("Error finding world to use!");
        }
        List<NamedLocation> locations = plugin.getHomesManager().getLocations(world);
        if (locations.size() == 0) throw new CommandException("No homes match!");

        int maxPages = locations.size() / PER_PAGE;
        if (page < 0 || page > maxPages) throw new CommandException(
                "Unknown page selected! " + maxPages + " total pages.");
        String defaultWorld = plugin.getServer().getWorlds().get(0).getName();
        sender.sendMessage(ChatColor.YELLOW +
                "Owner - World  - Location (page " + (page + 1) + "/" + (maxPages + 1) + ")");
        for (int i = PER_PAGE * page; i < PER_PAGE * page + PER_PAGE  && i < locations.size(); i++) {
            NamedLocation loc = locations.get(i);
            sender.sendMessage(ChatColor.YELLOW.toString() + loc.getCreatorName()
                    + " - " + (loc.getWorldName() == null ? defaultWorld : loc.getWorldName())
                    + " - " + loc.getLocation().getBlockX() + "," + loc.getLocation().getBlockY()
                    + "," + loc.getLocation().getBlockZ());
        }
    }

}
