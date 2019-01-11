//
// smartcart copyright 2015 Ian Clark
//
// Distributed under the MIT License
// http://opensource.org/licenses/MIT
//
package io.github.seanboyy.smartcart;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Rail;
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
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class SmartCartListener implements Listener {

    private SmartCart plugin;

    SmartCartListener(SmartCart plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onVehicleUpdate(VehicleUpdateEvent event) {
        Vehicle vehicle = event.getVehicle();
        // Return if vehicle is not a minecart
        if (!(vehicle instanceof Minecart)) return;
        SmartCartVehicle cart = SmartCart.util.getCartFromList(vehicle.getEntityId());
        SmartCartTrainVehicle trainCart = SmartCart.util.getTrainCartFromList(vehicle.getEntityId());
        if(trainCart != null){
            if(trainCart.isLeadCart())
                plugin.getServer().getPluginManager().callEvent(new TrainUpdateEvent(SmartCart.util.getTrain(trainCart)));
        }
        if(cart != null){
            cart.saveCurrentLocation();
            if (cart.getCart().getPassengers().isEmpty()) cart.setEmptyCartTimer();
            else cart.resetEmptyCartTimer();
            if (cart.getCart().isDead() || cart.isNotOnRail()) return;
            if(cart.getCart().isEmpty() || cart.getPassenger() != null && cart.getPassenger().getType() != EntityType.PLAYER) return;
            if (cart.isNewBlock()) cart.readControlSign();
            if (cart.isHeld()) cart.getCart().setVelocity(new Vector(0, 0, 0));
            if (cart.isOnControlBlock()) cart.executeControl();
            else {
                cart.setPreviousMaterial(null);
                cart.setSpeed(cart.getConfigSpeed());
            }
        }
    }

    @EventHandler
    public void onTrainUpdate(TrainUpdateEvent event){
        SmartCartTrain train = event.getTrain();
        train.leadCart.saveCurrentLocation();
        if(train.leadCart.getCart().isDead() || train.leadCart.isNotOnRail()) return;
        if(train.leadCart.getCart().isEmpty() || train.leadCart.getPassenger() != null && train.leadCart.getPassenger().getType() != EntityType.PLAYER) return;
        if(train.leadCart.isNewBlock()) train.leadCart.readControlSign();
        for(SmartCartTrainVehicle followCart : train.followCarts){
            if(followCart.isNewBlock()) followCart.readControlSign();
        }
        if(train.leadCart.isHeld()){
            train.leadCart.getCart().setVelocity(new Vector(0, 0, 0));
            for(SmartCartTrainVehicle trainVehicle : train.followCarts){
                trainVehicle.getCart().setVelocity(new Vector(0, 0, 0));
            }
        }
        if(train.leadCart.isOnControlBlock()) train.executeControl();
        else{
            train.leadCart.setPreviousMaterial(null);
        }
        for(SmartCartTrainVehicle trainVehicle : train.followCarts){
            trainVehicle.saveCurrentLocation();
            trainVehicle.setSpeed(train.leadCart.getConfigSpeed());
        }
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        Vehicle vehicle = event.getVehicle();
        if (!(vehicle instanceof Minecart)) return;
        SmartCartVehicle cart = SmartCart.util.getCartFromList(vehicle.getEntityId());
        SmartCartTrainVehicle trainCart = SmartCart.util.getTrainCartFromList(vehicle.getEntityId());
        if(cart != null) {
            if (cart.getCart().isDead() || cart.isNotOnRail()) return;
            if (cart.isLocked()) event.setCancelled(true);
        }
        if(trainCart != null){
            if(trainCart.getCart().isDead() || trainCart.isNotOnRail()) return;
            if(trainCart.isLocked()) event.setCancelled(true);
            if(!trainCart.isLeadCart()) return;
            SmartCartTrain train = SmartCart.util.getTrain(trainCart);
            if(train.shouldKillTrain()){
                train.leadCart.remove(true);
                for(SmartCartTrainVehicle _trainVehicle : train.followCarts){
                    _trainVehicle.remove(true);
                }
                SmartCart.util.removeTrain(train);
            }
        }
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        Vehicle vehicle = event.getVehicle();
        // Return if vehicle is not a minecart
        if (!(vehicle instanceof Minecart)) return;
        // Return if it wasn't a player that entered
        if (event.getEntered().getType() != EntityType.PLAYER) return;
        /*
        Why is this section necessary? Future use stuff?
        SmartCartVehicle cart = SmartCart.util.getCartFromList((Minecart) vehicle);
        // Return if minecart is marked for removal, or off rails for any reason
        if (cart.getCart().isDead() || cart.isNotOnRail()) return;
        */
    }

    @EventHandler
    public void onVehicleDestroyed(VehicleDestroyEvent event) {
        Vehicle vehicle = event.getVehicle();
        if(!(vehicle instanceof Minecart)) return;
        SmartCartVehicle cart = SmartCart.util.getCartFromList(vehicle.getEntityId());
        SmartCartTrainVehicle trainCart = SmartCart.util.getTrainCartFromList(vehicle.getEntityId());
        if(cart != null) cart.remove(false);
        if(trainCart != null) trainCart.remove(false);
    }

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if (event.getOldCurrent() > event.getNewCurrent()){
            if(event.getNewCurrent() != 0) return;
            int search_radius = 1;
            ArrayList<Block> signs = SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.SIGN);
            signs.addAll(SmartCart.util.getBlocksNearby(event.getBlock(), search_radius, Material.WALL_SIGN));
            if(signs.size() ==0) return;
            for (Block block : signs) {
                ArrayList<Block> cartLocations = SmartCart.util.getBlocksNearby(block, search_radius, Material.RAIL);
                cartLocations.addAll(SmartCart.util.getBlocksNearby(block, search_radius, Material.POWERED_RAIL));
                cartLocations.addAll(SmartCart.util.getBlocksNearby(block, search_radius, Material.DETECTOR_RAIL));
                cartLocations.addAll(SmartCart.util.getBlocksNearby(block, search_radius, Material.ACTIVATOR_RAIL));
                if(cartLocations.size() ==0 ) return;
                Minecart minecart;
                for(Block _block : cartLocations){
                    if((minecart = SmartCart.util.getCartAtBlock(_block)) != null){
                        SmartCartVehicle cart = SmartCart.util.getCartFromList(minecart);
                        cart.executeSign(block);
                        break;
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
        ArrayList<Block> trainSpawnBlocks = new ArrayList<>();

        // Check each of the command blocks and put spawn blocks in an arraylist
        for (Block thisBlock : cmdBlockList){
            if (SmartCart.util.isSpawnBlock(thisBlock)) spawnBlocks.add(thisBlock);
            if (SmartCart.util.isTrainSpawnBlock(thisBlock)) trainSpawnBlocks.add(thisBlock);
        }


        if (spawnBlocks.size() == 0 && trainSpawnBlocks.size() == 0) return;

        // Now we know block is a control block and the redstone was activating.
        //   Time to take action!
        if(spawnBlocks.size() > 0) {
            Block block = spawnBlocks.get(0).getLocation().add(0D, 1D, 0D).getBlock();

            // spawn a cart
            Minecart cart = SmartCart.util.spawnCart(block).getCart();
            if (cart == null) return;

            // pick up a nearby player
            double r = SmartCart.config.getDouble("pickup_radius");
            for (Entity entity : cart.getNearbyEntities(r, r, r))
                if (entity instanceof Player && cart.getPassengers().isEmpty() && entity.getVehicle() == null) {
                    cart.addPassenger(entity);
                    SmartCartVehicle smartCart = SmartCart.util.getCartFromList(cart);
                    boolean foundSignNearby = false;
                    ArrayList<Block> foundSign = new ArrayList<>();
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
                    if (SmartCart.util.isSign(block1)) {
                        foundSignNearby = true;
                        foundSign.add(block1);
                    }
                    if (SmartCart.util.isSign(block2)) {
                        foundSignNearby = true;
                        foundSign.add(block2);
                    }
                    if (SmartCart.util.isSign(block3)) {
                        foundSignNearby = true;
                        foundSign.add(block3);
                    }
                    if (SmartCart.util.isSign(block4)) {
                        foundSignNearby = true;
                        foundSign.add(block4);
                    }
                    if (SmartCart.util.isSign(block5)) {
                        foundSignNearby = true;
                        foundSign.add(block5);
                    }
                    if (SmartCart.util.isSign(block6)) {
                        foundSignNearby = true;
                        foundSign.add(block6);
                    }
                    if (SmartCart.util.isSign(block7)) {
                        foundSignNearby = true;
                        foundSign.add(block7);
                    }
                    if (SmartCart.util.isSign(block8)) {
                        foundSignNearby = true;
                        foundSign.add(block8);
                    }
                    if (SmartCart.util.isSign(block9)) {
                        foundSignNearby = true;
                        foundSign.add(block9);
                    }
                    if (foundSignNearby) {
                        for(Block sign : foundSign){
                            smartCart.executeSign(sign);
                        }
                    }
                    else SmartCart.util.sendMessage(entity, "Move in the direction you wish to go.");
                    break;
                }
        }
        if(trainSpawnBlocks.size() > 0){
            Block block = trainSpawnBlocks.get(0).getLocation().add(0D, 1D, 0D).getBlock();
            //read signs
            boolean foundSignNearby = false;
            ArrayList<Block> foundSign = new ArrayList<>();
            Block block1 = block.getLocation().add(0, -2, 0).getBlock();
            Block block2 = block.getLocation().add(1, -1, 0).getBlock();
            Block block3 = block.getLocation().add(-1, -1, 0).getBlock();
            Block block4 = block.getLocation().add(0, -1, 1).getBlock();
            Block block5 = block.getLocation().add(1, -1, -1).getBlock();
            Block block6 = block.getLocation().add(1, 0, 0).getBlock();
            Block block7 = block.getLocation().add(-1, 0, 0).getBlock();
            Block block8 = block.getLocation().add(0, 0, 1).getBlock();
            Block block9 = block.getLocation().add(0, 0, -1).getBlock();
            // Return if we're not over a sign //why?
            if (SmartCart.util.isSign(block1)) {
                foundSignNearby = true;
                foundSign.add(block1);
            }
            if (SmartCart.util.isSign(block2)) {
                foundSignNearby = true;
                foundSign.add(block2);
            }
            if (SmartCart.util.isSign(block3)) {
                foundSignNearby = true;
                foundSign.add(block3);
            }
            if (SmartCart.util.isSign(block4)) {
                foundSignNearby = true;
                foundSign.add(block4);
            }
            if (SmartCart.util.isSign(block5)) {
                foundSignNearby = true;
                foundSign.add(block5);
            }
            if (SmartCart.util.isSign(block6)) {
                foundSignNearby = true;
                foundSign.add(block6);
            }
            if (SmartCart.util.isSign(block7)) {
                foundSignNearby = true;
                foundSign.add(block7);
            }
            if (SmartCart.util.isSign(block8)) {
                foundSignNearby = true;
                foundSign.add(block8);
            }
            if (SmartCart.util.isSign(block9)) {
                foundSignNearby = true;
                foundSign.add(block9);
            }
            int numberOfCarts = 1;
            //find out how many carts to spawn
            int goingDir = -1;
            if (foundSignNearby) {
                for(Block sign : foundSign){
                    Sign _sign = (Sign)sign.getState();
                    List<Pair<String, String>> signTokens = SmartCartVehicle.parseSign(_sign);
                    for(Pair<String, String> pair : signTokens){
                        if(pair.left().equals("$AMT")){
                            int temp = pair.right().length() >= 2 && Character.isLetter(pair.right().charAt(0))? Integer.parseInt(pair.right().substring(1)) : 1;
                            if(temp < 1) temp = 1;
                            if(temp > SmartCart.config.getInt("max_train_length")) temp = SmartCart.config.getInt("max_train_length");
                            numberOfCarts = temp;
                            switch(pair.right().charAt(0)){
                                case 'N':
                                    goingDir = 2;
                                    break;
                                case 'E':
                                    goingDir = 3;
                                    break;
                                case 'S':
                                    goingDir = 0;
                                    break;
                                case 'W':
                                    goingDir = 1;
                                    break;
                            }
                            break;
                        }
                    }
                }
            }
            else return;
            //find out where to place the carts
            ArrayList<Location> trainPlaces = new ArrayList<>();
            trainPlaces.add(block.getLocation());
            findNeighborRails(trainPlaces, block, null, numberOfCarts - 1, goingDir);
            //place first cart
            SmartCartTrainVehicle trainCart = SmartCart.util.spawnTrainCart(trainPlaces.get(0).getBlock());
            if(trainCart.getCart() == null) return;
            trainCart.setLeadCart(true);
            SmartCartTrain train = new SmartCartTrain(trainCart);
            //place the rest of the carts
            for(int i = 1; i < trainPlaces.size(); ++i){
                SmartCartTrainVehicle followCart = SmartCart.util.spawnTrainCart(trainPlaces.get(i).getBlock());
                followCart.setLeadCart(false);
                if(followCart.getCart() == null) return;
                train.followCarts.add(followCart);
            }
            //attach players nearby
            double r = SmartCart.config.getDouble("pickup_radius");
            for(Entity entity : train.leadCart.getCart().getNearbyEntities(r, r, r)){
                if(entity instanceof Player && train.leadCart.getCart().isEmpty() && entity.getVehicle() == null) train.leadCart.getCart().addPassenger(entity);
            }
            for(SmartCartTrainVehicle trainVehicle : train.followCarts) {
                for (Entity entity : trainVehicle.getCart().getNearbyEntities(r, r, r)) {
                    if (entity instanceof Player && trainVehicle.getCart().getPassengers().isEmpty() && entity.getVehicle() == null)
                        trainVehicle.getCart().addPassenger(entity);
                }
            }
            SmartCart.util.addTrain(train);
            //do the rest of the sign
            for(Block sign : foundSign){
                train.executeSign(sign);
            }
            //move the train in the correct direction
            switch(goingDir){
                case 0:
                    //send train south
                    train.setVelocity(0, 1);
                    break;
                case 1:
                    //send train west
                    train.setVelocity(-1, 0);
                    break;
                case 2:
                    //send train north
                    train.setVelocity(0, -1);
                    break;
                case 3:
                    //send train east
                    train.setVelocity(1, 0);
                    break;
            }
        }
    }

    /*
        From Dir:
        0 = north
        1 = east
        2 = south
        3 = west
     */
    private void findNeighborRails(List<Location> locations, Block rail, Block parent, int num, int goingDir){
        if (num == 0) return;
        //if parent goes north south, try going south first, then west, then east. If nothing is found, stop.
        //if parent goes east west, try going south first, then west, then east. If nothing is found, stop.
        //if parent goes north east
        if(parent != null){
            switch(goingDir){
                //north
                case 0:
                    switch(((Rail)rail.getBlockData()).getShape()){
                        case NORTH_SOUTH:
                            if(SmartCart.util.isRail(rail.getLocation().add(0, 0, -1).getBlock())){
                                locations.add(rail.getLocation().add(0, 0, -1));
                                goingDir = 0;
                                parent = rail;
                                rail = rail.getLocation().add(0, 0, -1).getBlock();
                            }
                            else return;
                            break;
                        case SOUTH_EAST:
                            if(SmartCart.util.isRail(rail.getLocation().add(1, 0, 0).getBlock())){
                                locations.add(rail.getLocation().add(1, 0, 0));
                                goingDir = 1;
                                parent = rail;
                                rail = rail.getLocation().add(1, 0, 0).getBlock();
                            }
                            else return;
                            break;
                        case SOUTH_WEST:
                            if(SmartCart.util.isRail(rail.getLocation().add(-1, 0, 0).getBlock())){
                                locations.add(rail.getLocation().add(-1, 0, 0));
                                goingDir = 3;
                                parent = rail;
                                rail = rail.getLocation().add(-1, 0, 0).getBlock();
                            }
                            else return;
                            break;
                        case ASCENDING_NORTH:
                            if(SmartCart.util.isRail(rail.getLocation().add(0, 1, -1).getBlock())){
                                locations.add(rail.getLocation().add(0, 1, -1));
                                goingDir = 0;
                                parent = rail;
                                rail = rail.getLocation().add(0, 1, -1).getBlock();
                            }
                            else return;
                            break;
                        case ASCENDING_SOUTH:
                            if(SmartCart.util.isRail(rail.getLocation().add(0, 0, -1).getBlock())){
                                locations.add(rail.getLocation().add(0, 0, -1));
                                goingDir = 0;
                                parent = rail;
                                rail = rail.getLocation().add(0, 0, -1).getBlock();
                            }
                            else return;
                            break;
                    }
                    break;
                //east
                case 1:
                    switch(((Rail)rail.getBlockData()).getShape()){
                        case NORTH_WEST:
                            if(SmartCart.util.isRail(rail.getLocation().add(0, 0, -1).getBlock())){
                                locations.add(rail.getLocation().add(0, 0, -1));
                                goingDir = 0;
                                parent = rail;
                                rail = rail.getLocation().add(0, 0, -1).getBlock();
                            }
                            else return;
                            break;
                        case SOUTH_WEST:
                            if(SmartCart.util.isRail(rail.getLocation().add(0, 0, 1).getBlock())){
                                locations.add(rail.getLocation().add(0, 0, 1));
                                goingDir = 2;
                                parent = rail;
                                rail = rail.getLocation().add(0, 0, 1).getBlock();
                            }
                            else return;
                            break;
                        case EAST_WEST:
                            if(SmartCart.util.isRail(rail.getLocation().add(1, 0, 0).getBlock())){
                                locations.add(rail.getLocation().add(1, 0, 0));
                                goingDir = 1;
                                parent = rail;
                                rail = rail.getLocation().add(1, 0, 0).getBlock();
                            }
                            else return;
                            break;
                        case ASCENDING_EAST:
                            if(SmartCart.util.isRail(rail.getLocation().add(1, 1, 0).getBlock())){
                                locations.add(rail.getLocation().add(1, 1, 0));
                                goingDir = 1;
                                parent = rail;
                                rail = rail.getLocation().add(1, 1, 0).getBlock();
                            }
                            else return;
                            break;
                        case ASCENDING_WEST:
                            if(SmartCart.util.isRail(rail.getLocation().add(1, 0, 0).getBlock())){
                                locations.add(rail.getLocation().add(1, 0, 0));
                                goingDir = 1;
                                parent = rail;
                                rail = rail.getLocation().add(1, 0, 0).getBlock();
                            }
                            else return;
                            break;
                    }
                    break;
                //south
                case 2:
                    switch(((Rail)rail.getBlockData()).getShape()){
                        case NORTH_SOUTH:
                            if(SmartCart.util.isRail(rail.getLocation().add(0, 0, 1).getBlock())){
                                locations.add(rail.getLocation().add(0, 0, 1));
                                goingDir = 2;
                                parent = rail;
                                rail = rail.getLocation().add(0, 0, 1).getBlock();
                            }
                            else return;
                            break;
                        case NORTH_EAST:
                            if(SmartCart.util.isRail(rail.getLocation().add(1, 0, 0).getBlock())){
                                locations.add(rail.getLocation().add(1, 0, 0));
                                goingDir = 1;
                                parent = rail;
                                rail = rail.getLocation().add(1, 0, 0).getBlock();
                            }
                            else return;
                            break;
                        case NORTH_WEST:
                            if(SmartCart.util.isRail(rail.getLocation().add(-1, 0, 0).getBlock())){
                                locations.add(rail.getLocation().add(-1, 0, 0));
                                goingDir = 3;
                                parent = rail;
                                rail = rail.getLocation().add(-1, 0, 0).getBlock();
                            }
                            else return;
                            break;
                        case ASCENDING_NORTH:
                            if(SmartCart.util.isRail(rail.getLocation().add(0, 0, 1).getBlock())){
                                locations.add(rail.getLocation().add(0, 0, 1));
                                goingDir = 2;
                                parent = rail;
                                rail = rail.getLocation().add(0, 0, 1).getBlock();
                            }
                            else return;
                            break;
                        case ASCENDING_SOUTH:
                            if(SmartCart.util.isRail(rail.getLocation().add(0, 1, 1).getBlock())){
                                locations.add(rail.getLocation().add(0, 1, 1));
                                goingDir = 2;
                                parent = rail;
                                rail = rail.getLocation().add(0, 1, 1).getBlock();
                            }
                            else return;
                            break;
                    }
                    break;
                //west
                case 3:
                    switch(((Rail)rail.getBlockData()).getShape()){
                        case EAST_WEST:
                            if(SmartCart.util.isRail(rail.getLocation().add(-1, 0, 0).getBlock())){
                                locations.add(rail.getLocation().add(-1, 0, 0));
                                goingDir = 3;
                                parent = rail;
                                rail = rail.getLocation().add(-1, 0, 0).getBlock();
                            }
                            else return;
                            break;
                        case SOUTH_EAST:
                            if(SmartCart.util.isRail(rail.getLocation().add(0, 0, 1).getBlock())){
                                locations.add(rail.getLocation().add(0, 0, 1));
                                goingDir = 2;
                                parent = rail;
                                rail = rail.getLocation().add(0, 0, 1).getBlock();
                            }
                            else return;
                            break;
                        case NORTH_EAST:
                            if(SmartCart.util.isRail(rail.getLocation().add(0, 0, -1).getBlock())){
                                locations.add(rail.getLocation().add(0, 0, -1));
                                goingDir = 0;
                                parent = rail;
                                rail = rail.getLocation().add(0, 0, -1).getBlock();
                            }
                            else return;
                            break;
                        case ASCENDING_EAST:
                            if(SmartCart.util.isRail(rail.getLocation().add(-1, 0, 0).getBlock())){
                                locations.add(rail.getLocation().add(-1, 0, 0));
                                goingDir = 3;
                                parent = rail;
                                rail = rail.getLocation().add(-1, 0, 0).getBlock();
                            }
                            else return;
                            break;
                        case ASCENDING_WEST:
                            if(SmartCart.util.isRail(rail.getLocation().add(-1, 1, 0).getBlock())){
                                locations.add(rail.getLocation().add(-1, 1, 0));
                                goingDir = 3;
                                parent = rail;
                                rail = rail.getLocation().add(-1, 1, 0).getBlock();
                            }
                            else return;
                            break;
                    }
                    break;
            }
        }
        else{
            switch(((Rail)rail.getBlockData()).getShape()){
                case NORTH_SOUTH:
                    //look south
                    if(goingDir == 2 && SmartCart.util.isRail(rail.getLocation().add(0, 0, 1).getBlock())){
                        locations.add(rail.getLocation().add(0, 0, 1));
                        parent = rail;
                        rail = rail.getLocation().add(0, 0, 1).getBlock();
                    }
                    //otherwise look north
                    else if(goingDir == 0 && SmartCart.util.isRail(rail.getLocation().add(0, 0, -1).getBlock())){
                        locations.add(rail.getLocation().add(0, 0, -1));
                        parent = rail;
                        rail = rail.getLocation().add(0, 0, -1).getBlock();
                    }
                    //this is the only rail.
                    else return;
                    break;
                case EAST_WEST:
                    //look west
                    if(goingDir == 3 && SmartCart.util.isRail(rail.getLocation().add(-1, 0, 0).getBlock())){
                        locations.add(rail.getLocation().add(-1, 0, 0));
                        parent = rail;
                        rail = rail.getLocation().add(-1, 0, 0).getBlock();
                    }
                    //look east
                    else if(goingDir == 1 && SmartCart.util.isRail(rail.getLocation().add(1, 0, 0).getBlock())){
                        locations.add(rail.getLocation().add(1, 0, 0));
                        parent = rail;
                        rail = rail.getLocation().add(1, 0, 0).getBlock();
                    }
                    //this is the only rail
                    else return;
                    break;
                case NORTH_EAST:
                    if(goingDir == 1 && SmartCart.util.isRail(rail.getLocation().add(1, 0, 0).getBlock())){
                        locations.add(rail.getLocation().add(1, 0, 0));
                        parent = rail;
                        rail = rail.getLocation().add(1, 0, 0).getBlock();
                    }
                    else if(goingDir == 0 && SmartCart.util.isRail(rail.getLocation().add(0, 0, -1).getBlock())){
                        locations.add(rail.getLocation().add(0, 0, -1));
                        parent = rail;
                        rail = rail.getLocation().add(0, 0, -1).getBlock();
                    }
                    else return;
                    break;
                case NORTH_WEST:
                    if(goingDir == 3 && SmartCart.util.isRail(rail.getLocation().add(-1, 0, 0).getBlock())){
                        locations.add(rail.getLocation().add(-1, 0, 0));
                        parent = rail;
                        rail = rail.getLocation().add(-1, 0, 0).getBlock();
                    }
                    else if(goingDir == 0 && SmartCart.util.isRail(rail.getLocation().add(0, 0, -1).getBlock())){
                        locations.add(rail.getLocation().add(0, 0, -1));
                        parent = rail;
                        rail = rail.getLocation().add(0, 0, -1).getBlock();
                    }
                    else return;
                    break;
                case SOUTH_EAST:
                    if(goingDir == 2 && SmartCart.util.isRail(rail.getLocation().add(0, 0, 1).getBlock())){
                        locations.add(rail.getLocation().add(0, 0, 1));
                        parent = rail;
                        rail = rail.getLocation().add(0, 0, 1).getBlock();
                    }
                    else if(goingDir == 1 && SmartCart.util.isRail(rail.getLocation().add(1, 0, 0).getBlock())){
                        locations.add(rail.getLocation().add(1, 0, 0));
                        parent = rail;
                        rail = rail.getLocation().add(1, 0, 0).getBlock();
                    }
                    else return;
                    break;
                case SOUTH_WEST:
                    if(goingDir == 2 && SmartCart.util.isRail(rail.getLocation().add(0, 0, 1).getBlock())){
                        locations.add(rail.getLocation().add(0, 0, 1));
                        parent = rail;
                        rail = rail.getLocation().add(0, 0, 1).getBlock();
                    }
                    else if(goingDir == 3 && SmartCart.util.isRail(rail.getLocation().add(-1, 0, 0).getBlock())){
                        locations.add(rail.getLocation().add(-1, 0, 0));
                        parent = rail;
                        rail = rail.getLocation().add(-1, 0, 0).getBlock();
                    }
                    else return;
                    break;
                case ASCENDING_EAST:
                    if(SmartCart.util.isRail(rail.getLocation().add(-1, 0, 0).getBlock())){
                        locations.add(rail.getLocation().add(-1, 0, 0));
                        parent = rail;
                        rail = rail.getLocation().add(-1, 0, 0).getBlock();
                    }
                    else if(SmartCart.util.isRail(rail.getLocation().add(1, 1, 0).getBlock())){
                        locations.add(rail.getLocation().add(1, 1, 0));
                        parent = rail;
                        rail = rail.getLocation().add(1, 1, 0).getBlock();
                    }
                    else return;
                    break;
                case ASCENDING_NORTH:
                    if(goingDir == 2 && SmartCart.util.isRail(rail.getLocation().add(0, 0, 1).getBlock())){
                        locations.add(rail.getLocation().add(0, 0, 1));
                        parent = rail;
                        rail = rail.getLocation().add(0, 0, 1).getBlock();
                    }
                    else if(goingDir == 0 && SmartCart.util.isRail(rail.getLocation().add(0, 1, -1).getBlock())){
                        locations.add(rail.getLocation().add(0, 1, -1));
                        parent = rail;
                        rail = rail.getLocation().add(0, 1, -1).getBlock();
                    }
                    else return;
                    break;
                case ASCENDING_WEST:
                    if(goingDir == 3 && SmartCart.util.isRail(rail.getLocation().add(-1, 1, 0).getBlock())){
                        locations.add(rail.getLocation().add(-1, 1, 0));
                        parent = rail;
                        rail = rail.getLocation().add(-1, 1, 0).getBlock();
                    }
                    else if(goingDir == 1 && SmartCart.util.isRail(rail.getLocation().add(1, 0, 0).getBlock())){
                        locations.add(rail.getLocation().add(1, 0, 0));
                        parent = rail;
                        rail = rail.getLocation().add(1, 0, 0).getBlock();
                    }
                    else return;
                    break;
                case ASCENDING_SOUTH:
                    if(goingDir == 2 && SmartCart.util.isRail(rail.getLocation().add(0, 1, 1).getBlock())){
                        locations.add(rail.getLocation().add(0, 1, 1));
                        parent = rail;
                        rail = rail.getLocation().add(0, 1, 1).getBlock();
                    }
                    else if(goingDir == 1 && SmartCart.util.isRail(rail.getLocation().add(0, 0, -1).getBlock())){
                        locations.add(rail.getLocation().add(0, 0, -1));
                        parent = rail;
                        rail = rail.getLocation().add(0, 0, -1).getBlock();
                    }
                    else return;
                    break;
            }
        }
        findNeighborRails(locations, rail, parent, num - 1, goingDir);
    }
}
