package io.github.seanboyy.smartcart;

import org.bukkit.Location;
import org.bukkit.block.Block;
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
    void executeSign(Block sign){
        if(leadCart) SmartCart.util.getTrain(this).executeSign(sign);
    }

    @Override
    void executeControl(){
        if(leadCart) SmartCart.util.getTrain(this).executeControl();
    }
}
