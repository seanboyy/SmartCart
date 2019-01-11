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

    private enum Direction {
        NORTH,
        EAST,
        SOUTH,
        WEST,
        NONE;
    }

    private SmartCart plugin;

    SmartCartListener(SmartCart plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onVehicleUpdate(VehicleUpdateEvent event) {
        SmartCartVehicle[] cart = {null};
        SmartCartTrainVehicle[] trainCart = {null};
        helperFunction5(event.getVehicle(), cart, trainCart);
        if(trainCart[0] != null){
            if(trainCart[0].isLeadCart())
                plugin.getServer().getPluginManager().callEvent(new TrainUpdateEvent(SmartCart.util.getTrain(trainCart[0])));
        }
        if(cart[0] != null){
            cart[0].saveCurrentLocation();
            if (cart[0].getCart().getPassengers().isEmpty()) cart[0].setEmptyCartTimer();
            else cart[0].resetEmptyCartTimer();
            if (cart[0].getCart().isDead() || cart[0].isNotOnRail()) return;
            if(cart[0].getCart().isEmpty() || cart[0].getPassenger() != null && cart[0].getPassenger().getType() != EntityType.PLAYER) return;
            if (cart[0].isNewBlock()) cart[0].readControlSign();
            if (cart[0].isHeld()) cart[0].getCart().setVelocity(new Vector(0, 0, 0));
            if (cart[0].isOnControlBlock()) cart[0].executeControl();
            else {
                cart[0].setPreviousMaterial(null);
                cart[0].setSpeed(cart[0].getConfigSpeed());
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
        SmartCartVehicle[] cart = {null};
        SmartCartTrainVehicle[] trainCart = {null};
        helperFunction5(event.getVehicle(), cart, trainCart);
        if(cart[0] != null) {
            if (cart[0].getCart().isDead() || cart[0].isNotOnRail()) return;
            if (cart[0].isLocked()) event.setCancelled(true);
        }
        if(trainCart[0] != null){
            if(trainCart[0].getCart().isDead() || trainCart[0].isNotOnRail()) return;
            if(trainCart[0].isLocked()) event.setCancelled(true);
            if(!trainCart[0].isLeadCart()) return;
            SmartCartTrain train = SmartCart.util.getTrain(trainCart[0]);
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
    public void onVehicleDestroyed(VehicleDestroyEvent event) {
        SmartCartTrainVehicle[] trainCart = {null};
        SmartCartVehicle[] cart = {null};
        helperFunction5(event.getVehicle(), cart, trainCart);
        if(cart[0] != null) cart[0].remove(false);
        if(trainCart[0] != null) trainCart[0].remove(false);
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
                    ArrayList<Block> foundSign = new ArrayList<>();
                    boolean foundSignNearby = helperFunction3(foundSign, cart.getLocation().getBlock().getLocation());
                    // Return if we're not over a sign
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
            ArrayList<Block> foundSign = new ArrayList<>();
            boolean foundSignNearby = helperFunction3(foundSign, block.getLocation());
            int numberOfCarts = 1;
            //find out how many carts to spawn
            Direction goingDir = Direction.NONE;
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
                                    goingDir = Direction.SOUTH;
                                    break;
                                case 'E':
                                    goingDir = Direction.WEST;
                                    break;
                                case 'S':
                                    goingDir = Direction.NORTH;
                                    break;
                                case 'W':
                                    goingDir = Direction.EAST;
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
            Direction _goingDir = goingDir;
            Direction[] goingDirRef = new Direction[] {_goingDir};
            Block[] railRef = new Block[] {block};
            Block[] parentRef = new Block[] {null};
            findNeighborRails(trainPlaces, railRef, parentRef, numberOfCarts - 1, goingDirRef);
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
                case NORTH:
                    //send train south
                    train.setVelocity(0, 1);
                    break;
                case EAST:
                    //send train west
                    train.setVelocity(-1, 0);
                    break;
                case SOUTH:
                    //send train north
                    train.setVelocity(0, -1);
                    break;
                case WEST:
                    //send train east
                    train.setVelocity(1, 0);
                    break;
            }
        }
    }

    private void findNeighborRails(List<Location> locations, Block[] rail, Block[] parent, int num, Direction[] goingDir){
        if (num == 0) return;
        if(parent[0] != null){
            switch(goingDir[0]){
                //north
                case NORTH:
                    switch(((Rail)rail[0].getBlockData()).getShape()){
                        case NORTH_SOUTH:
                            if(helperFunction1(locations, rail, parent, 0, 0, -1, goingDir, Direction.NORTH)) break;
                            else return;
                        case SOUTH_EAST:
                            if(helperFunction1(locations, rail, parent, 1, 0, 0, goingDir, Direction.EAST)) break;
                            else return;
                        case SOUTH_WEST:
                            if(helperFunction1(locations, rail, parent, -1, 0, 0, goingDir, Direction.WEST)) break;
                            else return;
                        case ASCENDING_NORTH:
                            if(helperFunction1(locations, rail, parent, 0, 1, -1, goingDir, Direction.NORTH)) break;
                            else return;
                        case ASCENDING_SOUTH:
                            if(helperFunction1(locations, rail, parent, 0, 0, -1, goingDir, Direction.NORTH)) break;
                            else return;
                    }
                    break;
                //east
                case EAST:
                    switch(((Rail)rail[0].getBlockData()).getShape()){
                        case NORTH_WEST:
                            if(helperFunction1(locations, rail, parent, 0, 0, -1, goingDir, Direction.NORTH)) break;
                            else return;
                        case SOUTH_WEST:
                            if(helperFunction1(locations, rail, parent, 0, 0, 1, goingDir, Direction.SOUTH)) break;
                            else return;
                        case EAST_WEST:
                            if(helperFunction1(locations, rail, parent, 1, 0, 0, goingDir, Direction.EAST)) break;
                            else return;
                        case ASCENDING_EAST:
                            if(helperFunction1(locations, rail, parent, 1, 1, 0, goingDir, Direction.EAST)) break;
                            else return;
                        case ASCENDING_WEST:
                            if(helperFunction1(locations, rail, parent, 1, 1, 0, goingDir, Direction.EAST)) break;
                            else return;
                    }
                    break;
                //south
                case SOUTH:
                    switch(((Rail)rail[0].getBlockData()).getShape()){
                        case NORTH_SOUTH:
                            if(helperFunction1(locations, rail, parent, 0, 0, 1, goingDir, Direction.SOUTH)) break;
                            else return;
                        case NORTH_EAST:
                            if(helperFunction1(locations, rail, parent, 1, 0, 0, goingDir, Direction.EAST)) break;
                            else return;
                        case NORTH_WEST:
                            if(helperFunction1(locations, rail, parent, -1, 0, 0, goingDir, Direction.WEST)) break;
                            else return;
                        case ASCENDING_NORTH:
                            if(helperFunction1(locations, rail, parent, 0, 0, 1, goingDir, Direction.SOUTH)) break;
                            else return;
                        case ASCENDING_SOUTH:
                            if(helperFunction1(locations, rail, parent, 0, 1, 1, goingDir, Direction.SOUTH)) break;
                            else return;
                    }
                    break;
                //west
                case WEST:
                    switch(((Rail)rail[0].getBlockData()).getShape()){
                        case EAST_WEST:
                            if(helperFunction1(locations, rail, parent, -1, 0, 0, goingDir, Direction.WEST)) break;
                            else return;
                        case SOUTH_EAST:
                            if(helperFunction1(locations, rail, parent, 0, 0, 1, goingDir, Direction.SOUTH)) break;
                            else return;
                        case NORTH_EAST:
                            if(helperFunction1(locations, rail, parent, 0, 0, -1, goingDir, Direction.NORTH)) break;
                            else return;
                        case ASCENDING_EAST:
                            if(helperFunction1(locations, rail, parent, -1, 0, 0, goingDir, Direction.WEST)) break;
                            else return;
                        case ASCENDING_WEST:
                            if(helperFunction1(locations, rail, parent, -1, 1, 0, goingDir, Direction.WEST)) break;
                            else return;
                    }
                    break;
            }
        }
        else{
            switch(((Rail)rail[0].getBlockData()).getShape()){
                case NORTH_SOUTH:
                    if(helperFunction2(Direction.SOUTH, Direction.NORTH, locations, rail, parent, 0, 0, 1, 0, 0, -1, goingDir)) break;
                    else return;
                case EAST_WEST:
                    if(helperFunction2(Direction.WEST, Direction.EAST, locations, rail, parent, -1, 0, 0, 1, 0, 0, goingDir)) break;
                    else return;
                case NORTH_EAST:
                    if(helperFunction2(Direction.EAST, Direction.NORTH, locations, rail, parent, 1, 0, 0, 0, 0, -1, goingDir)) break;
                    else return;
                case NORTH_WEST:
                    if(helperFunction2(Direction.WEST, Direction.NORTH, locations, rail, parent, -1, 0, 0, 0, 0, -1, goingDir)) break;
                    else return;
                case SOUTH_EAST:
                    if(helperFunction2(Direction.SOUTH, Direction.EAST, locations, rail, parent, 0, 0, 1, 1, 0, 0, goingDir)) break;
                    else return;
                case SOUTH_WEST:
                    if(helperFunction2(Direction.SOUTH, Direction.WEST, locations, rail, parent, 0, 0, 1, -1, 0, 0, goingDir)) break;
                    else return;
                case ASCENDING_EAST:
                    if(helperFunction2(Direction.WEST, Direction.EAST, locations, rail, parent, -1, 0, 0, 1, 1, 0, goingDir)) break;
                    else return;
                case ASCENDING_NORTH:
                    if(helperFunction2(Direction.SOUTH, Direction.NORTH, locations, rail, parent, 0, 0, 1, 0, 1, -1, goingDir)) break;
                    else return;
                case ASCENDING_WEST:
                    if(helperFunction2(Direction.WEST, Direction.EAST, locations, rail, parent, -1, 1, 0, 1, 0, 0, goingDir)) break;
                    else return;
                case ASCENDING_SOUTH:
                    if(helperFunction2(Direction.SOUTH, Direction.NORTH, locations, rail, parent, 0, 1, 1, 0, 0, -1, goingDir)) break;
                    else return;
            }
        }
        findNeighborRails(locations, rail, parent, num - 1, goingDir);
    }

    private boolean helperFunction1(List<Location> locations, Block[] railRef, Block[] parentRef, int x, int y, int z, Direction[] goingDirRef, Direction newGoingDir){
        if(SmartCart.util.isRail(railRef[0].getLocation().add(x, y, z).getBlock())){
            locations.add(railRef[0].getLocation().add(x, y, z));
            goingDirRef[0] = newGoingDir;
            parentRef[0] = railRef[0];
            railRef[0] = railRef[0].getLocation().add(x, y, z).getBlock();
            return true;
        }
        return false;
    }

    private boolean helperFunction2(Direction goingDirOption1, Direction goingDirOption2, List<Location> locations, Block[] railRef, Block[] parentRef, int x1, int y1, int z1, int x2, int y2, int z2, Direction[] goingDirRef){
        return (goingDirRef[0] == goingDirOption1 && helperFunction1(locations, railRef, parentRef, x1, y1, z1, goingDirRef, goingDirRef[0])) ||
                (goingDirRef[0] == goingDirOption2 && helperFunction1(locations, railRef, parentRef, x2, y2, z2, goingDirRef, goingDirRef[0]));
    }

    private boolean helperFunction3(ArrayList<Block> signs, Location location){
        boolean[] ret = {false};
        Block block1 = location.add(0, -2, 0).getBlock();
        Block block2 = location.add(1, -1, 0).getBlock();
        Block block3 = location.add(-1, -1, 0).getBlock();
        Block block4 = location.add(0, -1, 1).getBlock();
        Block block5 = location.add(1, -1, -1).getBlock();
        Block block6 = location.add(1, 0, 0).getBlock();
        Block block7 = location.add(-1, 0, 0).getBlock();
        Block block8 = location.add(0, 0, 1).getBlock();
        Block block9 = location.add(0, 0, -1).getBlock();
        helperFunction4(block1, signs, ret);
        helperFunction4(block2, signs, ret);
        helperFunction4(block3, signs, ret);
        helperFunction4(block4, signs, ret);
        helperFunction4(block5, signs, ret);
        helperFunction4(block6, signs, ret);
        helperFunction4(block7, signs, ret);
        helperFunction4(block8, signs, ret);
        helperFunction4(block9, signs, ret);
        return ret[0];

    }

    private void helperFunction4(Block block, ArrayList<Block> signs, boolean[] foundSignRef){
        if(SmartCart.util.isSign(block)){
            signs.add(block);
            foundSignRef[0] = true;
        }
    }

    private void helperFunction5(Vehicle vehicle, SmartCartVehicle[] cart, SmartCartTrainVehicle[] trainCart){
        if(!(vehicle instanceof Minecart)) return;
        cart[0] = SmartCart.util.getCartFromList(vehicle.getEntityId());
        trainCart[0] = SmartCart.util.getTrainCartFromList(vehicle.getEntityId());
    }
}
