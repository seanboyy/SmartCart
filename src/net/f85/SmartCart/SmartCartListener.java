//
// SmartCart copyright 2015 Ian Clark
//
// Distributed under the MIT License
// http://opensource.org/licenses/MIT
//
package net.f85.SmartCart;

import org.bukkit.block.Sign;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.*;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.block.Block;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class SmartCartListener implements Listener {


    private net.f85.SmartCart.SmartCart plugin;


    SmartCartListener(net.f85.SmartCart.SmartCart plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onVehicleUpdate(VehicleUpdateEvent event) {
        Vehicle vehicle = event.getVehicle();
        // Return if vehicle is not a minecart
        if (!(vehicle instanceof Minecart)) return;
        net.f85.SmartCart.SmartCartVehicle cart = SmartCart.util.getCartFromList((Minecart) vehicle);
        cart.saveCurrentLocation();
        if (cart.getCart().getPassengers().isEmpty()) cart.setEmptyCartTimer();
        else cart.resetEmptyCartTimer();
        // Return if minecart is marked for removal, or off rails for any reason
        if (cart.getCart().isDead() || cart.isNotOnRail()) return;
        // Return if it isn't a player in the cart
        if (cart.getCart().getPassengers().isEmpty() || cart.getCart().getPassengers().get(0) != null && cart.getCart().getPassengers().get(0).getType() != EntityType.PLAYER) return;
        if (cart.isNewBlock()) cart.readControlSign();
        if (cart.isOnControlBlock()) cart.executeControl();
        else {
            cart.setPreviousMaterial(null);
            cart.setSpeed(cart.getConfigSpeed());
        }
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (!(vehicle instanceof Minecart)) return;
        SmartCartVehicle cart = SmartCart.util.getCartFromList((Minecart) vehicle);
        if (cart.getCart().isDead() || cart.isNotOnRail()) return;
        if (cart.isHeld()) vehicle.teleport(event.getFrom());
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (!(vehicle instanceof Minecart)) return;
        SmartCartVehicle cart = SmartCart.util.getCartFromList((Minecart) vehicle);
        if (cart.getCart().isDead() || cart.isNotOnRail()) return;
        if (cart.isLocked()) event.setCancelled(true);
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        Vehicle vehicle = event.getVehicle();
        // Return if vehicle is not a minecart
        if (!(vehicle instanceof Minecart)) return;
        // Return if it wasn't a player that entered
        if (event.getEntered().getType() != EntityType.PLAYER) return;
        SmartCartVehicle cart = SmartCart.util.getCartFromList((Minecart) vehicle);
        // Return if minecart is marked for removal, or off rails for any reason
        if (cart.getCart().isDead() || cart.isNotOnRail()) return;
    }


    @EventHandler
    public void onVehicleDestroyed(VehicleDestroyEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (vehicle instanceof Minecart) SmartCart.util.getCartFromList((Minecart) vehicle).remove(false);
    }


    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        // Return if the redstone current is turning off instead of on
        if (event.getOldCurrent() > event.getNewCurrent()) {
            if (SmartCart.util.isSign(event.getBlock())) {
                Sign sign = (Sign) event.getBlock().getState();
                List<Pair<String, String>> commands = SmartCartVehicle.parseSign(sign);
                for (Pair<String, String> command : commands)
                    if (command.left().equals("$HOLD")) {
                        Block block1 = event.getBlock().getLocation().add(0, 2, 0).getBlock();
                        Block block2 = event.getBlock().getLocation().add(-1, 1, 0).getBlock();
                        Block block3 = event.getBlock().getLocation().add(1, 1, 0).getBlock();
                        Block block4 = event.getBlock().getLocation().add(0, 1, -1).getBlock();
                        Block block5 = event.getBlock().getLocation().add(-1, 1, 1).getBlock();
                        Block block6 = event.getBlock().getLocation().add(-1, 0, 0).getBlock();
                        Block block7 = event.getBlock().getLocation().add(1, 0, 0).getBlock();
                        Block block8 = event.getBlock().getLocation().add(0, 0, -1).getBlock();
                        Block block9 = event.getBlock().getLocation().add(0, 0, 1).getBlock();
                        Minecart minecart;
                        if ((minecart = SmartCart.util.getCartAtBlock(block1)) != null) {
                            SmartCartVehicle cart = SmartCart.util.getCartFromList(minecart);
                            cart.setHeld(false);
                        }
                        if ((minecart = SmartCart.util.getCartAtBlock(block2)) != null) {
                            SmartCartVehicle cart = SmartCart.util.getCartFromList(minecart);
                            cart.setHeld(false);
                        }
                        if ((minecart = SmartCart.util.getCartAtBlock(block3)) != null) {
                            SmartCartVehicle cart = SmartCart.util.getCartFromList(minecart);
                            cart.setHeld(false);
                        }
                        if ((minecart = SmartCart.util.getCartAtBlock(block4)) != null) {
                            SmartCartVehicle cart = SmartCart.util.getCartFromList(minecart);
                            cart.setHeld(false);
                        }
                        if ((minecart = SmartCart.util.getCartAtBlock(block5)) != null) {
                            SmartCartVehicle cart = SmartCart.util.getCartFromList(minecart);
                            cart.setHeld(false);
                        }
                        if ((minecart = SmartCart.util.getCartAtBlock(block6)) != null) {
                            SmartCartVehicle cart = SmartCart.util.getCartFromList(minecart);
                            cart.setHeld(false);
                        }
                        if ((minecart = SmartCart.util.getCartAtBlock(block7)) != null) {
                            SmartCartVehicle cart = SmartCart.util.getCartFromList(minecart);
                            cart.setHeld(false);
                        }
                        if ((minecart = SmartCart.util.getCartAtBlock(block8)) != null) {
                            SmartCartVehicle cart = SmartCart.util.getCartFromList(minecart);
                            cart.setHeld(false);
                        }
                        if ((minecart = SmartCart.util.getCartAtBlock(block9)) != null) {
                            SmartCartVehicle cart = SmartCart.util.getCartFromList(minecart);
                            cart.setHeld(false);
                        }
                    }
            }
            return;
        }

        // Function takes a location, radius, and material to search for -- get all command blocks
        int search_radius = 1;
        ArrayList<Block> cmdBlockList = SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.RED_WOOL);
        cmdBlockList.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.ORANGE_WOOL));
        cmdBlockList.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.YELLOW_WOOL));
        cmdBlockList.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.LIME_WOOL));
        cmdBlockList.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.GREEN_WOOL));
        cmdBlockList.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.CYAN_WOOL));
        cmdBlockList.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.LIGHT_BLUE_WOOL));
        cmdBlockList.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.BLUE_WOOL));
        cmdBlockList.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.PURPLE_WOOL));
        cmdBlockList.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.MAGENTA_WOOL));
        cmdBlockList.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.PINK_WOOL));
        cmdBlockList.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.BROWN_WOOL));
        cmdBlockList.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.WHITE_WOOL));
        cmdBlockList.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.LIGHT_GRAY_WOOL));
        cmdBlockList.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.GRAY_WOOL));
        cmdBlockList.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.BLACK_WOOL));
        // Return if we didn't find any command blocks
        if (cmdBlockList.size() == 0) return;

        ArrayList<Block> spawnBlocks = new ArrayList<>();

        // Check each of the command blocks and put spawn blocks in an arraylist
        for (Block thisBlock : cmdBlockList)
            if (SmartCart.util.isSpawnBlock(thisBlock)) spawnBlocks.add(thisBlock);


        if (spawnBlocks.size() == 0) return;

        // Now we know block is a control block and the redstone was activating.
        //   Time to take action!

        Block block = spawnBlocks.get(0).getLocation().add(0D, 1D, 0D).getBlock();

        // spawn a cart
        Minecart cart = SmartCart.util.spawnCart(block).getCart();
        if (cart == null) return;

        // pick up a nearby player
        double r = net.f85.SmartCart.SmartCart.config.getDouble("pickup_radius");
        for (Entity entity : cart.getNearbyEntities(r, r, r))
            if (entity instanceof Player && cart.getPassengers().isEmpty() && entity.getVehicle() == null) {
                cart.addPassenger(entity);
                SmartCartVehicle smartCart = SmartCart.util.getCartFromList(cart);
                boolean foundSignNearby = false;
                Block block1 = smartCart.getCart().getLocation().add(0, -2, 0).getBlock();
                Block block2 = smartCart.getCart().getLocation().add(1, -1, 0).getBlock();
                Block block3 = smartCart.getCart().getLocation().add(-1, -1, 0).getBlock();
                Block block4 = smartCart.getCart().getLocation().add(0, -1, 1).getBlock();
                Block block5 = smartCart.getCart().getLocation().add(1, -1, -1).getBlock();
                Block block6 = smartCart.getCart().getLocation().add(1, 0, 0).getBlock();
                Block block7 = smartCart.getCart().getLocation().add(-1, 0, 0).getBlock();
                Block block8 = smartCart.getCart().getLocation().add(0, 0, 1).getBlock();
                Block block9 = smartCart.getCart().getLocation().add(0, 0, -1).getBlock();
                // Return if we're not over a sign
                if (SmartCart.util.isSign(block1)) foundSignNearby = true;
                if (SmartCart.util.isSign(block2)) foundSignNearby = true;
                if (SmartCart.util.isSign(block3)) foundSignNearby = true;
                if (SmartCart.util.isSign(block4)) foundSignNearby = true;
                if (SmartCart.util.isSign(block5)) foundSignNearby = true;
                if (SmartCart.util.isSign(block6)) foundSignNearby = true;
                if (SmartCart.util.isSign(block7)) foundSignNearby = true;
                if (SmartCart.util.isSign(block8)) foundSignNearby = true;
                if (SmartCart.util.isSign(block9)) foundSignNearby = true;
                if (foundSignNearby) smartCart.executeControl();
                else SmartCart.util.sendMessage(entity, "Move in the direction you wish to go.");
                break;
            }
    }
}
