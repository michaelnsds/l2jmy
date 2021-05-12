package l2f.gameserver.utils;

import l2f.gameserver.data.xml.holder.ResidenceHolder;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.entity.residence.Residence;
import l2f.gameserver.tables.SkillTable;

public class SiegeUtils
{
	public static void addSiegeSkills(Player character)
	{
		character.addSkill(SkillTable.getInstance().getInfo(246, 1), false);
		character.addSkill(SkillTable.getInstance().getInfo(247, 1), false);
		if (character.isNoble())
			character.addSkill(SkillTable.getInstance().getInfo(326, 1), false);

		if (character.getClan() != null && character.getClan().getCastle() > 0)
		{
			character.addSkill(SkillTable.getInstance().getInfo(844, 1), false);
			character.addSkill(SkillTable.getInstance().getInfo(845, 1), false);
		}
	}

	public static void removeSiegeSkills(Player character)
	{
		character.removeSkill(SkillTable.getInstance().getInfo(246, 1), false);
		character.removeSkill(SkillTable.getInstance().getInfo(247, 1), false);
		character.removeSkill(SkillTable.getInstance().getInfo(326, 1), false);

		if (character.getClan() != null && character.getClan().getCastle() > 0)
		{
			character.removeSkill(SkillTable.getInstance().getInfo(844, 1), false);
			character.removeSkill(SkillTable.getInstance().getInfo(845, 1), false);
		}
	}

	public static boolean getCanRide()
	{
		for (Residence residence : ResidenceHolder.getInstance().getResidences())
			if (residence != null && residence.getSiegeEvent().isInProgress())
				return false;
		return true;
	}
}
