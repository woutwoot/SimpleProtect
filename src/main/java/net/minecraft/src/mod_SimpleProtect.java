package net.minecraft.src;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import net.minecraft.server.*;
import net.minecraft.src.*;
import net.minecraft.src.forge.*;

public class mod_SimpleProtect extends BaseMod implements ICanMineHandler , IChatHandler
{
		static Configuration configuration = new Configuration(new File("config/SimpleProtect.cfg"));
	    static boolean log = configurationProperties();
	    static boolean logPlayer;
		static	int ProtX;
		static	int ProtZ;
		static	int ProtR;
	    private Set regions = new HashSet();
	    static	String regionsFile;
	    static String color;
	    static String playerMessage;
	    static String logMessage;
	public void load() 
	{
		MinecraftForge.registerChatHandler(this);
		MinecraftForge.registerCanMineHandler(this);
		loadRegions();
	}
	
	public static boolean configurationProperties()
   	{
           configuration.load();
           regionsFile = configuration.getOrCreateProperty("Filename and path", Configuration.CATEGORY_GENERAL, "config/regions.txt").value;
           color = configuration.getOrCreateProperty("ChatColor", Configuration.CATEGORY_GENERAL, "\u00a7A").value;
           playerMessage = configuration.getOrCreateProperty("Message used to warn users", Configuration.CATEGORY_GENERAL, "You aren't allowed to do stuff in region").value;
           logPlayer = Boolean.parseBoolean(configuration.getOrCreateBooleanProperty("Warn user", Configuration.CATEGORY_GENERAL, true).value);
           logMessage = configuration.getOrCreateProperty("Message used when logging", Configuration.CATEGORY_GENERAL, "tried to mess up").value;
           log = Boolean.parseBoolean(configuration.getOrCreateBooleanProperty("Make log in server.log", Configuration.CATEGORY_GENERAL, true).value);
           configuration.save();
           return log;
   	}
	
	public boolean canMine(EntityPlayer player, int X, int Y, int Z)
	{
		if (ModLoader.getMinecraftServerInstance().configManager.isOp(player.getUsername())){return true;}
	    Iterator regionsI = regions.iterator();
        while (regionsI.hasNext())
        {
            String var3 = (String)regionsI.next();
            String[] var4 = var3.split(";");
            ProtX=Integer.parseInt(var4[1]);
            ProtZ=Integer.parseInt(var4[2]);
            ProtR=Integer.parseInt(var4[3]);
    		int var1 = MathHelper.abs(X - ProtX);
    		int var2 = MathHelper.abs(Z - ProtZ);
    		if (var1 > var2){var2 = var1; } 
    		if (var2 < ProtR)
    			{
    				if (logPlayer==true){player.addChatMessage(playerMessage + " " + var4[0]+" !");}
    				if (log==true){ModLoader.getMinecraftServerInstance().log(player.getUsername() + " " + logMessage + " " + var4[0]);}
    				return false;
    			}
        }
		return true;
	}

	private void loadRegions()
    {
        try
        {
            regions.clear();
            BufferedReader var1 = new BufferedReader(new FileReader(regionsFile));
            String var2 = "";
            while ((var2 = var1.readLine()) != null)
            {
                regions.add(var2.trim().toLowerCase());
            }

            var1.close();
            ModLoader.getMinecraftServerInstance().log("Loaded " + regions.size() + " regions from file!");
        }
        catch (Exception var3)
        {
        	saveRegions();
            ModLoader.getMinecraftServerInstance().logWarning("Loading of Regions file failed! "+ var3);
        }
    }
	
    private void saveRegions()
    {
        try
        {
            PrintWriter var1 = new PrintWriter(new FileWriter(regionsFile, false));
            Iterator var2 = regions.iterator();

            while (var2.hasNext())
            {
                String var3 = (String)var2.next();
                var1.println(var3);
            }

            var1.close();
        }
        catch (Exception var4)
        {
        	ModLoader.getMinecraftServerInstance().logWarning("Saving of Regions file failed! "+ var4);
        }
    }
	
	public String getVersion() {return "0.0.1 By Dries007";}

	public boolean onServerCommand(Object paramObject, String paramString1, String paramString2) 
	{
		return false;
	}
	public String onServerCommandSay(Object paramObject, String paramString1, String paramString2) 
	{
		return paramString2;
	}
	public String onClientChatRecv(String paramString) 
	{
		return paramString;
	}
	public String onServerChat(EntityPlayer paramih, String paramString) 
	{
	return paramString;
	}
	
	public boolean onChatCommand(EntityPlayer player, boolean isOp, String command) 
	{
		command=command.toLowerCase();
		if (command.startsWith("regions"))
		{
			command=command.replace("regions", "");
			handleCommand(player, isOp, command);
			return true;
		}
		if (command.startsWith("region"))
		{
			command=command.replace("region", "");
			handleCommand(player, isOp, command);
			return true;
		}
		return false;
	}
	
