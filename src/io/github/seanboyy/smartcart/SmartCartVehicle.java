//
// smartcart copyright 2015 Ian Clark
//
// Distributed under the MIT License
// http://opensource.org/licenses/MIT
//
package io.github.seanboyy.smartcart;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.*;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SmartCartVehicle{

    Minecart cart;
    private BlockMaterial previousMaterial;
    private Location currentLocation;
    private Location previousLocation;
    private int[] currentRoughLocation;
    private int[] previousRoughLocation;
    private int emptyCartTimer = 0;
    private boolean locked = false;
    private Vector previousVelocity;
    // Settables
    private double configSpeed = SmartCart.config.getDouble("normal_cart_speed");
    private String tag = "";
    private boolean held = false;
    boolean doOnceSet = false;
    boolean doOnceRelease = false;

    SmartCartVehicle(Minecart vehicle){
        cart = vehicle;
        cart.setMaxSpeed(SmartCart.config.getDouble("max_cart_speed"));
    }

    // Accessors
    Minecart getCart() {
        return cart;
    }
    private Location getPreviousLocation() {
        return previousLocation;
    }
    double getConfigSpeed() {
        return configSpeed;
    }
    void setConfigSpeed(Double speed) {
        configSpeed = speed;
    }
    private BlockMaterial getPreviousMaterial() {
        return previousMaterial;
    }
    void setPreviousMaterial(BlockMaterial material) {
        previousMaterial = material;
    }
    void saveCurrentLocation() {
        previousLocation = currentLocation;
        currentLocation = getLocation();
        previousRoughLocation = currentRoughLocation;
        currentRoughLocation = new int[] {
                currentLocation.getBlockX(), currentLocation.getBlockY(), currentLocation.getBlockZ()
        };
    }
    boolean isHeld() {
        return held;
    }
    void setHeld(boolean held){
        this.held = held;
    }
    boolean isLocked() {
        return locked;
    }
    void setLocked(boolean locked){
        this.locked = locked;
    }
    void setTag(String tag){
        this.tag = tag;
    }
    String getTag() {
        return tag;
    }
    void setPreviousVelocity(Vector oldVelocity){
        previousVelocity = new Vector(oldVelocity.getX(), oldVelocity.getY(), oldVelocity.getZ());
    }
    Vector getPreviousVelocity(){
        return previousVelocity;
    }

    // These methods just pass through to the Minecart class
    int getEntityId() {
        return getCart().getEntityId();
    }
    Entity getPassenger() {
        return getCart().getPassengers().isEmpty() ? null : getCart().getPassengers().get(0);
    }
    Location getLocation() {
        return getCart().getLocation();
    }

    boolean isNewBlock() {
        return !Arrays.equals(currentRoughLocation, previousRoughLocation);
    }

    void setEmptyCartTimer() {
        if(
                SmartCart.config.getBoolean("empty_cart_timer_ignore_commandminecart", true) && isCommandMinecart() ||
                        SmartCart.config.getBoolean("empty_cart_timer_ignore_explosiveminecart", true) && isExplosiveMinecart() ||
                        SmartCart.config.getBoolean("empty_cart_timer_ignore_storagemincart", true) && isStorageMinecart() ||
                        SmartCart.config.getBoolean("empty_cart_timer_ignore_hoppermincart", true) && isHopperMinecart() ||
                        SmartCart.config.getBoolean("empty_cart_timer_ignore_poweredmincart", true) && isPoweredMinecart() ||
                        SmartCart.config.getBoolean("empty_cart_timer_ignore_spawnermincart", true) && isSpawnerMinecart() ||
                        SmartCart.config.getInt("empty_cart_timer") == 0
                ) {
            emptyCartTimer = 0;
        } else {
            emptyCartTimer += 1;
            if (emptyCartTimer > SmartCart.config.getInt("empty_cart_timer") * 20) {
                remove(true);
            }
        }
    }

    void resetEmptyCartTimer() {
        emptyCartTimer = 0;
    }

    // Returns the block beneath the rail
    Block getBlockBeneath() {
        return getCart().getLocation().add(0D, -1D, 0D).getBlock();
    }

    // Returns true only if cart is in a rail block
    boolean isNotOnRail() {
        return getCart().getLocation().getBlock().getType() != Material.RAIL;
    }

    // Returns true if the cart is directly above a control block
    boolean isOnControlBlock() {
        return SmartCart.util.isControlBlock( getCart().getLocation().add(0D, -1D, 0D).getBlock() );
    }

    // This looks two blocks below the rail for a sign. Sets the signText variable to
    //   the sign contents if the sign is a valid control sign, otherwise "".
    void readControlSign() {
        SmartCart.util.helperFunction5(this, getCart().getLocation());
    }

    boolean isMoving() {
        Vector velocity = getCart().getVelocity();
        return (velocity.getX() != 0D || velocity.getZ() != 0D);
    }

    // Sets the speed to the max, in the direction the cart is already travelling
    void setSpeed(double speed) {
        // Check if the cart is empty, and if we should boost empty carts
        if (getCart().isEmpty() && !SmartCart.config.getBoolean("boost_empty_carts")) return;
        Vector velocity = getCart().getVelocity();
        // If the cart is moving
        if (isMoving()) {
            // Maintain velocity
            Vector newVelocity = new Vector();
            // Check to see which axis we're moving along and use that to set the new vector
            //   The signum function just returns 1 if passed a positive, -1 if negative
            if (Math.abs(velocity.getX()) > Math.abs(velocity.getZ())) newVelocity.setX(Math.signum(velocity.getX()) * speed);
            else newVelocity.setZ(Math.signum(velocity.getZ()) * speed);
            // Update the velocity
            getCart().setVelocity(newVelocity);
        }
    }

    // Destroy the cart (from plugin & server)
    void remove(boolean kill) {
        Entity passenger = getPassenger();
        // Move the passenger out of the cart
        if (passenger != null) {
            // Get location of passenger & add 1 to Y value)
            Location loc = passenger.getLocation();
            loc.setX( cart.getLocation().getBlockX() + 0.5D );
            loc.setY( cart.getLocation().getBlockY());
            loc.setZ( cart.getLocation().getBlockZ() + 0.5D );
            passenger.teleport(loc);
        }
        // Remove from list of carts
        SmartCart.util.removeCart(this);
        // If we need to kill the actual cart, kill it
        if (kill) getCart().remove();
    }

    private void transferSettings(SmartCartVehicle newSC) {
        newSC.setConfigSpeed(configSpeed);
        newSC.setTag(tag);
    }

    void executeControl() {
        if (getCart().getPassengers().isEmpty()) return;
        Block block = getBlockBeneath();
        if(SmartCart.util.isSlowBlock(block)){
            setPreviousMaterial(BlockMaterial.SlowBlock);
            setSpeed(SmartCart.config.getDouble("slow_cart_speed"));
        }
        if(SmartCart.util.isKillBlock(block)){
            if(isLeavingBlock()) {
                Entity passenger = cart.getPassengers().get(0);
                remove(true);
                Block[] blocks = {null, null, null, null, null, null, null, null, null};
                SmartCart.util.helperFunction6(blocks, getCart().getLocation());
                SmartCart.util.helperFunction7(this, blocks, passenger);
            }
            else setSpeed(0.1D);
        }
        if(SmartCart.util.isIntersectionBlock(block)) {
            if(getPreviousMaterial() == BlockMaterial.IntersectionBlock) {
                Block blockAhead;
                if(isMoving() && (blockAhead = getBlockAheadPassenger()) != null) {
                    Entity passenger = getCart().getPassengers().get(0);
                    if(SmartCart.util.isRail(blockAhead)) {
                        remove(true);
                        SmartCartVehicle newSC = SmartCart.util.spawnCart(blockAhead);
                        newSC.getCart().addPassenger(passenger);
                        transferSettings(newSC);
                    }
                }
                return;
            }
            if(isLeavingBlock()) {
                setPreviousMaterial(BlockMaterial.IntersectionBlock);
                setSpeed(0D);
                sendPassengerMessage("Move in the direction you wish to go.", true);
            }
            else setSpeed(0.1D);
        }
        if(SmartCart.util.isElevatorBlock(block)) {
            if(!isLeavingBlock()) {
                setSpeed(0.1D);
                return;
            }
            if(getPreviousMaterial() == BlockMaterial.ElevatorBlock) return;
            setPreviousMaterial(BlockMaterial.ElevatorBlock);
            Block elevator = SmartCart.util.getElevatorBlock(block.getLocation());
            if(elevator == null) return;
            Block tpTarget = elevator.getLocation().add(0, 1, 0).getBlock();
            Entity passenger = getCart().getPassengers().get(0);
            Vector cartVelocity = getCart().getVelocity();
            Location passengerLoc = passenger.getLocation();
            passengerLoc.setY(tpTarget.getLocation().getBlockY());
            remove(true);
            SmartCartVehicle newCart = SmartCart.util.spawnCart(tpTarget);
            passenger.teleport(passengerLoc);
            newCart.getCart().addPassenger(passenger);
            newCart.getCart().setVelocity(cartVelocity);
            newCart.setSpeed(1);
            newCart.setPreviousMaterial(BlockMaterial.ElevatorBlock);
            transferSettings(newCart);
        }
    }

    String getPassengerName() {
        if (getCart().getPassengers().isEmpty()) return "None";
        return getCart().getPassengers().get(0).getName();
    }

    // Returns the block directly ahead of the passenger
    private Block getBlockAheadPassenger() {
        // Get the passenger's direction as an integer
        //   -1/3 = pos x
        //   -2/2 = neg z
        //   -3/1 = neg x
        //   -4/0 = pos z
        if(getCart().getPassengers().isEmpty()) return null;
        int passengerDir = Math.round( getCart().getPassengers().get(0).getLocation().getYaw() / 90f );
        Block block = null;
        switch (passengerDir) {
            case 0:
            case -4:
                block = getCart().getLocation().add(0,0,1).getBlock();
                break;
            case 1:
            case -3:
                block = getCart().getLocation().add(-1,0,0).getBlock();
                break;
            case 2:
            case -2:
                block = getCart().getLocation().add(0,0,-1).getBlock();
                break;
            case 3:
            case -1:
                block = getCart().getLocation().add(1,0,0).getBlock();
                break;
        }
        return block;
    }

    // Find out if the cart is headed towards or away from the middle of the current block
    boolean isLeavingBlock() {
        // Gotta check to make sure this exists first
        if (getPreviousLocation() == null) return false;
        // If we just moved to a new block, the previous location is invalid for this check
        if (getPreviousLocation().getBlockX() != getLocation().getBlockX()
                || getPreviousLocation().getBlockZ() != getLocation().getBlockZ()) {
            // This lets you chain control blocks by setting the prev wool color to null unless we
            // just got off an elevator.
            if (Math.abs(getPreviousLocation().getBlockY() - getLocation().getBlockY()) < 2) setPreviousMaterial(null);
            return false;
        }
        // Get the previous and current locations
        double prevX = Math.abs( getPreviousLocation().getX() );
        double prevZ = Math.abs( getPreviousLocation().getZ() );
        double currX = Math.abs( getLocation().getX() );
        double currZ = Math.abs( getLocation().getZ() );
        // Just get the decimal part of the double
        prevX = prevX - (int) prevX;
        prevZ = prevZ - (int) prevZ;
        currX = currX - (int) currX;
        currZ = currZ - (int) currZ;
        // Get distance from the middle of the block
        double prevDistFromMidX = Math.abs( prevX - 0.5 );
        double prevDistFromMidZ = Math.abs( prevZ - 0.5 );
        double currDistFromMidX = Math.abs( currX - 0.5 );
        double currDistFromMidZ = Math.abs( currZ - 0.5 );
        return currDistFromMidX > prevDistFromMidX || currDistFromMidZ > prevDistFromMidZ || (currDistFromMidX < 0.1 && currDistFromMidZ < 0.1);
    }

    private void sendPassengerMessage(String message, boolean prefix){
        if(prefix) message = "§6[smartcart] §7" + message;
        else message = "§7" + message;
        Entity entity = getPassenger();
        if(entity instanceof Player) ((Player)entity).sendRawMessage(message);
    }

    private boolean isCommandMinecart() {
        return getCart() instanceof CommandMinecart;
    }

    private boolean isExplosiveMinecart() {
        return  getCart() instanceof ExplosiveMinecart;
    }

    private boolean isHopperMinecart() {
        return getCart() instanceof HopperMinecart;
    }

    private boolean isPoweredMinecart() {
        return getCart() instanceof PoweredMinecart;
    }

    private boolean isSpawnerMinecart() {
        return  getCart() instanceof SpawnerMinecart;
    }

    private boolean isStorageMinecart() {
        return getCart() instanceof StorageMinecart;
    }

    static List<Pair<String, String>> parseSign(Sign sign){
        List<Pair<String, String>> ret = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for( String value : sign.getLines() ) stringBuilder.append(value);
        String text = stringBuilder.toString();
        // Check to see if the sign string matches the control sign prefix; return otherwise
        Pattern p = Pattern.compile(SmartCart.config.getString("control_sign_prefix_regex"));
        Matcher m = p.matcher(text);
        // Return if the control prefix isn't matched
        if (!m.find()) return new ArrayList<>();
        String signText = m.replaceAll(""); // Remove the control prefix
        for(String pair : signText.split("\\|")) {
            if(!pair.contains(":")) ret.add(new Pair<>(pair, ""));
            else {
                String[] tokens = pair.split(":");
                tokens[0] = tokens[0].replaceAll("\\s+", "");
                if (!tokens[0].contains("MSG")) tokens[1] = tokens[1].replaceAll("\\s+", "");
                ret.add(new Pair<>(tokens[0], tokens[1]));
            }
        }
        return ret;
    }

    private void spawnCartInNewDirection(SmartCartVehicle oldCart, String direction){
        Entity[] passenger = {null};
        Block[] blockAhead = {null};
        Vector[] vector = {new Vector(0, 0, 0)};
        helperFunction1(oldCart, direction, blockAhead, vector, passenger);
        if (SmartCart.util.isRail(blockAhead[0])) {
            oldCart.remove(true);
            SmartCartVehicle newSC = SmartCart.util.spawnCart(blockAhead[0]);
            newSC.getCart().addPassenger(passenger[0]);
            newSC.getCart().setVelocity(vector[0]);
            oldCart.transferSettings(newSC);
        }
    }

    void executeSign(Block block) {
        if (isNotOnRail()) return;
        boolean foundEndpoint = false;
        Sign sign = (Sign) block.getState(); // Cast to Sign
        for (Pair<String, String> pair : parseSign(sign)) {
            Pattern p;
            if (pair.left().equals("$LNC"))
                if (SmartCart.util.isSpawnBlock(getCart().getLocation().add(0, -1, 0).getBlock())) spawnCartInNewDirection(this, pair.right());
            if (pair.left().equals("$SPD")) {
                if(!helperFunctionSPD(pair.right())) return;
                configSpeed = Double.parseDouble(pair.right());
            }
            if(pair.left().equals("$LOCK")){
                setLocked(true);
            }
            if(pair.left().equals("$UNLOCK")){
                setLocked(false);
            }
            if(pair.left().equals("$LEV")){
                getCart().setFlyingVelocityMod(new Vector(1, 0, 1));
                getCart().setDerailedVelocityMod(new Vector(1, 0, 1));
            }
            if(pair.left().equals("$PLM")){
                int y = Integer.parseInt(pair.right());
                if (y < 1) y = 1;
                getCart().setFlyingVelocityMod(new Vector(1, y, 1));
                getCart().setDerailedVelocityMod(new Vector(1, y, 1));
            }
            if (pair.left().equals("$MSG")) sendPassengerMessage(pair.right(), false);
            if (pair.left().equals("&ENDs")) setTag(pair.right());
            if (pair.left().equals("$TAGs")) setTag(pair.right());
            if (pair.left().equals("$END")) {
                setTag(pair.right());
                sendPassengerMessage("Endpoint set to §a" + pair.right(), true);
            }
            if (pair.left().equals("$TAG")) {
                setTag(pair.right());
                sendPassengerMessage("Set tag to §a" + pair.right(), true);
            }
            if (pair.left().equals("$N"))
                if (cart.getVelocity().getZ() < 0) spawnCartInNewDirection(this, pair.right());
            if (pair.left().equals("$E"))
                if (cart.getVelocity().getX() > 0) spawnCartInNewDirection(this, pair.right());
            if (pair.left().equals("$W"))
                if (cart.getVelocity().getX() < 0) spawnCartInNewDirection(this, pair.right());
            if (pair.left().equals("$S"))
                if (cart.getVelocity().getZ() > 0) spawnCartInNewDirection(this, pair.right());
            if(pair.left().equals("$HOLD")) {
                if(SmartCart.util.isPoweredSign(block) && !doOnceSet){
                    setHeld(true);
                    setPreviousVelocity(cart.getVelocity());
                    getCart().setVelocity(new Vector(0, 0, 0));
                    doOnceRelease = false;
                    doOnceSet = true;
                }
                else if(/*!smartcart.util.isPoweredSign(block)) &&*/ !doOnceRelease){
                    setHeld(false);
                    if(getPreviousVelocity() != null) getCart().setVelocity(getPreviousVelocity());
                    setPreviousVelocity(cart.getVelocity());
                    doOnceRelease = true;
                    doOnceSet = false;
                }
            }
            if (pair.left().equals(tag) || pair.left().equals("$DEF")) {
                // Skip this if we already found and used the endpoint
                Entity[] passenger = {null};
                Block[] blockAhead = {null};
                Vector[] vector = {null};
                helperFunction1(this, pair.right(), blockAhead, vector, passenger);
                if(foundEndpoint || passenger[0] == null) return;
                foundEndpoint = true;
                if (SmartCart.util.isRail(blockAhead[0])) {
                    remove(true);
                    SmartCartVehicle newSC = SmartCart.util.spawnCart(blockAhead[0]);
                    newSC.getCart().addPassenger(passenger[0]);
                    newSC.getCart().setVelocity(vector[0]);
                    transferSettings(newSC);
                }
            }
        }
    }

    void executeEJT(Entity passenger, Block block){
        Sign sign = (Sign) block.getState();
        for (Pair<String, String> pair : parseSign(sign)) {
            if (pair.left().equals("$EJT") && pair.right().length() >= 2) {
                int dist = SmartCartUtil.isInteger(pair.right()) ? Integer.parseInt(pair.right().substring(1)) : 0;
                helperFunctionEJT(pair.right(), passenger, dist);
            }
        }
    }

    void helperFunction1(SmartCartVehicle oldCart, String direction, Block[] blockAhead, Vector[] vector, Entity[] passenger){
        if (!oldCart.cart.isEmpty()) {
            passenger[0] = oldCart.cart.getPassengers().get(0);
        }
        switch (direction) {
            case "N":
                blockAhead[0] = oldCart.cart.getLocation().add(0D, 0D, -1D).getBlock();
                vector[0] = new Vector(0, 0, -1);
                break;
            case "S":
                blockAhead[0] = oldCart.cart.getLocation().add(0D, 0D, 1D).getBlock();
                vector[0] = new Vector(0, 0, 1);
                break;
            case "E":
                blockAhead[0] = oldCart.cart.getLocation().add(1D, 0D, 0D).getBlock();
                vector[0] = new Vector(1, 0, 0);
                break;
            case "W":
                blockAhead[0] = oldCart.cart.getLocation().add(-1D, 0D, 0D).getBlock();
                vector[0] = new Vector(-1, 0, 0);
                break;
        }
    }

    boolean helperFunctionSPD(String string){
        Pattern p = Pattern.compile("^\\d*\\.?\\d+");
        double minSpeed = 0D;
        double maxSpeed = SmartCart.config.getDouble("max_cart_speed");
        if (!p.matcher(string).find() || Double.parseDouble(string) > maxSpeed || Double.parseDouble(string) < minSpeed) {
            sendPassengerMessage("Bad speed value: \"" + string + "\". Must be a numeric value (decimals OK) between "
                    + minSpeed + " and " + maxSpeed + ".", true);
            return false;
        }
        return true;
    }

    static void helperFunctionEJT(String string, Entity passenger, int dist){
        switch (string.charAt(0)) {
            case 'N':
                passenger.teleport(passenger.getLocation().add(0, 0, -dist));
                break;
            case 'E':
                passenger.teleport(passenger.getLocation().add(dist, 0, 0));
                break;
            case 'S':
                passenger.teleport(passenger.getLocation().add(0, 0, dist));
                break;
            case 'W':
                passenger.teleport(passenger.getLocation().add(-dist, 0, 0));
                break;
            case 'U':
                passenger.teleport(passenger.getLocation().add(0, dist, 0));
                break;
            case 'D':
                passenger.teleport(passenger.getLocation().add(0, -dist, 0));
                break;
        }
    }
}
