package l2f.gameserver.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import l2f.gameserver.Config;
import l2f.gameserver.instancemanager.QuestManager;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.World;
import l2f.gameserver.model.entity.CCPHelpers.CCPCWHPrivilages;
import l2f.gameserver.model.entity.CCPHelpers.CCPOffline;
import l2f.gameserver.model.entity.CCPHelpers.CCPPassword;
import l2f.gameserver.model.entity.CCPHelpers.CCPPoll;
import l2f.gameserver.model.entity.CCPHelpers.CCPRepair;
import l2f.gameserver.model.entity.CCPHelpers.CCPSecondaryPassword;
import l2f.gameserver.model.entity.CCPHelpers.CCPSmallCommands;
import l2f.gameserver.model.entity.CCPHelpers.itemLogs.CCPItemLogs;
import l2f.gameserver.model.quest.Quest;
import l2f.gameserver.model.quest.QuestState;
import l2f.gameserver.network.serverpackets.DeleteObject;
import l2f.gameserver.network.serverpackets.L2GameServerPacket;

import org.apache.commons.lang3.StringUtils;

public class CharacterControlPanel
{
	private static CharacterControlPanel _instance;

	public String useCommand(Player activeChar, String text, String bypass)
	{
		// While some1 is currently writing secondary password
		if (activeChar.isBlocked() && !text.contains("secondaryPass"))
		{
			return null;
		}

		String[] param = text.split(" ");
		if (param.length == 0)
			return "char.htm";

		// Block unwanted buffs
		else if (param[0].equalsIgnoreCase("grief"))
		{
			CCPSmallCommands.setAntiGrief(activeChar);
		}
		// Block Experience
		else if (param[0].equalsIgnoreCase("noe"))
		{
			if (activeChar.getVar("NoExp") == null)
				activeChar.setVar("NoExp", "1", -1);
			else
				activeChar.unsetVar("NoExp");
		}
		// Auto Shoulshots
		else if (param[0].equalsIgnoreCase("soulshot"))
		{
			if (activeChar.getVar("soulshot") == null)
				activeChar.setVar("soulshot", "1", -1);
			else
				activeChar.unsetVar("soulshot");
		}
		// Show Online Players
		else if (param[0].equalsIgnoreCase("online"))
		{
			activeChar.sendMessage(CCPSmallCommands.showOnlineCount());
		}
		else if (param[0].equalsIgnoreCase("changeLog"))
		{
			Quest q = QuestManager.getQuest(QuestManager.TUTORIAL_QUEST_ID);
			if (q != null)
			{
				QuestState st = activeChar.getQuestState(q.getName());
				if (st != null)
				{
					String change = ChangeLogManager.getInstance().getChangeLog(ChangeLogManager.getInstance().getLatestChangeId());
					st.showTutorialHTML(change);
				}
			}
		}
		// Item logs
		else if (param[0].equalsIgnoreCase("itemLogs"))
		{
			CCPItemLogs.showPage(activeChar);
			return null;
		}
		// Show private stores Hide private stores / Fixed
		else if (param[0].equalsIgnoreCase(Player.NO_TRADERS_VAR))
		{
			if (activeChar.getVar(Player.NO_TRADERS_VAR) == null)
			{
				ArrayList<L2GameServerPacket> pls = new ArrayList<>();
				List<Player> list = World.getAroundPlayers(activeChar);
				for (Player player : list)
				{
					if (player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
						pls.add(new DeleteObject(player));
				}

				list.clear();

				activeChar.sendPacket(pls);
				activeChar.setNotShowTraders(true);
				activeChar.setVar(Player.NO_TRADERS_VAR, "1", -1);
			}
			else
			{
				activeChar.setNotShowTraders(false);
				activeChar.unsetVar(Player.NO_TRADERS_VAR);

				List<Player> list = World.getAroundPlayers(activeChar);
				for (Player player : list)
				{
					if (player.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
						player.broadcastUserInfo(true);
				}
			}
		}

		// Show skill animations
		else if (param[0].equalsIgnoreCase(Player.NO_ANIMATION_OF_CAST_VAR))
		{
			if (activeChar.getVar(Player.NO_ANIMATION_OF_CAST_VAR) == null)
			{
				activeChar.setNotShowBuffAnim(true);
				activeChar.setVar(Player.NO_ANIMATION_OF_CAST_VAR, "true", -1);
			}
			else
			{
				activeChar.setNotShowBuffAnim(false);
				activeChar.unsetVar(Player.NO_ANIMATION_OF_CAST_VAR);
			}
		}
		// Change auto loot
		else if (param[0].equalsIgnoreCase("autoloot"))
		{
			setAutoLoot(activeChar);
		}
		else if (param[0].equalsIgnoreCase("autolootherbs"))
		{
			setAutoLootHerbs(activeChar);
		}
		else if (param[0].equalsIgnoreCase("blocktrade"))
		{
			activeChar.setTradeRefusal(!activeChar.getTradeRefusal());
		}
		else if (param[0].equalsIgnoreCase("blockpartyinvite"))
		{
			activeChar.setPartyInviteRefusal(!activeChar.getPartyInviteRefusal());
		}
		else if (param[0].equalsIgnoreCase("blockfriendinvite"))
		{
			activeChar.setFriendInviteRefusal(!activeChar.getFriendInviteRefusal());
		}
		else if (param[0].equalsIgnoreCase("repairCharacter"))
		{
			if (param.length > 1)
				CCPRepair.repairChar(activeChar, param[1]);
			else
				return null;
		}	
		else if (param[0].equalsIgnoreCase("offlineStore"))
		{
			boolean result = CCPOffline.setOfflineStore(activeChar);
			if (result)
				return null;
			else
				return "char.htm";
		}
		else if (param[0].startsWith("poll") || param[0].startsWith("Poll"))
		{
			CCPPoll.bypass(activeChar, param);
			return null;
		}
		else if (param[0].equals("combine"))
		{
			CCPSmallCommands.combineTalismans(activeChar);
			return null;
		}
		else if (param[0].equals("otoad"))
		{
			CCPSmallCommands.openToad(activeChar, -1);
			return null;
		}
		else if (param[0].equals("hwidPage"))
		{
			// if (Config.ALLOW_SMARTGUARD)
			// {
			if (activeChar.getHwidLock() != null)
				return "cfgUnlockHwid.htm";
			else
				return "cfgLockHwid.htm";
			// }
		}
		else if (param[0].equals("lockHwid"))
		{
			// if (Config.ALLOW_SMARTGUARD)
			// {
			boolean shouldLock = Boolean.parseBoolean(param[1]);
			if (shouldLock)
			{
				activeChar.setHwidLock(activeChar.getHWID());
				activeChar.sendMessage("Character is now Locked!");
			}
			else
			{
				activeChar.setHwidLock(null);
				activeChar.sendMessage("Character is now Unlocked!");
			}
			// }
		}
		else if (param[0].startsWith("secondaryPass"))
		{
			CCPSecondaryPassword.startSecondaryPasswordSetup(activeChar, text);
			return null;
		}
		else if (param[0].equalsIgnoreCase("showPassword"))
		{
			return "cfgPassword.htm";
		}
		else if (param[0].equals("changePassword"))
		{
			StringTokenizer st = new StringTokenizer(text, " | ");
			String[] passes = new String[st.countTokens() - 1];
			st.nextToken();
			for (int i = 0; i < passes.length; i++)
			{
				passes[i] = st.nextToken();
			}
			boolean newDialog = CCPPassword.setNewPassword(activeChar, passes);
			if (newDialog)
				return null;
			else
				return "cfgPassword.htm";
		}
		else if (param[0].equalsIgnoreCase("showRepair"))
		{
			return "cfgRepair.htm";
		}
		else if (param[0].equalsIgnoreCase("ping"))
		{
			CCPSmallCommands.getPing(activeChar);
			return null;
		}
		else if (param[0].equalsIgnoreCase("cwhPrivs"))
		{
			if (param.length > 1)
			{
				String args = param[1] + (param.length > 2 ? " " + param[2] : "");
				return CCPCWHPrivilages.clanMain(activeChar, args);
			}
			else
			{
				return "cfgClan.htm";
			}
		}
		else if (param[0].equals("delevel"))
		{
			if (param.length > 1 && StringUtils.isNumeric(param[1]))
			{
				boolean success = CCPSmallCommands.decreaseLevel(activeChar, Integer.parseInt(param[1]));
				if (success)
					return null;
			}

			return "cfgDelevel.htm";
		}

		return "char.htm";
	}

	public String replacePage(String currentPage, Player activeChar, String additionalText, String bypass)
	{
		currentPage = currentPage.replaceFirst("%online%", CCPSmallCommands.showOnlineCount());
		currentPage = currentPage.replaceFirst("%antigrief%", getONOFF(activeChar.getVarB("antigrief")));
		currentPage = currentPage.replaceFirst("%noe%", getONOFF(activeChar.getVarB("NoExp")));
		currentPage = currentPage.replaceFirst("%soulshot%", getONOFF(activeChar.getVarB("soulshot")));
		currentPage = currentPage.replaceFirst("%notraders%", getONOFF(activeChar.getVarB("notraders")));
		currentPage = currentPage.replaceFirst("%notShowBuffAnim%", getONOFF(activeChar.getVarB("notShowBuffAnim")));
		currentPage = currentPage.replaceFirst("%autoLoot%", getONOFF(activeChar.isAutoLootEnabled()));
		currentPage = currentPage.replaceFirst("%autoLootHerbs%", getONOFF(activeChar.isAutoLootHerbsEnabled()));
		currentPage = currentPage.replaceFirst("%blocktrade%", getONOFF(activeChar.getTradeRefusal()));
		currentPage = currentPage.replaceFirst("%blockpartyinvite%", getONOFF(activeChar.getPartyInviteRefusal()));
		currentPage = currentPage.replaceFirst("%blockfriendinvite%", getONOFF(activeChar.getFriendInviteRefusal()));
		if (currentPage.contains("%charsOnAccount%"))
			currentPage = currentPage.replaceFirst("%charsOnAccount%", CCPRepair.getCharsOnAccount(activeChar.getName(), activeChar.getAccountName()));

		return currentPage;
	}

	private String getONOFF(boolean ON)
	{
		if (ON)
			return "ON";
		else
			return "OFF";
	}

	public void setAutoLoot(Player player)
	{
		if (Config.AUTO_LOOT_INDIVIDUAL)
		{
			player.setAutoLoot(!player.isAutoLootEnabled());
		}
	}
	
	public void setAutoLootHerbs(Player player)
	{
		if (Config.AUTO_LOOT_INDIVIDUAL)
		{
			player.setAutoLootHerbs(!player.isAutoLootHerbsEnabled());
		}
	}
	
	public static CharacterControlPanel getInstance()
	{
		if (_instance == null)
			_instance = new CharacterControlPanel();
		return _instance;
	}
}