	public boolean handleCommand(EntityPlayer player, boolean isOp, String command)
	{
		command=command.replaceFirst(" ", "");
		/*
		 * all players
		 */
		if (command.startsWith("list"))
		{
			String msg = "";
		    Iterator regionsI = regions.iterator();
			 while (regionsI.hasNext())
		        {
		            String var3 = (String)regionsI.next();
		            String[] var4 = var3.split(";");
		            msg = var4[0] + "," + msg;
		        }
				player.addChatMessage(color + "A list of all region on the server:");
				player.addChatMessage(color + msg);
				return true;
		}
		if (command.startsWith("info"))
		{
			command = command.replaceFirst("info", "");
			if ((!command.contains(" "))||command.equals(" "))
			{
				player.addChatMessage(color + "You need to give a name: /region info [Name]");
				return true;
			}
			String name = command.replaceFirst(" ", "");
			Iterator regionsI = regions.iterator();
			while (regionsI.hasNext())
		        {
		            String var3 = (String)regionsI.next();
		            String[] var4 = var3.split(";");
		            if (var4[0].equals(name))
		            {
		            	player.addChatMessage(color + name + " Coord: " + var4[1] + "; " + var4[2] + " Radius:" + var4[3]);
		            	return true;
		            }
		            else
		            {
		            	player.addChatMessage(color + var4[0] + "=/=" + name + ".");
		            }
		        }
			player.addChatMessage(color + name + " not found!");
			return true;
		}
		/*
		 * Op only
		 */
		if (command.startsWith("load")&&isOp)
		{
			saveRegions();
			log = configurationProperties();
			player.addChatMessage(color + "Loading the regions and config files.");
			loadRegions();
			return true;
		}
		if (command.startsWith("save")&&isOp)
		{
			player.addChatMessage(color + "Saving the regions file.");
			saveRegions();
			return true;
		}
		if (command.startsWith("add")&&isOp)
		{
			command=command.replaceFirst("add", "");
			if ((!command.contains(" "))||command.equals(" "))
			{
				player.addChatMessage(color + "You need to give a name: /region add [Name]");
				player.addChatMessage(color + "Or give coordinates /region add [Name] [X] [Z] [Radius]");
				return true;
			}
			command = command.replaceFirst(" ", "");
			if (command.contains(";"))
			{
				try
				{
					String[] var4 = command.split(" ");
					String[] coord = var4[1].split(";");
					regions.add(var4[0]+";"+Integer.parseInt(coord[0])+";"+Integer.parseInt(coord[1])+";"+Integer.parseInt(var4[2]));
					player.addChatMessage(color + "Added " + var4[0].toLowerCase() + " with cood " + Integer.parseInt(coord[2]) + ";" + Integer.parseInt(coord[1]) + " and radius " + Integer.parseInt(var4[2])+ "");
					saveRegions();
				}
				catch (ArrayIndexOutOfBoundsException  var42)
				{
					player.addChatMessage(color + "You need to give a name: /region add [Name] [Radius]");
					player.addChatMessage(color + "Or give coordinates /region add [Name] [X;Z] [Radius]");
					return true;
				}
			}
			else
			{
				Double playerX = player.posX;
				Double playerZ = player.posZ;
				Integer playerXint = playerX.intValue();
				Integer playerZint = playerZ.intValue();
				try
				{
					String var4[] = command.split(" ");
					regions.add(var4[0]+";"+playerXint.toString()+";"+playerZint.toString()+";"+Integer.parseInt(var4[1]));
					player.addChatMessage(color + "Added " + var4[0].toLowerCase() + " with cood " + playerXint + ";" + playerZint + " and radius " + Integer.parseInt(var4[1])+ "");
					saveRegions();
				}
				catch (ArrayIndexOutOfBoundsException  var42)
				{
					player.addChatMessage(color + "You need to give a name: /region add [Name] [Radius]");
					player.addChatMessage(color + "Or give coordinates /region add [Name] [X;Z] [Radius]");
					return true;
				}
			}
			
			return true;
		}
		if (command.startsWith("remove")&&isOp)
		{
			String name = command.replaceFirst("remove", "");
			if ((!command.contains(" "))||command.equals(" "))
			{
				player.addChatMessage(color + "You need to give a name: /region remove [Name]");
				return true;
			}
			else
			{
				command = command.replace("remove ", "");
				Iterator regionsI = regions.iterator();
		        while (regionsI.hasNext())
		        {
		            String var3 = (String)regionsI.next();
		            String[] var4 = var3.split(";");
		            if (command.equals(var4[0]))
		            {
		            	regions.remove(var3);
		            	player.addChatMessage(color + "Removed " + var4[0]);
		            	saveRegions();
		            	return true;
		            }
		        }
	            player.addChatMessage(color + command + " not found!");
	            return true;
			}
		}
		if (isOp)
		{
			player.addChatMessage(color + "You must use one of these commands:");
			player.addChatMessage(color + "Help, save, load, list, add, remove, info");
		}
		else
		{
			player.addChatMessage(color + "You must use one of these commands:");
			player.addChatMessage(color + "Help, list, info");
		}
		return true;
	}
	
}