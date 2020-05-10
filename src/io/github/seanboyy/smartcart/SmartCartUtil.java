//
// smartcart copyright 2015 Ian Clark
//
// Distributed under the MIT License
// http://opensource.org/licenses/MIT
//
package io.github.seanboyy.smartcart;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

class SmartCartUtil {

    private ArrayList<SmartCartVehicle> cartList = new ArrayList<>();
    private ArrayList<SmartCartTrainVehicle> trainCartList = new ArrayList<>();
    private ArrayList<SmartCartTrain> trainList = new ArrayList<>();
    private SmartCart plugin;

    SmartCartUtil(SmartCart plugin) {
        this.plugin = plugin;
    }
    // Accessors!

    ArrayList<SmartCartTrain> getTrainList(){
        return new ArrayList<>(trainList);
    }

    ArrayList<SmartCartTrain> getTrainList(World world){
        ArrayList<SmartCartTrain> worldTrains = new ArrayList<>();
        for(SmartCartTrain train : trainList){
            if(train.leadCart.getLocation().getWorld() == world) worldTrains.add(train);
        }
        return worldTrains;
    }

    SmartCartTrain getTrain(SmartCartTrainVehicle vehicle){
        for(SmartCartTrain train : trainList){
            if(train.leadCart.getEntityId() == vehicle.getEntityId()) return train;
            for(SmartCartTrainVehicle trainVehicle : train.followCarts){
                if(trainVehicle.getEntityId() == vehicle.getEntityId()) return train;
            }
        }
        return null;
    }

    SmartCartTrain getTrain(int entityID){
        for(SmartCartTrain train : trainList){
            if(train.leadCart.getEntityId() == entityID) return train;
            for(SmartCartTrainVehicle trainVehicle : train.followCarts){
                if(trainVehicle.getEntityId() == entityID) return train;
            }
        }
        return null;
    }

    void addTrain(SmartCartTrain train){
        trainList.add(train);
    }

    void removeTrain(SmartCartTrain deadTrain){
        for(int i = 0; i < trainList.size(); ++i){
            if(trainList.get(i).leadCart.getEntityId() == deadTrain.leadCart.getEntityId()){
                trainList.remove(i);
                break;
            }
        }
    }

    ArrayList<SmartCartVehicle> getCartList() {
        return new ArrayList<>(cartList);
    }
    // This returns an ArrayList of all carts in the provided world
    ArrayList<SmartCartVehicle> getCartList(World world) {
        // This is where the carts we find, if any, will go
        ArrayList<SmartCartVehicle> worldCarts = new ArrayList<>();
        // Loop through all carts, saving the ones that match
        for (SmartCartVehicle cart : cartList)
            if (cart.getLocation().getWorld() == world) worldCarts.add(cart);
        return worldCarts;
    }

    ArrayList<SmartCartTrainVehicle> getTrainCartList(){
        return new ArrayList<>(trainCartList);
    }

    ArrayList<SmartCartTrainVehicle> getTrainCartList(World world){
        ArrayList<SmartCartTrainVehicle> worldTrainCarts = new ArrayList<>();
        for(SmartCartTrainVehicle cart : trainCartList) {
            if (cart.getLocation().getWorld() == world) worldTrainCarts.add(cart);
        }
        return worldTrainCarts;
    }

    SmartCartVehicle getCartFromList(Minecart requestedCart) {
        // Search for an existing SmartCartVehicle first
        for (SmartCartVehicle cart : cartList)
            if (cart.getEntityId() == requestedCart.getEntityId()) return cart;
        // If the cart doesn't already exist as a SmartCartVehicle, create it
        SmartCartVehicle newCart = new SmartCartVehicle(requestedCart);
        cartList.add(newCart);
        return newCart;
    }

    SmartCartVehicle getCartFromList(int entityID) {
        // Search for an existing SmartCartVehicle first
        for (SmartCartVehicle cart : cartList) {
            if (cart.getEntityId() == entityID) {
                return cart;
            }
        }
        return null;
    }

    private SmartCartTrainVehicle getTrainCartFromList(Minecart requestedCart){
        for(SmartCartVehicle cart : cartList)
            if(cart instanceof SmartCartTrainVehicle && cart.getEntityId() == requestedCart.getEntityId()) return (SmartCartTrainVehicle)cart;
        SmartCartTrainVehicle newCart = new SmartCartTrainVehicle(requestedCart);
        trainCartList.add(newCart);
        return newCart;
    }

    SmartCartTrainVehicle getTrainCartFromList(int entityID){
        for(SmartCartTrainVehicle cart : trainCartList){
            if(cart.getEntityId() == entityID){
                return cart;
            }
        }
        return null;
    }

