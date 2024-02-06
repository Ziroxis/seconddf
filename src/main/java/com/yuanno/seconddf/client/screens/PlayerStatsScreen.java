package com.yuanno.seconddf.client.screens;

import com.google.common.base.Strings;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import xyz.pixelatedw.mineminenomi.api.helpers.DevilFruitHelper;
import xyz.pixelatedw.mineminenomi.api.helpers.FactionHelper.MarineRank;
import xyz.pixelatedw.mineminenomi.api.helpers.FactionHelper.RevolutionaryRank;
import xyz.pixelatedw.mineminenomi.data.entity.ability.AbilityDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.IAbilityData;
import xyz.pixelatedw.mineminenomi.data.entity.challenges.ChallengesDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.challenges.IChallengesData;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.data.entity.entitystats.EntityStatsCapability;
import xyz.pixelatedw.mineminenomi.data.entity.entitystats.IEntityStats;
import xyz.pixelatedw.mineminenomi.data.entity.quests.IQuestData;
import xyz.pixelatedw.mineminenomi.data.entity.quests.QuestDataCapability;
import xyz.pixelatedw.mineminenomi.data.world.ExtendedWorldData;
import xyz.pixelatedw.mineminenomi.events.devilfruits.RandomFruitEvents;
import xyz.pixelatedw.mineminenomi.init.ModAbilities;
import xyz.pixelatedw.mineminenomi.init.ModDimensions;
import xyz.pixelatedw.mineminenomi.init.ModI18n;
import xyz.pixelatedw.mineminenomi.init.ModResources;
import xyz.pixelatedw.mineminenomi.items.AkumaNoMiItem;
import xyz.pixelatedw.mineminenomi.packets.client.CRequestSyncAbilityDataPacket;
import xyz.pixelatedw.mineminenomi.packets.client.CRequestSyncQuestDataPacket;
import xyz.pixelatedw.mineminenomi.packets.client.CRequestSyncWorldDataPacket;
import xyz.pixelatedw.mineminenomi.packets.client.ui.COpenChallengesScreenPacket;
import xyz.pixelatedw.mineminenomi.screens.CrewDetailsScreen;
import xyz.pixelatedw.mineminenomi.wypi.WyHelper;
import xyz.pixelatedw.mineminenomi.wypi.WyNetwork;

@OnlyIn(Dist.CLIENT)
public class PlayerStatsScreen extends Screen
{
	private final PlayerEntity player;
	private ExtendedWorldData worldProps;
	private IEntityStats entityStatsProps;
	private IDevilFruit devilFruitProps;
	private IChallengesData challengesProps;
	private IAllow allow;
	private final boolean hasQuests;
	private final boolean hasChallenges;
	
	public PlayerStatsScreen(boolean hasQuests, boolean hasChallenges)
	{
		super(new StringTextComponent(""));
		this.player = Minecraft.getInstance().player;
		this.hasQuests = hasQuests;
		this.hasChallenges = hasChallenges;
		this.worldProps = ExtendedWorldData.get(this.player.level);
	}

