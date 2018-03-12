package STWalker;

import org.powerbot.script.Tile;

public class ComplexObstacle {

    private Tile startTile;
    private Tile endTile;
    private String interaction;
    private int ID;
    private boolean reversible;
    private int agilityLevel = -1;
    private int[] entranceBounds = {-32, 32, -64, 0, -32, 32};
    private int[] exitBounds = {-32, 32, -64, 0, -32, 32};

    public ComplexObstacle(Tile s, Tile e, String i, int ID, boolean reversible){
        this.startTile = s;
        this.endTile = e;
        this.interaction = i;
        this.ID = ID;
        this.reversible = reversible;
    }

    /**
     * Complex Obstacle object
     * Note: This works by getting the beginning and end tile, ensure that they are correct!!!
     *
     * @param s Starting tile, this should be the EXACT tile at the start of the obstacle
     * @param e End tile, this should be the EXACT tile at the end of the obstacle
     * @param i This is the interact string, for example a staircase would be: "Open"
     * @param ID The ID of the GameObject
     * @param reversible True if this object can be traversed both ways, and the bounds/interaction/id are the same for both ways
     * @param agilityLevel Optional, if it's an agility shortcut this is the required level
     */
    public ComplexObstacle(Tile s, Tile e, String i, int ID, boolean reversible, int agilityLevel){
        this.startTile = s;
        this.endTile = e;
        this.interaction = i;
        this.ID = ID;
        this.reversible = reversible;
        this.agilityLevel = agilityLevel;
    }

    public void setBounds(int[] bounds, int type){
        if(type == -1){
            this.entranceBounds = bounds;
        }else if(type == 1){
            this.exitBounds = bounds;
        }else{
            this.entranceBounds = bounds;
            this.exitBounds = bounds;
        }
    }

    public int[] getBounds(int type){
        if(type == -1){
            return entranceBounds;
        }else if(type == 1){
            return exitBounds;
        }else{
            return entranceBounds;
        }
    }

    public Tile getStartTile() {
        return startTile;
    }

    public Tile getEndTile() {
        return endTile;
    }

    public String getInteraction() {
        return interaction;
    }

    public int getID() {
        return ID;
    }

    public boolean isReversible() {
        return reversible;
    }

    public int getAgilityLevel() {
        return agilityLevel;
    }
}
