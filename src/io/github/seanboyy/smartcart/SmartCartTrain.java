package io.github.seanboyy.smartcart;


import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

class SmartCartTrain{
    SmartCartTrainVehicle leadCart;
    List<SmartCartTrainVehicle> followCarts = new ArrayList<>();

    SmartCartTrain(SmartCartTrainVehicle leadCart){
        this.leadCart = leadCart;
    }

    boolean shouldKillTrain(){
        if(!leadCart.getCart().isEmpty()) return false;
        for(SmartCartTrainVehicle trainVehicle : followCarts){
            if(!trainVehicle.getCart().isEmpty()) return false;
        }
        return true;
    }

    void executeSign(Block block){
        //TODO: make trains execute signs
    }

    void executeControl(){
        //TODO: make trains execute control blocks
    }

    void setVelocity(int x, int z){
        setVelocity((double)x, (double)z);
    }

    private void setVelocity(double x, double z){
        leadCart.getCart().setVelocity(new Vector(x * leadCart.getConfigSpeed(), 0, z * leadCart.getConfigSpeed()));
        for (SmartCartTrainVehicle followCart : followCarts) {
            followCart.getCart().setVelocity(new Vector(x * leadCart.getConfigSpeed(), 0, z * leadCart.getConfigSpeed()));
        }
    }

    void readControlSign() {
        Block block1 = leadCart.getCart().getLocation().add(0, -2, 0).getBlock();
        Block block2 = leadCart.getCart().getLocation().add(1, -1, 0).getBlock();
        Block block3 = leadCart.getCart().getLocation().add(-1, -1, 0).getBlock();
        Block block4 = leadCart.getCart().getLocation().add(0, -1, 1).getBlock();
        Block block5 = leadCart.getCart().getLocation().add(1, -1, -1).getBlock();
        Block block6 = leadCart.getCart().getLocation().add(1, 0, 0).getBlock();
        Block block7 = leadCart.getCart().getLocation().add(-1, 0, 0).getBlock();
        Block block8 = leadCart.getCart().getLocation().add(0, 0, 1).getBlock();
        Block block9 = leadCart.getCart().getLocation().add(0, 0, -1).getBlock();
        if(SmartCart.util.isSign(block1)) executeSign(block1);
        if(SmartCart.util.isSign(block2)) executeSign(block2);
        if(SmartCart.util.isSign(block3)) executeSign(block3);
        if(SmartCart.util.isSign(block4)) executeSign(block4);
        if(SmartCart.util.isSign(block5)) executeSign(block5);
        if(SmartCart.util.isSign(block6)) executeSign(block6);
        if(SmartCart.util.isSign(block7)) executeSign(block7);
        if(SmartCart.util.isSign(block8)) executeSign(block8);
        if(SmartCart.util.isSign(block9)) executeSign(block9);
    }
}