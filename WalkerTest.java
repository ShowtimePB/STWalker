package STWalker;

import org.powerbot.script.PaintListener;
import org.powerbot.script.PollingScript;
import org.powerbot.script.Script;
import org.powerbot.script.Tile;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.TileMatrix;

import java.awt.*;

//@Script.Manifest(
//        name = "STWalker",
//        description = "Walking Test",
//        properties = "author=ShowtimeScripts; topic=0; client=4;")

public class WalkerTest extends PollingScript<ClientContext> implements PaintListener{


    Color lightGreen = new Color(0, 255, 0, 80);
    Color darkGreen = new Color(0, 255, 0);
    Color lightRed = new Color(255, 0, 0, 80);
    Color darkRed = new Color(255, 0, 0);

    @Override
    public void poll() {

        STWalker w = new STWalker(ctx);

        final Tile[] path = {new Tile(3315, 3234, 0), new Tile(3311, 3234, 0), new Tile(3307, 3234, 0), new Tile(3303, 3232, 0), new Tile(3300, 3229, 0), new Tile(3297, 3226, 0), new Tile(3297, 3221, 0), new Tile(3297, 3217, 0), new Tile(3298, 3213, 0), new Tile(3298, 3209, 0), new Tile(3298, 3205, 0), new Tile(3297, 3201, 0), new Tile(3297, 3197, 0), new Tile(3295, 3193, 0), new Tile(3294, 3189, 0), new Tile(3291, 3186, 0), new Tile(3288, 3183, 0), new Tile(3285, 3180, 0), new Tile(3281, 3180, 0), new Tile(3280, 3176, 0), new Tile(3280, 3172, 0), new Tile(3280, 3168, 0), new Tile(3280, 3164, 0), new Tile(3280, 3159, 0), new Tile(3280, 3155, 0), new Tile(3280, 3151, 0), new Tile(3284, 3148, 0), new Tile(3288, 3145, 0), new Tile(3292, 3143, 0), new Tile(3296, 3143, 0), new Tile(3299, 3140, 0), new Tile(3301, 3135, 0), new Tile(3302, 3131, 0), new Tile(3302, 3127, 0), new Tile(3302, 3123, 0), new Tile(3303, 3119, 0), new Tile(3304, 3115, 0), new Tile(3305, 3110, 0), new Tile(3309, 3107, 0), new Tile(3312, 3104, 0)};

        w.walkPath(path);


    }

    @Override
    public void repaint(Graphics graphics) {

//        Graphics2D g = (Graphics2D)graphics;
//
//        int x = ctx.players.local().tile().x();
//        int y = ctx.players.local().tile().y();
//
//        for(int i = -5; i < 5; i++){
//            for(int j = -5; j < 5; j++){
//                Tile t = new Tile(x + i, y + j);
//                if(t.matrix(ctx).reachable()){
//                    paintTiles(g, t, lightGreen, darkGreen);
//                }else{
//                    paintTiles(g, t, lightRed, darkRed);
//                }
//            }
//        }

    }

    private void paintTiles(Graphics2D g2d, Tile t, Color light, Color deep){

        TileMatrix matrix = t.matrix(ctx);

        g2d.setColor(light);
        g2d.fillPolygon(matrix.bounds());
        g2d.setColor(deep);
        g2d.drawPolygon(matrix.bounds());


    }
}