	@Override
	public void render(MatrixStack matrixStack, int x, int y, float f)
	{
		this.renderBackground(matrixStack);
		
		int posX = (this.width - 256) / 2;
		int posY = (this.height - 256) / 2;

		String colaLabel = new TranslationTextComponent(ModI18n.GUI_COLA).getString();
		String dorikiLabel = new TranslationTextComponent(ModI18n.GUI_DORIKI).getString();
		String factionLabel = new TranslationTextComponent(ModI18n.FACTION_NAME).getString();
		String raceLabel = new TranslationTextComponent(ModI18n.RACE_NAME).getString();
		String styleLabel = new TranslationTextComponent(ModI18n.STYLE_NAME).getString();

		String faction = WyHelper.getResourceName(this.entityStatsProps.getFaction());
		if(Strings.isNullOrEmpty(faction))
			faction = "empty";
		
		String race = WyHelper.getResourceName(this.entityStatsProps.getRace().toLowerCase());
		if(Strings.isNullOrEmpty(race))
			race = "empty";
		
		String style = WyHelper.getResourceName(this.entityStatsProps.getFightingStyle().toLowerCase());
		if(Strings.isNullOrEmpty(style))
			style = "empty";
		
		String actualRank = "";
		if(this.entityStatsProps.isMarine())
		{
			MarineRank marineRank = this.entityStatsProps.getMarineRank();
			actualRank = marineRank != null ? " - " + marineRank.getLocalizedName() : "";
		}
		else if(this.entityStatsProps.isRevolutionary())
		{
			RevolutionaryRank revoRank = this.entityStatsProps.getRevolutionaryRank();
			actualRank = revoRank != null ? " - " + revoRank.getLocalizedName() : "";
		}
		
		String factionActual = new TranslationTextComponent("faction." + faction).getString() + actualRank;
		String raceActual = new TranslationTextComponent("race." + race).getString();
		String styleActual = new TranslationTextComponent("style." + style).getString();
	
		if (this.entityStatsProps.isCyborg())
			WyHelper.drawStringWithBorder(this.font, matrixStack, TextFormatting.BOLD + colaLabel + ": " + TextFormatting.RESET + this.entityStatsProps.getCola() + " / " + this.entityStatsProps.getMaxCola(), posX - 50, posY + 50, -1);
		WyHelper.drawStringWithBorder(this.font, matrixStack, TextFormatting.BOLD + dorikiLabel + ": " + TextFormatting.RESET + Math.round(this.entityStatsProps.getDoriki()), posX - 50, posY + 70, -1);
		WyHelper.drawStringWithBorder(this.font, matrixStack, TextFormatting.BOLD + factionLabel + ": " + TextFormatting.RESET + factionActual, posX - 50, posY + 90, -1);
		WyHelper.drawStringWithBorder(this.font, matrixStack, TextFormatting.BOLD + raceLabel + ": " + TextFormatting.RESET + raceActual, posX - 50, posY + 110, -1);
		WyHelper.drawStringWithBorder(this.font, matrixStack, TextFormatting.BOLD + styleLabel + ": " + TextFormatting.RESET + styleActual, posX - 50, posY + 130, -1);

		if (this.entityStatsProps.getBelly() > 0)
		{
			WyHelper.drawStringWithBorder(this.font, matrixStack, "" + this.entityStatsProps.getBelly(), posX + 215, posY + 72, -1);
			this.minecraft.textureManager.bind(ModResources.CURRENCIES);
			this.blit(matrixStack, posX + 190, posY + 60, 0, 32, 32, 64);
		}

		if (this.entityStatsProps.getExtol() > 0)
		{
			WyHelper.drawStringWithBorder(this.font, matrixStack, "" + this.entityStatsProps.getExtol(), posX + 215, posY + 102, -1);
			this.minecraft.textureManager.bind(ModResources.CURRENCIES);
			this.blit(matrixStack, posX + 190, posY + 89, 34, 32, 64, 86);
		}
		
		if (!Strings.isNullOrEmpty(this.devilFruitProps.getDevilFruit()))
		{
			ItemStack yamiFruit = new ItemStack(ModAbilities.YAMI_YAMI_NO_MI);
			ItemStack df;
			if (this.allow.hasTwoDevilFruits())
			{
				df = DevilFruitHelper.getDevilFruitItemStack(this.devilFruitProps.getDevilFruit());
				yamiFruit = DevilFruitHelper.getDevilFruitItemStack(this.allow.getSecondFruit());
				String dfKey = ((AkumaNoMiItem) df.getItem()).getFruitKey();
				
				boolean dual = this.allow.hasTwoDevilFruits();
				
				if(dual)
					this.minecraft.font.drawShadow(matrixStack, TextFormatting.BOLD + "" + yamiFruit.getHoverName().getString() + " + " + df.getHoverName().getString(), posX - 28, posY + 194, -1);
				else
					this.minecraft.font.drawShadow(matrixStack, TextFormatting.BOLD + "" + yamiFruit.getHoverName().getString(), posX - 28, posY + 194, -1);
				
				if (dual)
					this.drawItemStack(df, posX - 56, posY + 187, "");
				this.drawItemStack(yamiFruit, posX - 50, posY + 190, "");
			}
			else
			{
				df = DevilFruitHelper.getDevilFruitItemStack(this.devilFruitProps.getDevilFruit());
				if(df != null) {
					String fruitName = df.getHoverName().getString();
					if(RandomFruitEvents.Client.HAS_RANDOMIZED_FRUIT)
					{
						AkumaNoMiItem item = ((AkumaNoMiItem) df.getItem()).getReverseShiftedFruit(this.player.level);
						df = new ItemStack(item);
					}
					boolean doubleYamiCheck = false;
					
					String dfKey = ((AkumaNoMiItem) df.getItem()).getFruitKey();
					if(dfKey.equalsIgnoreCase("yami_yami") && this.devilFruitProps.hasYamiPower())
						doubleYamiCheck = true;
					
					if (this.devilFruitProps.hasYamiPower() && !doubleYamiCheck)
						this.minecraft.font.drawShadow(matrixStack, TextFormatting.BOLD + "" + yamiFruit.getHoverName().getString() + " + " + df.getHoverName().getString(), posX - 28, posY + 194, -1);
					else
						this.minecraft.font.drawShadow(matrixStack, TextFormatting.BOLD + "" + fruitName, posX - 28, posY + 194, -1);
					
					if (this.devilFruitProps.hasYamiPower() && !doubleYamiCheck)
						this.drawItemStack(yamiFruit, posX - 56, posY + 187, "");
					this.drawItemStack(df, posX - 50, posY + 190, "");					
				}
				else {
					this.minecraft.font.drawShadow(matrixStack, "§4§lUnknown Fruit§r", posX - 28, posY + 194, -1);
				}
			}

		}

		//WyRenderHelper.renderEntityInInventory();
		//GuiInventory.renderEntityInInventory(posX + 140, posY + 180, 68, 0, 0, this.player);

		super.render(matrixStack, x, y, f);
	}

