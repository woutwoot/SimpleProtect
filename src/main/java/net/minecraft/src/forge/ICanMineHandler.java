package net.minecraft.src.forge;

import net.minecraft.src.EntityPlayer;

public interface ICanMineHandler 
{
    /**
     * Called when a player sends a normal chat message to the server
     * that starts with a '/'.
     * 
     * This is only called on the server side.
     * 
     * Return true from this function to indicate that you have 
     * handled the command and no further processing is necessary.
     * 
     * @param player The player trying to issue the command
     * @param isOp True if the player is on the Op list 
     * @param command The command trying to be issued
     * @return True if no further processing is necessary, false to continue processing.
     */
    public boolean canMine(EntityPlayer player, int X, int Y, int Z);
}
