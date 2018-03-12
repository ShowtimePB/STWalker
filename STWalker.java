package STWalker;

import STWalker.teleportation.TierFour;
import STWalker.teleportation.jewelry.Jewelry;
import STWalker.teleportation.teleports.Teleport;
import STWalker.thirdtier.FollowElkoy;
import STWalker.thirdtier.SpiritTreeObstacle;
import STWalker.thirdtier.TierThreeObstacle;
import org.powerbot.script.Condition;
import org.powerbot.script.Random;
import org.powerbot.script.Tile;
import org.powerbot.script.rt4.*;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * Created by Chris on 05/01/2016.
 *
 * This is a modified version of the walker class created by Chris. It has been modified to handle
 * not only doors and ladders/stairs. But also more complex objects such as agility shortcuts and the
 * wilderness ditch. If you wish to add more feel free to make a pull request, remember to push
 * updates so that we can expand this database!
 *
 * Thanks
 *       - ST
 */
public class STWalker {

    protected ClientContext ctx;
    private ArrayList<ComplexObstacle> obstacleList = new ArrayList<>();
    private ArrayList<TierThreeObstacle> tierThreeList = new ArrayList<>();
    ArrayList<TierFour> tierFourList = new ArrayList<>();



    public STWalker(ClientContext ctx) {
        this.ctx = ctx;
        initObstacles();
    }

    /**
     * Returns the next tile to walk to, disregarding whether it is reachable.
     *
     * @param t The tile path being traversed.
     * @return The next tile to traverse to.
     */
    public Tile getNextTile(Tile[] t) {
        Tile nextTile = ctx.movement.newTilePath(t).next();                 //The next tile, as suggested by the RSBot api, this will be the next reachable tile.
        int index = 0;                                                      //The index at which the next tile (by our definition) is at. Default to 0 (start tile).
        final Player p = ctx.players.local();

        /*
         * Loop through the path, backwards.
         * Find the intended next tiles index
         * then check if there is a better option.
         */
        for (int i = t.length - 1; i >= 0; i--) {
            if (t[i].equals(nextTile)) {                                    //This is the index at which the suggested next tile resides
                if (i + 1 <= t.length - 1 && nextTile.distanceTo(p) < 3) {  //If we're not at the end of the path and we're very close to the suggested next tile
                    index = i + 1;                                          //then it's too close to bother with. We will try to go to the tile after instead.
                    break;
                }
                index = i;                                                  //Suggested next tile was the best option as it is not very close, and reachable.
                break;
            } else if(t[i].distanceTo(p)<8){
                index = i;                                                  //if next closest tile is <8 and it's not the next tile then we can assume the next tile is probably not correct, perhaps no reachable tile available...
                break;
            }
        }
        return t[index];
    }

