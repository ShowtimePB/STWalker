package STWalker.thirdtier;

import org.powerbot.script.Condition;
import org.powerbot.script.Tile;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.Component;
import org.powerbot.script.rt4.GameObject;

public class SpiritTreeObstacle extends TierThreeObstacle {

    Component w;


    /**
     *
     * @param endtile The EXACT tile you get teleported to
     * @param interaction Interaction string, "Travel" in this case
     * @param w "Widget.Component for the teleport
     */
    public SpiritTreeObstacle(Tile endtile, String interaction, Component w, ClientContext ctx) {
        super(endtile, interaction, ctx);
        this.w = w;
    }

    @Override
    public boolean traverse(){

        System.out.println(ctx.objects.select().name("Spirit tree").poll());
        GameObject tree = ctx.objects.select().name("Spirit tree").nearest().poll();

        if(!w.valid()){
            tree.interact(this.getInteraction());
            Condition.wait(() -> w.valid(), 550, 7);
        }

        if(w.valid()){
            w.click();
            Condition.wait(() -> !tree.valid(), 100, 30);
            if(!tree.valid()) return true;
        }

        if(this.getEndTile().distanceTo(ctx.players.local().tile()) < 4) return true;


        return false;
    }


    public Component getW() {
        return w;
    }
}