	@Override
	public void init()
	{
		//WyNetwork.sendToServer(new CRequestSyncWorldDataPacket());
		//WyNetwork.sendToServer(new CRequestSyncPirateCrewsPacket());
		WyNetwork.sendToServer(new CRequestSyncWorldDataPacket());
		this.entityStatsProps = EntityStatsCapability.get(this.player);
		this.devilFruitProps = DevilFruitCapability.get(this.player);
		this.challengesProps = ChallengesDataCapability.get(this.player);
		this.allow = AllowCapability.get(this.player);
		IQuestData questProps = QuestDataCapability.get(this.player);
		IAbilityData abilityProps = AbilityDataCapability.get(this.player);
	
		int posX = ((this.width - 256) / 2) - 110;
		int posY = (this.height - 256) / 2;

		boolean hasAbilities = abilityProps.countUnlockedAbilities() > 0;
		posX += 80;
		Button abilitiesButton = new Button(posX, posY + 210, 70, 20, new TranslationTextComponent(ModI18n.GUI_ABILITIES), b ->
		{
			WyNetwork.sendToServer(new CRequestSyncAbilityDataPacket(true));
		});
		if (!hasAbilities)
			abilitiesButton.active = false;
		this.addButton(abilitiesButton);

		if(this.hasQuests)
		{
			boolean hasQuests = questProps.countInProgressQuests() > 0;
			posX += 80;
			Button questsButton = new Button(posX, posY + 210, 70, 20, new TranslationTextComponent(ModI18n.GUI_QUESTS), b ->
			{
				WyNetwork.sendToServer(new CRequestSyncQuestDataPacket(true));
			});
			if (!hasQuests)
				questsButton.active = false;
			this.addButton(questsButton);
		}
	
		if(this.entityStatsProps.isPirate())
		{
			boolean hasCrew = this.worldProps.getCrewWithMember(this.player.getUUID()) != null;
			posX += 80;
			Button crewButton = new Button(posX, posY + 210, 70, 20, new TranslationTextComponent(ModI18n.GUI_CREW), b ->
			{				
				Minecraft.getInstance().setScreen(new CrewDetailsScreen());
			});
			if(!hasCrew)
				crewButton.active = false;
			this.addButton(crewButton);
		}
		
		if(this.hasChallenges)
		{
			boolean hasChallenges = this.challengesProps.countChallenges() > 0;
			posX += 80;
			Button challengesButton = new Button(posX, posY + 210, 70, 20, new TranslationTextComponent(ModI18n.GUI_CHALLENGES), b ->
			{
				WyNetwork.sendToServer(new COpenChallengesScreenPacket());
			});
			if(this.player.level.dimension() == ModDimensions.CHALLENGES)
				challengesButton.active = false;
			if(!hasChallenges)
				challengesButton.active = false;
			this.addButton(challengesButton);
		}
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}

	private void drawItemStack(ItemStack itemStack, int x, int y, String string)
	{
		GL11.glTranslatef(0.0F, 0.0F, 32.0F);
		this.itemRenderer.blitOffset = 200.0F;
		FontRenderer font = null;
		if (itemStack != null)
			font = itemStack.getItem().getFontRenderer(itemStack);
		if (font == null)
			font = this.font;
		this.itemRenderer.renderGuiItem(itemStack, x, y);
		this.itemRenderer.blitOffset = 0.0F;
	}
	
	public static void open(boolean hasQuests, boolean hasChallenges) 
	{
		Minecraft.getInstance().setScreen(new PlayerStatsScreen(hasQuests, hasChallenges));
	}
}
