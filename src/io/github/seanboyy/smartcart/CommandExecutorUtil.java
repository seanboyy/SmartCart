// smartcart copyright 2015 Ian Clark
//
// Distributed under the MIT License
// http://opensource.org/licenses/MIT
//
package io.github.seanboyy.smartcart;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;

public class CommandExecutorUtil implements CommandExecutor {

    SmartCart plugin;

    CommandExecutorUtil(SmartCart plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String commandHelp = "Usage:\n"
                + "   /sc list [<world>]  -- List all minecarts (world optional)\n"
                + "   /sc kill <id>  -- Destroy the specified minecart\n"
                + "   /sc killall [<world>]  -- Destroy all minecarts (world optional)";
        String badWorld = "The world you specified does not exist.  Valid worlds are: "
                + SmartCart.util.getWorldList(", ");
        if (!cmd.getName().equalsIgnoreCase("sc")) return false;
        if (!(sender instanceof Player)) {
            sender.sendMessage("SmartCart console control not yet implemented :(");
            return true;
        }
        else {
            int argSize = args.length;
            if (argSize == 1 || argSize == 2) {
                switch (args[0]) {
                    case "list":
                        if (argSize == 1) {
                            SmartCart.util.sendFullCartList(SmartCart.util.getCartList(), SmartCart.util.getTrainCartList(), (Entity) sender);
                            return true;
                        } else {
                            // Try to get the requested world
                            World world = Bukkit.getWorld(args[1]);
                            // If the world specified doesn't exist, let them know
                            if (world == null) {
                                SmartCart.util.sendMessage((Entity) sender, badWorld);
                                return true;
                            }
                            // Since it does exist, list the carts in it
                            SmartCart.util.sendFullCartList(SmartCart.util.getCartList(world), SmartCart.util.getTrainCartList(world), (Entity) sender);
                            return true;
                        }
                    case "listtrain":
                        if(argSize == 1){
                            SmartCart.util.sendTrainCartList(SmartCart.util.getTrainCartList(), (Entity)sender);
                            return true;
                        }
                        else{
                            World world = Bukkit.getWorld(args[1]);
                            if(world == null){
                                SmartCart.util.sendMessage((Entity)sender, badWorld);
                                return true;
                            }
                            SmartCart.util.sendTrainCartList(SmartCart.util.getTrainCartList(world), (Entity)sender);
                            return true;
                        }
                    case "listgeneric":
                        if(argSize == 1){
                            SmartCart.util.sendCartList(SmartCart.util.getCartList(), (Entity)sender);
                            return true;
                        }
                        else{
                            World world = Bukkit.getWorld(args[1]);
                            if(world == null){
                                SmartCart.util.sendMessage((Entity)sender, badWorld);
                                return true;
                            }
                            SmartCart.util.sendCartList(SmartCart.util.getCartList(world), (Entity)sender);
                            return true;
                        }

                    case "kill":
                        // If argSize is 1 the user didn't supply an ID to remove
                        if (argSize == 1) {
                            SmartCart.util.sendMessage((Entity) sender, "Usage: /sc kill <id> -- you "
                                    + "can find a cart's id using '/sc list [<world>]', or kill all carts "
                                    + "with '/sc killall [<world>]'");
                            return true;
                        }
                        // If argSize is 2, we have the right # of arguments, so check to make sure ID is an
                        //   int and the ID references a known cart, then kill it with fire.
                        else {
                            if (!SmartCartUtil.isInteger(args[1])) {
                                SmartCart.util.sendMessage((Entity) sender, "Error: Cart ID must be an integer!");
                                return true;
                            }
                            SmartCartVehicle cart = SmartCart.util.getCartFromList(Integer.parseInt(args[1]));
                            SmartCartTrainVehicle trainCart = SmartCart.util.getTrainCartFromList(Integer.parseInt(args[1]));
                            if (cart == null && trainCart == null) {
                                SmartCart.util.sendMessage((Entity) sender, "Error: Can't find that cart ID :(");
                                return true;
                            }
                            SmartCart.util.sendMessage((Entity) sender, "Removing cart " + args[1]);
                            if(cart != null) cart.remove(true);
                            if(trainCart != null) trainCart.remove(true);
                            return true;
                        }
                    case "killtrain":
                        if(argSize == 1){
                            SmartCart.util.sendMessage((Entity)sender, "Usage: /sc killtrain <id-of-lead-cart> -- you can find" +
                                    "a cart's id and whether it is a lead cart with /sc listtrain [<world>], or kill all carts with '/sc " +
                                    "killall {<world>]");
                            return true;
                        }
                        else{
                            if(!SmartCartUtil.isInteger(args[1])){
                                SmartCart.util.sendMessage((Entity)sender, "Error: Cart ID mus be an integer!");
                                return true;
                            }
                            SmartCartTrainVehicle cart = SmartCart.util.getTrainCartFromList(Integer.parseInt(args[1]));
                            if(cart == null){
                                SmartCart.util.sendMessage((Entity)sender, "Error: Can't find that cart ID :(");
                                return true;
                            }
                            if(!cart.isLeadCart()){
                                SmartCart.util.sendMessage((Entity)sender, "Error: not a lead cart");
                                return true;
                            }
                            SmartCart.util.sendMessage((Entity)sender, "Removing train with lead cart " + args[1]);
                            SmartCart.util.killTrain(SmartCart.util.getTrain(Integer.parseInt(args[1])));
                            return true;
                        }
                    case "killall":
                        if (argSize == 1) {
                            SmartCart.util.sendMessage((Entity) sender, "Removing all carts on the server!");
                            SmartCart.util.killCarts(SmartCart.util.getCartList());
                            for(SmartCartTrain train : SmartCart.util.getTrainList()){
                                SmartCart.util.killTrain(train);
                            }
                            return true;
                        } else {
                            // Try to get the requested world
                            World world = Bukkit.getWorld(args[1]);
                            // If the world specified doesn't exist, let them know
                            if (world == null) {
                                SmartCart.util.sendMessage((Entity) sender, badWorld);
                                return true;
                            }
                            // Since it does exist, remove the carts in it
                            SmartCart.util.sendMessage((Entity) sender, "Removing all carts in world: " + world.getName());
                            SmartCart.util.killCarts(SmartCart.util.getCartList(world));
                            for(SmartCartTrain train : SmartCart.util.getTrainList(world)){
                                SmartCart.util.killTrain(train);
                            }
                            return true;
                        }
                }
            }
            // If all else fails, return false!
            SmartCart.util.sendMessage((Entity) sender, commandHelp);
            return true;
        }
    }
}