    // Find & remove the cart from cartList
    void removeCart(SmartCartVehicle deadCart) {
        // Find the cart that is being removed
        for (SmartCartVehicle cart : cartList) {
            if (cart.getEntityId() == deadCart.getEntityId()) {
                cartList.remove(cart);
                break;
            }
        }
    }

    void removeCart(SmartCartTrainVehicle deadCart){
        for(SmartCartTrainVehicle cart : trainCartList){
            if(cart.getEntityId() == deadCart.getEntityId()){
                trainCartList.remove(cart);
                break;
            }
        }
    }

    void killCarts(ArrayList<SmartCartVehicle> removeCartList) {
        for (SmartCartVehicle cart : removeCartList) {
            cart.remove(true);
        }
    }

    private void killTrainCarts(ArrayList<SmartCartTrainVehicle> removeCartList){
        for(SmartCartTrainVehicle cart : removeCartList){
            cart.remove(true);
        }
    }

    void killTrain(SmartCartTrain train){
        removeTrain(train);
        train.leadCart.remove(true);
        killTrainCarts((ArrayList<SmartCartTrainVehicle>)train.followCarts);
    }

    boolean isControlBlock(Block block) {
        Block blockAbove = block.getLocation().add(0,1,0).getBlock();
        return isRail(blockAbove) && (block.getType() == BlockMaterial.ElevatorBlock.material ||
                block.getType() == BlockMaterial.IntersectionBlock.material ||
                block.getType() == BlockMaterial.KillBlock.material ||
                block.getType() == BlockMaterial.SlowBlock.material ||
                block.getType() == BlockMaterial.SpawnBlock.material ||
                block.getType() == BlockMaterial.TrainSpawnBlock.material);
    }

    boolean isElevatorBlock(Block block) {
        return isControlBlock(block) && block.getType() == BlockMaterial.ElevatorBlock.material;
    }

    boolean isSlowBlock(Block block){
        return isControlBlock(block) && block.getType() == BlockMaterial.SlowBlock.material;
    }

    boolean isIntersectionBlock(Block block){
        return isControlBlock(block) && block.getType() == BlockMaterial.IntersectionBlock.material;
    }

    boolean isKillBlock(Block block){
        return isControlBlock(block) && block.getType() == BlockMaterial.KillBlock.material;
    }

    boolean isTrainSpawnBlock(Block block){
        return isControlBlock(block) && block.getType() == BlockMaterial.TrainSpawnBlock.material;
    }

    boolean isSpawnBlock(Block block) {
        return isControlBlock(block) && block.getType() == BlockMaterial.SpawnBlock.material;
    }

    // This searches methodically through a cube with a side length of (radius * 2 + 1)
    //   for the material passed.  Returns an ArrayList of Blocks containing all matching
    //   material found
    ArrayList<Block> getBlocksNearby(Block centerBlock, int radius, Material material) {
        ArrayList<Block> blockList = new ArrayList<>();
        for (double xOffset = radius; xOffset >= radius * -1; xOffset--)
            for (double yOffset = radius; yOffset >= radius * -1; yOffset--)
                for (double zOffset = radius; zOffset >= radius * -1; zOffset--) {
                    Block testBlock = centerBlock.getLocation().add(xOffset, yOffset, zOffset).getBlock();
                    if (testBlock.getType() == material) blockList.add(testBlock);
                }
        return blockList;
    }

    // Send a message to the player
    void sendMessage(Entity entity, String message) {
        message = "§6[smartcart] §7" + message;
        if (entity instanceof Player) ((Player) entity).sendRawMessage(message);
    }

    // This method returns a delineated list of the worlds on the server
    String getWorldList(String separator) {
        List<World> worldList = Bukkit.getWorlds();
        StringBuilder stringBuilder = new StringBuilder();
        int counter = 1;
        for (World world : worldList) {
            stringBuilder.append(world.getName());
            // If we still have more worlds to add, insert separator
            if (worldList.size() > counter) stringBuilder.append(separator);
            counter += 1;
        }
        return stringBuilder.toString();
    }

    void sendFullCartList(ArrayList<SmartCartVehicle> cartList, ArrayList<SmartCartTrainVehicle> trainCartList, Entity entity){
        sendMessage(entity, helperFunction1(2, 7, 10, 18, 20, 5, 0, cartList, trainCartList));
    }

    // Send the provided list of carts to the provided entity
    void sendCartList(ArrayList<SmartCartVehicle> sendCartList, Entity entity) {
        sendMessage(entity, helperFunction1(0, 7, 10, 18, 20, 5, 0, sendCartList, null));
    }

