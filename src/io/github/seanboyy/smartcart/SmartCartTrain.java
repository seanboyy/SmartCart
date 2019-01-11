package io.github.seanboyy.smartcart;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

class SmartCartTrain{
    SmartCartTrainVehicle leadCart;
    List<SmartCartTrainVehicle> followCarts = new ArrayList<>();

    SmartCartTrain(SmartCartTrainVehicle leadCart){
        this.leadCart = leadCart;
    }

    private void setTag(String tag){
        leadCart.setTag(tag);
        for(SmartCartTrainVehicle followCart : followCarts){
            followCart.setTag(tag);
        }
    }

    boolean shouldKillTrain(){
        if(!leadCart.getCart().isEmpty()) return false;
        for(SmartCartTrainVehicle trainVehicle : followCarts){
            if(!trainVehicle.getCart().isEmpty()) return false;
        }
        return true;
    }

    private void sendPassengersMessage(String message, boolean prefix){
        if(prefix) message = "§6[smartcart] §7" + message;
        else message = "§7" + message;
        Entity entity = leadCart.getPassenger();
        if(entity instanceof Player) ((Player)entity).sendRawMessage(message);
        for(SmartCartTrainVehicle followCart : followCarts){
            Entity followEntity = followCart.getPassenger();
            if(followEntity instanceof Player) ((Player)followEntity).sendRawMessage(message);
        }
    }

    //Differences from generic carts: Trains are unaffected by $LNC
    void executeSign(Block block){
        if (leadCart.isNotOnRail()) return;
        Sign sign = (Sign) block.getState(); // Cast to Sign
        for (Pair<String, String> pair : SmartCartVehicle.parseSign(sign)) {
            Pattern p;
            if (pair.left().equals("$SPD")) {
                if(!leadCart.helperFunctionSPD(pair.right())) return;
                leadCart.setConfigSpeed(Double.parseDouble(pair.right()));
            }
            if (pair.left().equals("$LOCK")){
                leadCart.setLocked(true);
                for(SmartCartTrainVehicle followCart : followCarts){
                    followCart.setLocked(true);
                }
            }
            if (pair.left().equals("$UNLOCK")){
                leadCart.setLocked(false);
                for(SmartCartTrainVehicle followCart : followCarts){
                    followCart.setLocked(false);
                }
            }
            if (pair.left().equals("$LEV")){
                leadCart.getCart().setFlyingVelocityMod(new Vector(1, 0, 1));
                leadCart.getCart().setDerailedVelocityMod(new Vector(1, 0, 1));
                for(SmartCartTrainVehicle followCart : followCarts){
                    followCart.getCart().setFlyingVelocityMod(new Vector(1, 0, 1));
                    followCart.getCart().setDerailedVelocityMod(new Vector(1, 0, 1));
                }
            }
            if (pair.left().equals("$PLM")){
                int y = SmartCartUtil.isInteger(pair.right()) ? Integer.parseInt(pair.right()) : 1;
                if (y < 1) y = 1;
                leadCart.getCart().setFlyingVelocityMod(new Vector(1, y, 1));
                leadCart.getCart().setDerailedVelocityMod(new Vector(1, y, 1));
                for(SmartCartTrainVehicle followCart : followCarts){
                    followCart.getCart().setFlyingVelocityMod(new Vector(1, y, 1));
                    followCart.getCart().setDerailedVelocityMod(new Vector(1, y, 1));
                }
            }
            if (pair.left().equals("$MSG")) sendPassengersMessage(pair.right(), false);
            if (pair.left().equals("&ENDs")) setTag(pair.right());
            if (pair.left().equals("$TAGs")) setTag(pair.right());
            if (pair.left().equals("$END")) {
                setTag(pair.right());
                sendPassengersMessage("Endpoint set to §a" + pair.right(), true);
            }
            if (pair.left().equals("$TAG")) {
                setTag(pair.right());
                sendPassengersMessage("Set tag to §a" + pair.right(), true);
            }
            if(pair.left().equals("$HOLD")) {
                if(SmartCart.util.isPoweredSign(block) && !leadCart.doOnceSet){
                    leadCart.setHeld(true);
                    leadCart.setPreviousVelocity(leadCart.getCart().getVelocity());
                    leadCart.getCart().setVelocity(new Vector(0, 0, 0));
                    leadCart.doOnceRelease = false;
                    leadCart.doOnceSet = true;
                }
                else if(!leadCart.doOnceRelease){
                    leadCart.setHeld(false);
                    if(leadCart.getPreviousVelocity() != null) leadCart.getCart().setVelocity(leadCart.getPreviousVelocity());
                    leadCart.setPreviousVelocity(leadCart.getCart().getVelocity());
                    leadCart.doOnceRelease = true;
                    leadCart.doOnceSet = false;
                }
            }
        }
    }

    //Differences from generic carts: trains ignore intersections and elevators
    void executeControl(){
        if (leadCart.getCart().getPassengers().isEmpty()) return;
        Block block = leadCart.getBlockBeneath();
        if(SmartCart.util.isSlowBlock(block)){
            leadCart.setPreviousMaterial(BlockMaterial.SlowBlock);
            leadCart.setSpeed(SmartCart.config.getDouble("slow_cart_speed"));
            for(SmartCartTrainVehicle followCart : followCarts){
                followCart.setSpeed(SmartCart.config.getDouble("slow_cart_speed"));
            }
        }
        if(SmartCart.util.isKillBlock(block)){
            if(leadCart.isLeavingBlock()) {
                leadCart.remove(true);
                for(SmartCartTrainVehicle followCart : followCarts){
                    followCart.remove(true);
                }
                SmartCart.util.removeTrain(this);
                Block[] blocks = {null, null, null, null, null, null, null, null, null};
                SmartCart.util.helperFunction6(blocks, leadCart.getCart().getLocation());
                SmartCart.util.helperFunction7(this, blocks, null);
            }
            else {
                leadCart.setSpeed(0.1D);
            }
        }
    }

    void setVelocity(int x, int z){
        setVelocity((double)x, (double)z);
    }

    private void setVelocity(double x, double z){
        leadCart.getCart().setVelocity(new Vector(x * leadCart.getConfigSpeed(), leadCart.getCart().getVelocity().getY(), z * leadCart.getConfigSpeed()));
        for (SmartCartTrainVehicle followCart : followCarts) {
            followCart.getCart().setVelocity(new Vector(x * leadCart.getConfigSpeed(), followCart.getCart().getVelocity().getY(), z * leadCart.getConfigSpeed()));
        }
    }

    void executeEJT(Block block){
        Sign sign = (Sign) block.getState();
        List<Entity> passengers = new ArrayList<>();
        if(leadCart.getPassenger() != null) passengers.add(leadCart.getPassenger());
        for(SmartCartTrainVehicle followCart : followCarts){
            if(followCart.getPassenger() != null) passengers.add(followCart.getPassenger());
        }
        for (Pair<String, String> pair : SmartCartVehicle.parseSign(sign)) {
            if (pair.left().equals("$EJT") && pair.right().length() >= 2) {
                int dist = SmartCartUtil.isInteger(pair.right().substring(1)) ? Integer.parseInt(pair.right().substring(1)) : 0;
                for(Entity passenger : passengers) {
                    SmartCartVehicle.helperFunctionEJT(pair.right(), passenger, dist);
                }
            }
        }

    }
}