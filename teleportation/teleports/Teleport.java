package STWalker.teleportation.teleports;

import STWalker.teleportation.TierFour;
import org.powerbot.script.Tile;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.Magic;

public class Teleport extends TierFour{

    int teletabID = -1;
    Magic.Spell spell;
    int[] runes;
    int[] runeQuantities;

    /**
     * For teleports of the spell variety (spellbook)
     *
     * @param endTile The tile in the middle of the area that you teleport to
     * @param interaction Not needed, leave blank
     * @param ctx ctx
     * @param spell The spell enum, for example Magic.Spell.LUMBRIDGE_TELEPORT
     * @param runes An array of the rune IDs
     * @param runeQuantities An array of the rune quantities, MUST MATCH UP WITH RUNES ARRAY
     */
    public Teleport(Tile endTile, String interaction, ClientContext ctx, Magic.Spell spell, int[] runes, int[] runeQuantities) {
        super(endTile, interaction, ctx);
        this.spell = spell;
        this.runes = runes;
        this.runeQuantities = runeQuantities;
    }

    /**
     * Teleporting with teletabs
     *
     * @param endTile Tile in the middle of your destination
     * @param interaction Leave blank or put in "Break"
     * @param ctx ctx
     * @param teletabID ID of the tablet
     */
    public Teleport(Tile endTile, String interaction, ClientContext ctx, int teletabID){
        super(endTile, interaction, ctx);
        this.teletabID = teletabID;
    }

    public boolean hasRunes(){
        for(int i = 0; i < runes.length; i++){
            if(ctx.inventory.select().id(runes[i]).count() < runeQuantities[i]) return false;
        }
        return true;
    }

    public int getTeletabID() {
        return teletabID;
    }

    public Magic.Spell getSpell() {
        return spell;
    }

    public int[] getRunes() {
        return runes;
    }

    public int[] getRuneQuantities() {
        return runeQuantities;
    }
}
