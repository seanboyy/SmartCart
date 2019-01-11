package io.github.seanboyy.smartcart;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

class SmartCartTrainVehicle extends SmartCartVehicle {
    private boolean leadCart;
    boolean isLeadCart(){
        return leadCart;
    }
    void setLeadCart(boolean leadCart){
        this.leadCart = leadCart;
    }
    SmartCartTrainVehicle(Minecart vehicle){
        super(vehicle);
    }

    @Override
    void setSpeed(double speed){
        if(!leadCart) return;
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
            for(SmartCartTrainVehicle followCart : SmartCart.util.getTrain(this).followCarts){
                followCart.getCart().setVelocity(newVelocity);
            }
        }

    }

    @Override
    void remove(boolean kill){
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

    @Override
    void executeSign(Block block){
        boolean foundEndpoint = false;
        if (isNotOnRail()) return;
        Sign sign = (Sign)block.getState(); // Cast to Sign
        for (Pair<String, String> pair : SmartCartVehicle.parseSign(sign)) {
            if (pair.left().equals(getTag()) || pair.left().equals("$DEF")) {
                // Skip this if we already found and used the endpoint
                Entity passenger = null;
                if (!cart.getPassengers().isEmpty()) passenger = cart.getPassengers().get(0);
                if (foundEndpoint || passenger == null) return;
                foundEndpoint = true;
                Block blockAhead = null;
                Vector vector = new Vector(0, 0, 0);
                switch (pair.right()) {
                    case "N":
                        blockAhead = cart.getLocation().add(0D, 0D, -1D).getBlock();
                        vector = new Vector(0, 0, -1);
                        break;
                    case "S":
                        blockAhead = cart.getLocation().add(0D, 0D, 1D).getBlock();
                        vector = new Vector(0, 0, 1);
                        break;
                    case "E":
                        blockAhead = cart.getLocation().add(1D, 0D, 0D).getBlock();
                        vector = new Vector(1, 0, 0);
                        break;
                    case "W":
                        blockAhead = cart.getLocation().add(-1D, 0D, 0D).getBlock();
                        vector = new Vector(-1, 0, 0);
                        break;
                }
                if (SmartCart.util.isRail(blockAhead)) {
                    remove(true);
                    SmartCartTrainVehicle newSC = SmartCart.util.spawnTrainCart(blockAhead);
                    newSC.getCart().addPassenger(passenger);
                    newSC.getCart().setVelocity(vector);
                    transferSettings(newSC);
                }
            }
            if (pair.left().equals("$N"))
                if (cart.getVelocity().getZ() < 0) spawnTrainCartInNewDirection(this, pair.right());
            if (pair.left().equals("$E"))
                if (cart.getVelocity().getX() > 0) spawnTrainCartInNewDirection(this, pair.right());
            if (pair.left().equals("$W"))
                if (cart.getVelocity().getX() < 0) spawnTrainCartInNewDirection(this, pair.right());
            if (pair.left().equals("$S"))
                if (cart.getVelocity().getZ() > 0) spawnTrainCartInNewDirection(this, pair.right());
        }
        if(leadCart) SmartCart.util.getTrain(this).executeSign(block);
    }

    @Override
    void executeControl(){
        if(leadCart) SmartCart.util.getTrain(this).executeControl();
    }

    @Override
    void readControlSign(){
        if(!leadCart) {
            SmartCart.util.helperFunction5(this, getCart().getLocation());
        }
    }

    private void transferSettings(SmartCartTrainVehicle vehicle){
        vehicle.setConfigSpeed(getConfigSpeed());
        vehicle.setTag(getTag());
        vehicle.leadCart = leadCart;
        if(vehicle.leadCart) SmartCart.util.getTrain(this).leadCart = vehicle;
        else{
            for(int i = 0; i < SmartCart.util.getTrain(this).followCarts.size(); ++i){
                if(this.getEntityId() == SmartCart.util.getTrain(this).followCarts.get(i).getEntityId()){
                    SmartCart.util.getTrain(this).followCarts.remove(i);
                    SmartCart.util.getTrain(this).followCarts.add(vehicle);
                    break;
                }
            }
        }
    }

    private void spawnTrainCartInNewDirection(SmartCartTrainVehicle oldCart, String direction){
        Block[] blockAhead = {null};
        Vector[] vector = {new Vector(0, 0, 0)};
        Entity[] passenger = {null};
        helperFunction1(oldCart, direction, blockAhead, vector, passenger);
        if (SmartCart.util.isRail(blockAhead[0])) {
            oldCart.remove(true);
            SmartCartTrainVehicle newSC = SmartCart.util.spawnTrainCart(blockAhead[0]);
            newSC.getCart().addPassenger(passenger[0]);
            newSC.getCart().setVelocity(vector[0]);
            oldCart.transferSettings(newSC);
        }
    }
}
