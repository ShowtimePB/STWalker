package STWalker.thirdtier;

import STWalker.STWalker;
import org.powerbot.script.Tile;
import org.powerbot.script.rt4.ClientContext;

public abstract class TierThreeObstacle {

    private Tile endTile;
    private String interaction;
    protected ClientContext ctx;

    public TierThreeObstacle(Tile endtile, String interaction, ClientContext ctx){
        this.endTile = endtile;
        this.interaction = interaction;
        this.ctx = ctx;
    }

    public Tile getEndTile() {
        return endTile;
    }

    public String getInteraction() {
        return interaction;
    }

    public abstract boolean traverse();
}
