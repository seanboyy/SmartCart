package io.github.seanboyy.smartcart;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

public class CommandSetTag implements CommandExecutor{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        String help = "§6[Smart Cart] §cUsage: /scSetTag <Tag> -- sets tag of current cart to Tag";
        //if(!cmd.getName().equalsIgnoreCase("scsettag")) return false;
        if(!(sender instanceof Player)) {
            sender.sendMessage("Consoles can't be in vehicles");
            return true;
        }
        if(args.length < 1){
            ((Player) sender).sendRawMessage(help);
            return true;
        }
        if(((Player)sender).getVehicle() == null){
            ((Player) sender).sendRawMessage(help);
            return true;
        }
        if(!(((Player)sender).getVehicle() instanceof Minecart)){
            ((Player) sender).sendRawMessage("§6[Smart Cart] §cNon minecart vehicles aren't supported\n" + help);
            return true;
        }
        SmartCartVehicle cart = SmartCart.util.getCartFromList((((Player)sender).getVehicle()).getEntityId());
        SmartCartTrainVehicle trainCart = SmartCart.util.getTrainCartFromList((((Player)sender).getVehicle()).getEntityId());
        if(cart != null) cart.setTag(args[0]);
        if(trainCart != null) trainCart.setTag(args[0]);
        ((Player) sender).sendRawMessage("§6[Smart Cart] §7Set tag to §a" + args[0]);
        return true;
    }
}
