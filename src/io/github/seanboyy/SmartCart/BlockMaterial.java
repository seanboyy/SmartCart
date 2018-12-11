package io.github.seanboyy.SmartCart;

import org.bukkit.Material;

import static org.bukkit.Material.*;

public enum BlockMaterial {
    ElevatorBlock       (RED_WOOL),
    IntersectionBlock   (GREEN_WOOL),
    KillBlock           (YELLOW_WOOL),
    SlowBlock           (ORANGE_WOOL),
    SpawnBlock          (BLACK_WOOL);

    public Material material;

    BlockMaterial(Material material){
        this.material = material;
    }

    public void setMaterial(String material){
        this.material = Material.valueOf(material);
    }
}
