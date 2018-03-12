package STWalker.teleportation;

import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.Tile;


public class TierFour {

    private Tile endTile;
    private String interaction;
    protected ClientContext ctx;

    /**
     * Tier four is human supplied items/methods. This can include more custom methods such as spells
     * or it can include something like jewelry, or a teletab.
     *
     * @param endTile Destination tile, end of teleport
     * @param interaction Interaction, e.g. for a glory it would be "Edgeville"
     * @param ctx ctx
     */
    public TierFour(Tile endTile, String interaction, ClientContext ctx){
        this.endTile = endTile;
        this.interaction = interaction;
        this.ctx = ctx;
    }


    public Tile getEndTile() {
        return endTile;
    }

    public String getInteraction() {
        return interaction;
    }
}
