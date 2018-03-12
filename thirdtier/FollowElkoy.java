package STWalker.thirdtier;

import org.powerbot.script.Condition;
import org.powerbot.script.Tile;
import org.powerbot.script.rt4.ClientContext;

public class FollowElkoy extends TierThreeObstacle{

    private int elkoyID = 4968;

    public FollowElkoy(Tile endtile, String interaction, ClientContext ctx) {
        super(endtile, interaction, ctx);
    }

    @Override
    public boolean traverse() {

        Tile t = ctx.players.local().tile();
        ctx.npcs.select().id(elkoyID).poll().interact(getInteraction());
        Condition.wait(() -> t.distanceTo(ctx.players.local().tile()) > 12, 400, 6);

        return false;
    }





    public int getElkoyID() {
        return elkoyID;
    }
}
