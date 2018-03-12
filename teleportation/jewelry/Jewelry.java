package STWalker.teleportation.jewelry;

import STWalker.teleportation.TierFour;
import org.powerbot.script.Tile;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.Equipment;

public class Jewelry extends TierFour{

    Equipment.Slot slot;
    String name;

    public Jewelry(Tile endTile, String interaction, ClientContext ctx, Equipment.Slot slot, String name) {
        super(endTile, interaction, ctx);
        this.slot = slot;
        this.name = name;
    }

    public Equipment.Slot getSlot() {
        return slot;
    }

    public String getName() {
        return name;
    }
}