    /**
     * Will detect which obstacle to tackle to give Walker the ability to
     * traverse to the next tile.     *
     *
     * @param t The tile path being traversed.
     * @return True if obstacle was clicked. False otherwise.
     */
    public boolean handleObstacle(Tile[] t) {
        Tile nextTile = getNextTile(t);                                       //The calculated next tile.
        /*
         * Return false as there is no obstacle to handle.
         * Perhaps this was called whilst we were still walking
         * and the tile became reachable?
         */
        if (nextTile.matrix(ctx).reachable() && ctx.movement.distance(nextTile) < 20) {
            return false;
        }

        final Player p = ctx.players.local();
        double distance = Double.POSITIVE_INFINITY;
        GameObject obstacle = null;
        for (GameObject go : ctx.objects.select(5).name(Pattern.compile("(staircase)|(ladder)|((.*)?door)|(gate(.*)?)", Pattern.CASE_INSENSITIVE))) {                            //Changed to filter by name else all the filtering in the next part takes much too long to be considered effective..
            double calcDist = go.tile().distanceTo(new Tile(nextTile.x(), nextTile.y(), p.tile().floor()));
            /*
             * Check if the next tile is on a different floor. If it is then
             * we need to check whether it's up or down. We can use this
             * to filter objects by actions 'Climb-up' or 'Climb-down'
             */
            if (nextTile.floor() != p.tile().floor()) {
                if (go.type() == GameObject.Type.INTERACTIVE && go.actions().length>0) {
                    if (nextTile.floor() > p.tile().floor()) {                      //Need to climb up.
                        if (calcDist < distance && reachable(go)) {
                            for (String s : go.actions()) {
                                if (s != null && !s.equals("null") && s.contains("Climb-up")) {
                                    obstacle = go;
                                    distance = calcDist;                                                            //Set the distance to the object so we can compare future objects against it to determine the best.
                                    break;
                                }
                            }
                        }
                    } else {                                                     //Need to climb down.
                        if (calcDist < distance && reachable(go)) {
                            for (String s : go.actions()) {
                                if (s != null && !s.equals("null") && s.contains("Climb-down")) {
                                    obstacle = go;
                                    distance = calcDist;                                                            //Set the distance to the object so we can compare future objects against it to determine the best.
                                    break;
                                }
                            }
                        }
                    }
                }
            } else if (nextTile.distanceTo(ctx.players.local()) > 50){          //we can now assume that we need to go up or down .. not flawless whatsoever
                if (go.type() != GameObject.Type.BOUNDARY) {
                    for (String s : go.actions()) {
                        if (go.tile().distanceTo(nextTile) + go.tile().distanceTo(p) < distance && reachable(go)) {
                            if (s != null && !s.equals("null") && (s.contains("Climb-up") || s.contains("Climb-down") || s.contains("Enter"))) {
                                obstacle = go;
                                distance = go.tile().distanceTo(nextTile) + go.tile().distanceTo(p);              //Set the distance to the object so we can compare future objects against it to determine the best.
                                break;
                            }
                        }
                    }
                }
            } else {
                /*
                 * Floor was the same so we are blocked by a door, or gate.
                 * These are boundary objects, however we need to check the name
                 * as some random boundary objects appear with the name null.
                 */
                if (go.type() == GameObject.Type.BOUNDARY) {
                    if (calcDist < distance && reachable(go)) {
                        obstacle = go;
                        distance = calcDist;                                                            //Set the distance to the object so we can compare future objects against it to determine the best.
                    }
                }
            }
        }

        if (obstacle != null) {
            if (obstacle.inViewport()) {
                obstacle.bounds(getBounds(obstacle));
                if (nextTile.floor() > p.tile().floor()) {                                                      //Going up.
                    if (obstacle.interact("Climb-up")) {
                        return handlePostInteraction();
                    }
                } else if (nextTile.floor() < p.tile().floor()) {                                               //Going down.
                    if (obstacle.interact("Climb-down")) {
                        return handlePostInteraction();
                    }
                } else if (nextTile.distanceTo(ctx.players.local()) > 50) {                                      //This is just guessing
                    if (obstacle.interact("Climb-")) {
                        return handlePostInteraction();
                    }
                } else {
                    if (obstacle.interact("Open")) {                                                        //Going through.
                        return handlePostInteraction();
                    }
                }
            } else {
                if (ctx.movement.step(obstacle)) {                                                           //Can't see the obstacle, step towards it.
                    ctx.camera.turnTo(obstacle);                                                            //and turn the camera.
                    Condition.wait(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return p.animation() == -1 && !p.inMotion();
                        }
                    }, 1000, 3);
                }
            }
        }

        //If none of that was successful, call on complex obstacle function
        return handleComplexObstacle(ctx.players.local().tile(), nextTile);
    }

    /**
     * Determines if a game object is reachable by
     * checking the tiles around it.
     *
     * @param go The game object being tested.
     * @return True or false.
     */
    private boolean reachable(GameObject go) {
        int a = go.width();
        Tile t1 = new Tile(go.tile().x() + a, go.tile().y(), go.tile().floor());
        Tile t2 = new Tile(go.tile().x() - a, go.tile().y(), go.tile().floor());
        Tile t3 = new Tile(go.tile().x(), go.tile().y() + a, go.tile().floor());
        Tile t4 = new Tile(go.tile().x(), go.tile().y() - a, go.tile().floor());

        return (t1.matrix(ctx).reachable() || t2.matrix(ctx).reachable() || t3.matrix(ctx).reachable() || t4.matrix(ctx).reachable());
    }

    /**
     * Handles the period after interacting with the object.
     *
     * @return True if clicked successfully.
     */
    private boolean handlePostInteraction() {
        final Player p = ctx.players.local();
        if (ctx.game.crosshair() == Game.Crosshair.ACTION) {
            Condition.wait(() -> p.animation() == -1 && !p.inMotion(), 1000, 3);
            return true;
        }
        return false;

    }

    /**
     * Moves a single step towards the destination of a given path.
     * Most obstacles such as gates, ladders, doors, and stairs are handled.
     *
     * @param t The tile path to be traversed.
     * @return True if a step was taken, or object handled.
     */
    public boolean walkPath(Tile[] t) {
        Tile ti = getNextTile(t);
        Tile nt = randomizeTile(ti, 0);

        if(nt.matrix(ctx).reachable() && ctx.movement.distance(nt) < 20){
            Condition.wait(() -> ctx.players.local().tile().distanceTo(ctx.movement.destination()) < 6, 40, 40);
            return ctx.movement.step(nt);
        }

        return ti.matrix(ctx).reachable() && ctx.movement.distance(ti) < 20 ? ctx.movement.step(ti) : handleObstacle(t);
    }

    /**
     * Gets the bounds for a given object
     *
     * @param go The object to get bounds for.
     * @return Bounds. If not found, default bounds returned.
     */
    private int[] getBounds(GameObject go) {
        switch (getType(go.name())) {
            case DOOR:
                switch (go.orientation()) {
                    case 0:
                    case 4:
                    case 6:
                        return new int[]{-5, 15, 0, -220, 0, 90};
                    case 1:
                        return new int[]{0, 90, 0, -220, 110, 130};
                    case 2:
                        return new int[]{100, 120, 0, -220, 0, 90};
                    case 3:
                        return new int[]{0, 100, 0, -200, 10, 20};
                }
                break;
            case GATE:
                switch (go.orientation()) {
                    case 0:
                    case 4:
                    case 6:
                        return new int[]{5, 10, 0, -80, 20, 80};
                    case 1:
                        return new int[]{0, 80, 0, -80, 118, 123};
                    case 2:
                        return new int[]{118, 123, 0, -80, 0, 80};
                    case 3:
                        return new int[]{10, 80, 0, -80, 15, 0};
                }
                break;
            case LADDER:
                switch (go.orientation()) {
                    case 0:
                    case 4:
                    case 2:
                        return new int[]{-20, 40, 20, -40, 0, -60};
                    case 1:
                    case 5:
                        return new int[]{0, -40, -64, 0, -32, 32};
                    case 6:
                        return new int[]{-20, 40, 20, -40, 0, 60};
                    case 3:
                    case 7:
                        return new int[]{10, 40, -64, 0, -32, 32};
                }
                break;
        }

        return new int[]{-32, 32, -64, 0, -32, 32};
    }

    /**
     * Gets the enum associated with a given string, if applicable.
     *
     * @param s The string to check (name of object?)
     * @return The enum associated.
     */
    private Type getType(String s) {
        if (s.matches("(?i)((.*)?door)")) {
            return Type.DOOR;
        } else if (s.matches("(?i)(gate(.*)?)")) {
            return Type.GATE;
        } else if (s.equalsIgnoreCase("ladder")) {
            return Type.LADDER;
        }
        return Type.TEAPOT;
    }

    /**
     * Similar to <code>derive</code>. However it makes sure that the given
     * tile isn't behind a wall/gate or similar. Use in place of derive.
     *
     * @param t Tile you're modifying
     * @param iterations Enter 0
     * @return Returns randomized tile
     */

    private Tile randomizeTile(Tile t, int iterations){

        if(iterations == 4) return t;

        int randX = Random.nextInt(-2 + (iterations/2), 2 - (iterations/2)) + t.x();
        int randY = Random.nextInt(-2 + (iterations/2), 2 - (iterations/2)) + t.y();

        int steps = ctx.movement.distance(new Tile(randX, randY), t);


        //Potentially confusing, it checks to make sure that steps isn't too high
        //Checks to make sure steps isn't -1 (implies it's not reachable)
        //Then returns the selected tile if it's true, or recursively calls the function if false
        return (steps < 5 && steps != -1) ? new Tile(randX, randY): randomizeTile(t, iterations + 1);

    }

    /**
     * Reverses the given path, and then calls {@link #walkPath(Tile[] t)}.
     *
     * @param t
     * @return
     */
    public boolean walkPathReverse(Tile[] t){
        t = ctx.movement.newTilePath(t).reverse().toArray();

        return walkPath(t);
    }

    public enum Type {
        DOOR, GATE, LADDER, TEAPOT
    }

    /**
     * Handles complex obstacles (Tier 2/3)
     * \
     *
     * Includes agility shortcuts, does a level check as well
     *
     *
     * @param t Current Tile
     * @param d Destination tile
     * @return True if successful, false otherwise
     */

    private boolean handleComplexObstacle(Tile t, Tile d){

        ComplexObstacle co = null;

        for(ComplexObstacle c: obstacleList){
            if(c.getStartTile().distanceTo(ctx.players.local().tile()) < 12 && c.getStartTile().matrix(ctx).reachable()){
                if(c.getEndTile().distanceTo(d) < 12){
                    if(c.getStartTile().matrix(ctx).reachable()){
                        co = c;
                        break;
                    }
                }
            }else{
                if(c.isReversible() && c.getEndTile().distanceTo(ctx.players.local().tile()) < 12 && c.getEndTile().matrix(ctx).reachable()){
                    if(c.getStartTile().distanceTo(d) < 12){
                        if(c.getEndTile().matrix(ctx).reachable()){
                            co = c;
                            break;
                        }
                    }
                }
            }
        }

        if(co != null){

            if(co.getAgilityLevel() > ctx.skills.level(Constants.SKILLS_AGILITY)){
                return false;
            }

            Tile start;
            int type;

            if(co.getStartTile().matrix(ctx).reachable()){
                start = co.getStartTile();
                type = -1;
            }else{
                start = co.getEndTile();
                type = 1;
            }

            //Checking distance to start of obstacle
            if(start.distanceTo(ctx.players.local().tile()) < 12 && start.matrix(ctx).reachable()){

                GameObject GO = ctx.objects.select().id(co.getID()).nearest().poll();
                turnToObject(GO);

                if(GO.inViewport()){
                    GO.bounds(co.getBounds(type));
                    GO.interact(co.getInteraction());
                    return handlePostInteraction();
                }
            }
        }


        /*
         * This area handles Tier 3 obstacles
         * These are unique objects such as spirit trees, that require a different function
         * as they have custom code in order to work
         */

        for(TierThreeObstacle CO: tierThreeList){
            if(CO instanceof SpiritTreeObstacle){
                if(d.distanceTo(CO.getEndTile()) < 4){
                    if(reachable(ctx.objects.select().name("Spirit tree").poll())){
                        turnToObject(ctx.objects.select().name("Spirit tree").poll());
                        return CO.traverse();
                    }
                }
            }

            if(CO instanceof FollowElkoy){
                if(d.distanceTo(CO.getEndTile()) < 7){
                    if(ctx.npcs.select().id(((FollowElkoy) CO).getElkoyID()).poll().tile().matrix(ctx).reachable()){
                        turnToObject(ctx.npcs.select().id(((FollowElkoy) CO).getElkoyID()).poll());
                        return CO.traverse();
                    }
                }
            }
        }

        /*
         * Tier Four
         */

        for(TierFour tf: tierFourList){
            if(tf instanceof Jewelry){
                if(tf.getEndTile().distanceTo(d) < 7){
                    ctx.game.tab(Game.Tab.EQUIPMENT);
                    if(ctx.equipment.itemAt(((Jewelry) tf).getSlot()).name().contains(((Jewelry) tf).getName())){
                        ctx.equipment.itemAt(((Jewelry) tf).getSlot()).interact(tf.getInteraction());
                        Condition.wait(() -> d.distanceTo(ctx.players.local().tile()) < 8, 250, 8);
                        ctx.game.tab(Game.Tab.INVENTORY);
                        return true;
                    }
                }
            }else if(tf instanceof Teleport){
                if(tf.getEndTile().distanceTo(d) < 10){
                    if(((Teleport) tf).getTeletabID() == -1){
                        if(((Teleport) tf).hasRunes()){
                            ctx.game.tab(Game.Tab.MAGIC);
                            ctx.magic.cast(((Teleport) tf).getSpell());
                            Condition.wait(() -> d.distanceTo(ctx.players.local().tile()) < 10, 250, 8);
                            ctx.game.tab(Game.Tab.INVENTORY);
                        }
                    }else{
                        ctx.inventory.select().id(((Teleport) tf).getTeletabID()).poll().interact("Break");
                        Condition.wait(() -> d.distanceTo(ctx.players.local().tile()) < 10, 250, 8);
                    }
                }
            }
        }
        ctx.game.tab(Game.Tab.INVENTORY);



        /*
         * Last ditch effort to find something, do a quick check to make sure it still isn't reachable
         * Then it goes through every "nearby" GameObject trying to match IDs
         */
        if(d.matrix(ctx).reachable()) return true;

        for(GameObject GO: ctx.objects.select().within(16).select(gameObject -> gameObject.type().equals(GameObject.Type.INTERACTIVE))){
            for(ComplexObstacle CO: obstacleList){
                if(CO.getID() == GO.id()){

                    if(CO.getAgilityLevel() > ctx.skills.level(Constants.SKILLS_AGILITY)){
                        return false;
                    }

                    Tile start;
                    int type;

                    if(CO.getStartTile().matrix(ctx).reachable()){
                        start = CO.getStartTile();
                        type = -1;
                    }else{
                        start = CO.getEndTile();
                        type = 1;
                    }

                    //Checking distance to start of obstacle
                    if(start.distanceTo(ctx.players.local().tile()) < 12 && start.matrix(ctx).reachable()){

                        GameObject go = ctx.objects.select().id(CO.getID()).nearest().poll();
                        turnToObject(go);

                        if(go.inViewport()){
                            go.bounds(CO.getBounds(type));
                            go.interact(CO.getInteraction());
                            return handlePostInteraction();
                        }
                    }
                }
            }
        }

        return false;
    }


    /**
     * Inits the various tier 2 and tier 3 obstacles
     *
     * Tier 2 is considered all one-click obstacles, such as crumbling walls, loose railings etc.
     * Tier 3 is considered multi-click obstacles, such as Spirit trees, or NPCs
     *
     */
    private void initObstacles(){

        //...All the ComplexObstacle objects...//

        ComplexObstacle faladorCrumblingWall = new ComplexObstacle(new Tile(2936, 3355), new Tile(2934, 3355), "Climb-over", 24222, true, 5);

        ComplexObstacle gnomeVillageLooseRailing = new ComplexObstacle(new Tile(2515, 3160), new Tile(2515, 3161), "Squeeze-through", 2186, true);
        gnomeVillageLooseRailing.setBounds(new int[]{8, 131, -135, -24, -3, 12}, 0);

        ComplexObstacle trollheimStrongholdEntrance = new ComplexObstacle(new Tile(2840, 3690), new Tile(2837, 10090, 2), "Enter", 3771, false);
        trollheimStrongholdEntrance.setBounds(new int[]{-49, 19, -46, 305, -100, 122}, -1);

        ComplexObstacle shantayPass = new ComplexObstacle(new Tile(3304, 3117), new Tile(3304, 3115), "Go-through", 4031, true);


        //...End decs...//

        obstacleList.add(faladorCrumblingWall);
        obstacleList.add(gnomeVillageLooseRailing);
        obstacleList.add(trollheimStrongholdEntrance);
        obstacleList.add(shantayPass);


        SpiritTreeObstacle gnomeVillage = new SpiritTreeObstacle(new Tile(2542, 3170), "Travel", ctx.widgets.widget(187).component(3).component(0), ctx);
        FollowElkoy outOfVillage = new FollowElkoy(new Tile(2504, 3192), "Follow", ctx);

        tierThreeList.add(gnomeVillage);
        tierThreeList.add(outOfVillage);


        /**
         * Beginning of fourth tier
         */

        final int LAW_RUNE_ID = 563;
        final int AIR_RUNE_ID = 556;
        final int FIRE_RUNE_ID = 554;
        final int WATER_RUNE_ID = 555;
        final int EARTH_RUNE_ID = 557;


        Jewelry duelArena = new Jewelry(new Tile(3315, 3235), "Duel Arena", ctx, Equipment.Slot.RING, "Ring of duel");
        Teleport varrockSpell = new Teleport(new Tile(3212, 3426), "", ctx, Magic.Spell.VARROCK_TELEPORT, new int[]{LAW_RUNE_ID, AIR_RUNE_ID, FIRE_RUNE_ID}, new int[]{1, 3, 1});

        tierFourList.add(duelArena);
        tierFourList.add(varrockSpell);
    }

    private void turnToObject(GameObject GO){

        if(GO.inViewport()) return;

        if(GO.tile().distanceTo(ctx.players.local().tile()) > 7 && !GO.inViewport()){
            int randX = Random.nextInt(-1,1) + GO.tile().x();
            int randY = Random.nextInt(-1,1) + GO.tile().y();
            ctx.movement.step(new Tile(randX, randY));
            Condition.wait(() -> ctx.movement.destination().distanceTo(ctx.players.local().tile()) < 4 || GO.inViewport(), 10, 80);

        }

        if(GO.inViewport()){
            return;

        }else{

            while(!ctx.controller.isStopping() && !GO.inViewport()) {
                ctx.camera.turnTo(GO);
            }

        }
    }

    private void turnToObject(Npc GO){

        if(GO.inViewport()) return;

        if(GO.tile().distanceTo(ctx.players.local().tile()) > 7 && !GO.inViewport()){
            int randX = Random.nextInt(-1,1) + GO.tile().x();
            int randY = Random.nextInt(-1,1) + GO.tile().y();
            ctx.movement.step(new Tile(randX, randY));
            Condition.wait(() -> ctx.movement.destination().distanceTo(ctx.players.local().tile()) < 4 || GO.inViewport(), 10, 80);

        }

        if(GO.inViewport()){
            return;

        }else{

            while(!ctx.controller.isStopping() && !GO.inViewport()) {
                ctx.camera.turnTo(GO);
            }

        }
    }


}