    void sendTrainCartList(ArrayList<SmartCartTrainVehicle> trainCartList, Entity entity){
        sendMessage(entity, helperFunction1(1, 7, 10, 10, 18, 5, 5, null, trainCartList));
    }

    private static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    //public static String padLeft(String s, int n) {
    //    return String.format("%1$" + n + "s", s);
    //}

    private String getLocationString(Location loc) {
        return loc.getBlockX() + ","
                + loc.getBlockY() + ","
                + loc.getBlockZ();
    }

    private String getAgeString(Entity entity) {
        int ticks = entity.getTicksLived();
        int seconds = ticks / 20;
        return String.format("%d:%02d", seconds/60, seconds%60);
    }

    boolean isRail(Block block) {
        return block != null && block.getType() == Material.RAIL;
    }

    //public boolean isRail(Location loc) {
    //   return isRail(loc.getBlock());
    //}

    // Checks to see if a string is really an integer!
    static boolean isInteger(String str) {
        if (str == null) return false;
        int length = str.length();
        if (length == 0) return false;
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) return false;
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c <= '/' || c >= ':') return false;
        }
        return true;
    }

    Minecart getCartAtBlock(Block block) {
        for (Entity entity : block.getLocation().getChunk().getEntities())
            // If the entity is a minecart and on the location of the block passed, return the cart
            if (entity instanceof Minecart
                    && entity.getLocation().getBlockX() == block.getLocation().getX()
                    && entity.getLocation().getBlockY() == block.getLocation().getY()
                    && entity.getLocation().getBlockZ() == block.getLocation().getZ())
                return (Minecart)entity;
        return null;
    }


    SmartCartVehicle spawnCart(Block block) {
        // Check to make sure the block is a rail and no cart already exists here
        Minecart cartAtBlock = getCartAtBlock(block);
        if ( isRail(block) && cartAtBlock == null ) {
            Location loc = block.getLocation().add(0.5D,0D,0.5D);
            Minecart cart = loc.getWorld().spawn(loc, Minecart.class);
            return getCartFromList(cart);
        }
        return getCartFromList(cartAtBlock);
    }

    SmartCartTrainVehicle spawnTrainCart(Block block){
        Minecart cartAtBlock = getCartAtBlock(block);
        if(isRail(block) && cartAtBlock == null){
            Location loc = block.getLocation().add(0.5, 0, 0.5);
            Minecart cart = loc.getWorld().spawn(loc, Minecart.class);
            return getTrainCartFromList(cart);
        }
        return getTrainCartFromList(cartAtBlock);
    }

    Block getElevatorBlock(Location loc) {
        Location cmdLoc = loc.clone();
        for (int y = 0; y < 256; y++) {
            if (y == cmdLoc.getBlockY()) continue;
            loc.setY(y);
            if (isElevatorBlock(loc.getBlock())) return loc.getBlock();
        }
        return null;
    }

    boolean isSign(Block block){
        return block.getType() == Material.OAK_SIGN
                || block.getType() == Material.OAK_WALL_SIGN
                || block.getType() == Material.ACACIA_SIGN
                || block.getType() == Material.ACACIA_WALL_SIGN
                || block.getType() == Material.DARK_OAK_SIGN
                || block.getType() == Material.DARK_OAK_WALL_SIGN
                || block.getType() == Material.SPRUCE_SIGN
                || block.getType() == Material.SPRUCE_WALL_SIGN
                || block.getType() == Material.BIRCH_SIGN
                || block.getType() == Material.BIRCH_WALL_SIGN
                || block.getType() == Material.JUNGLE_SIGN
                || block.getType() == Material.JUNGLE_WALL_SIGN;
    }

    boolean isPoweredSign(Block block){
        return isSign(block) && (block.isBlockPowered() || block.isBlockIndirectlyPowered());
    }

    private String helperFunction1(int mode, int padID, int padWorld, int padLocation, int padPassenger, int padAge, int padIsLeadCart, ArrayList<SmartCartVehicle> cartList, ArrayList<SmartCartTrainVehicle> trainCartList){
        String ret = "";
        StringBuilder stringBuilder = new StringBuilder();
        switch(mode){
            //generic
            case 0:
                ret += "\n" +
                        padRight("ID", padID) +
                        padRight("World", padWorld) +
                        padRight("Location", padLocation) +
                        padRight("Passenger", padPassenger) +
                        padRight("Age", padAge) +
                        "\n";
                for(SmartCartVehicle cart : cartList)
                    stringBuilder.append(helperFunction2(0, cart, padID, padWorld, padLocation, padPassenger, padAge, padIsLeadCart));
                ret += stringBuilder.toString() + "Total: " + cartList.size();
                return ret;
            //train
            case 1:
                ret += "\n"
                        + padRight("ID", padID)
                        + padRight("World", padWorld)
                        + padRight("Location", padLocation)
                        + padRight("Passenger", padPassenger)
                        + padRight("Age", padAge)
                        + padRight("Lead", padIsLeadCart)
                        + "\n";
                for(SmartCartTrainVehicle cart : trainCartList)
                    stringBuilder.append(helperFunction2(1, cart, padID, padWorld, padLocation, padPassenger, padAge, padIsLeadCart));
                ret += stringBuilder.toString() + "Total: " + trainCartList.size();
                return ret;
            //both
            case 2:
                ret += "\n" +
                        padRight("ID", padID) +
                        padRight("World", padWorld) +
                        padRight("Location", padLocation) +
                        padRight("Passenger", padPassenger) +
                        padRight("Age", padAge) +
                        "\n";
                for (SmartCartVehicle cart : cartList)
                    stringBuilder.append(helperFunction2(0, cart, padID, padWorld, padLocation, padPassenger, padAge, padIsLeadCart));
                for (SmartCartTrainVehicle cart : trainCartList)
                    stringBuilder.append(helperFunction2(0, cart, padID, padWorld, padLocation, padPassenger, padAge, padIsLeadCart));
                ret += stringBuilder.toString() + "Total: " + (cartList.size() + trainCartList.size());
                return ret;
        }
        return "";
    }

    private String helperFunction2(int mode, SmartCartVehicle cart, int padID, int padWorld, int padLocation, int padPassenger, int padAge, int padLeadCart){
        switch(mode){
            case 0:
                return helperFunction3(cart, padID, padWorld, padLocation, padPassenger, padAge) + "\n";
            case 1:
                return helperFunction3(cart, padID, padWorld, padLocation, padPassenger, padAge) +
                        padRight(((SmartCartTrainVehicle)cart).isLeadCart() ? "Yes" : "No", padLeadCart) + "\n";
        }
        return "";
    }

    private String helperFunction3(SmartCartVehicle cart, int padID, int padWorld, int padLocation, int padPassenger, int padAge){
        String ret = "";
        ret += padRight(Integer.toString(cart.getEntityId()), padID) +
                padRight(cart.getLocation().getWorld().getName(), padWorld) +
                padRight(getLocationString(cart.getLocation()), padLocation) +
                padRight(cart.getPassengerName(), padPassenger) +
                padRight(getAgeString(cart.getCart()), padAge);
        return ret;
    }

    void helperFunction5(Object context, Location location){
        Block[] block = {null, null, null, null, null, null, null, null, null};
        helperFunction6(block, location);
        if(context instanceof SmartCartTrain){
            if(SmartCart.util.isSign(block[0])) ((SmartCartTrain)context).executeSign(block[0]);
            if(SmartCart.util.isSign(block[1])) ((SmartCartTrain)context).executeSign(block[1]);
            if(SmartCart.util.isSign(block[2])) ((SmartCartTrain)context).executeSign(block[2]);
            if(SmartCart.util.isSign(block[3])) ((SmartCartTrain)context).executeSign(block[3]);
            if(SmartCart.util.isSign(block[4])) ((SmartCartTrain)context).executeSign(block[4]);
            if(SmartCart.util.isSign(block[5])) ((SmartCartTrain)context).executeSign(block[5]);
            if(SmartCart.util.isSign(block[6])) ((SmartCartTrain)context).executeSign(block[6]);
            if(SmartCart.util.isSign(block[7])) ((SmartCartTrain)context).executeSign(block[7]);
            if(SmartCart.util.isSign(block[8])) ((SmartCartTrain)context).executeSign(block[8]);
        }
        else if(context instanceof SmartCartTrainVehicle){
            if(SmartCart.util.isSign(block[0])) ((SmartCartTrainVehicle)context).executeSign(block[0]);
            if(SmartCart.util.isSign(block[1])) ((SmartCartTrainVehicle)context).executeSign(block[1]);
            if(SmartCart.util.isSign(block[2])) ((SmartCartTrainVehicle)context).executeSign(block[2]);
            if(SmartCart.util.isSign(block[3])) ((SmartCartTrainVehicle)context).executeSign(block[3]);
            if(SmartCart.util.isSign(block[4])) ((SmartCartTrainVehicle)context).executeSign(block[4]);
            if(SmartCart.util.isSign(block[5])) ((SmartCartTrainVehicle)context).executeSign(block[5]);
            if(SmartCart.util.isSign(block[6])) ((SmartCartTrainVehicle)context).executeSign(block[6]);
            if(SmartCart.util.isSign(block[7])) ((SmartCartTrainVehicle)context).executeSign(block[7]);
            if(SmartCart.util.isSign(block[8])) ((SmartCartTrainVehicle)context).executeSign(block[8]);
        }
        else if(context instanceof SmartCartVehicle){
            if(SmartCart.util.isSign(block[0])) ((SmartCartVehicle)context).executeSign(block[0]);
            if(SmartCart.util.isSign(block[1])) ((SmartCartVehicle)context).executeSign(block[1]);
            if(SmartCart.util.isSign(block[2])) ((SmartCartVehicle)context).executeSign(block[2]);
            if(SmartCart.util.isSign(block[3])) ((SmartCartVehicle)context).executeSign(block[3]);
            if(SmartCart.util.isSign(block[4])) ((SmartCartVehicle)context).executeSign(block[4]);
            if(SmartCart.util.isSign(block[5])) ((SmartCartVehicle)context).executeSign(block[5]);
            if(SmartCart.util.isSign(block[6])) ((SmartCartVehicle)context).executeSign(block[6]);
            if(SmartCart.util.isSign(block[7])) ((SmartCartVehicle)context).executeSign(block[7]);
            if(SmartCart.util.isSign(block[8])) ((SmartCartVehicle)context).executeSign(block[8]);

        }
    }

    void helperFunction6(Block[] block, Location location){
       block[0] = location.add(0, -2, 0).getBlock();
       block[1] = location.add(1, -1, 0).getBlock();
       block[2] = location.add(-1, -1, 0).getBlock();
       block[3] = location.add(0, -1, 1).getBlock();
       block[4] = location.add(1, -1, -1).getBlock();
       block[5] = location.add(1, 0, 0).getBlock();
       block[6] = location.add(-1, 0, 0).getBlock();
       block[7] = location.add(0, 0, 1).getBlock();
       block[8] = location.add(0, 0, -1).getBlock();
    }

    void helperFunction7(Object context, Block[] blocks, Entity passenger){
        if(context instanceof SmartCartTrain){
            if (SmartCart.util.isSign(blocks[0])) ((SmartCartTrain)context).executeEJT(blocks[0]);
            if (SmartCart.util.isSign(blocks[1])) ((SmartCartTrain)context).executeEJT(blocks[1]);
            if (SmartCart.util.isSign(blocks[2])) ((SmartCartTrain)context).executeEJT(blocks[2]);
            if (SmartCart.util.isSign(blocks[3])) ((SmartCartTrain)context).executeEJT(blocks[3]);
            if (SmartCart.util.isSign(blocks[4])) ((SmartCartTrain)context).executeEJT(blocks[4]);
            if (SmartCart.util.isSign(blocks[5])) ((SmartCartTrain)context).executeEJT(blocks[5]);
            if (SmartCart.util.isSign(blocks[6])) ((SmartCartTrain)context).executeEJT(blocks[6]);
            if (SmartCart.util.isSign(blocks[7])) ((SmartCartTrain)context).executeEJT(blocks[7]);
            if (SmartCart.util.isSign(blocks[8])) ((SmartCartTrain)context).executeEJT(blocks[8]);
        }
        else if(context instanceof SmartCartVehicle){
            if (SmartCart.util.isSign(blocks[0])) ((SmartCartVehicle)context).executeEJT(passenger, blocks[0]);
            if (SmartCart.util.isSign(blocks[1])) ((SmartCartVehicle)context).executeEJT(passenger, blocks[1]);
            if (SmartCart.util.isSign(blocks[2])) ((SmartCartVehicle)context).executeEJT(passenger, blocks[2]);
            if (SmartCart.util.isSign(blocks[3])) ((SmartCartVehicle)context).executeEJT(passenger, blocks[3]);
            if (SmartCart.util.isSign(blocks[4])) ((SmartCartVehicle)context).executeEJT(passenger, blocks[4]);
            if (SmartCart.util.isSign(blocks[5])) ((SmartCartVehicle)context).executeEJT(passenger, blocks[5]);
            if (SmartCart.util.isSign(blocks[6])) ((SmartCartVehicle)context).executeEJT(passenger, blocks[6]);
            if (SmartCart.util.isSign(blocks[7])) ((SmartCartVehicle)context).executeEJT(passenger, blocks[7]);
            if (SmartCart.util.isSign(blocks[8])) ((SmartCartVehicle)context).executeEJT(passenger, blocks[8]);
        }
    }
}