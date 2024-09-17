package sxy.apin.power;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import sxy.apin.character.Furina;
import sxy.apin.helper.FurinaHelper;

import java.util.ArrayList;

/**
 * 会心 每打出一张攻击牌获得1层会心，受到一次伤害消除2层会心。回合结束时对最近敌人造成会心层数的伤害。
 */
public class CriticalBoost extends AbstractPower {
    // 能力的ID
    public static final String POWER_ID = FurinaHelper.makePowerID(CriticalBoost.class.getSimpleName());
    // 能力的本地化字段
    private static final PowerStrings powerStrings = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
    // 能力的名称
    private static final String NAME = powerStrings.NAME;
    // 能力的描述
    private static final String[] DESCRIPTIONS = powerStrings.DESCRIPTIONS;

    public CriticalBoost(AbstractCreature owner, int amount) {
        this.name = NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.type = PowerType.BUFF;
        this.amount = amount;
        // 添加一大一小两张能力图
        String path128 = "sxy/apin/img/powers/Example84.png";
        String path48 = "sxy/apin/img/powers/Example32.png";
        this.region128 = new TextureAtlas.AtlasRegion(ImageMaster.loadImage(path128), 0, 0, 84, 84);
        this.region48 = new TextureAtlas.AtlasRegion(ImageMaster.loadImage(path48), 0, 0, 32, 32);
        // 首次添加能力更新描述
        this.updateDescription();
    }

    public void updateDescription() {
        this.description = DESCRIPTIONS[0];
    }

    @Override
    public void stackPower(int stackAmount) {
        if (this.amount == -1) {
            return;
        }
        this.fontScale = 8.0F;
        this.amount += stackAmount;
        if (this.amount >= 999) {
            this.amount = 999;
        }
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.type == AbstractCard.CardType.ATTACK) {
            this.amount++;
            this.flash();
        }
    }

    @Override
    public void wasHPLost(DamageInfo info, int damageAmount) {
        this.amount -= 2;
        this.flash();
        if (this.amount <= 0) {
            FurinaHelper.removePlayerPower(this.ID);
        }
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer) {
            AbstractMonster mon;
            ArrayList<AbstractMonster> monsters = FurinaHelper.getMonsters();
            if (monsters.isEmpty()) {
                return;
            } else {
                mon = monsters.get(0);
            }
            for (AbstractMonster monster : monsters) {
                if (monster.isDeadOrEscaped() || monster.currentHealth <= 0) {
                    continue;
                }
                mon = monster;
                break;
            }
            this.flash();
            // 永世领唱 每持有 10 点气氛值，会心造成的伤害提升50%。
            double fac = 1.0;
            if (FurinaHelper.getPower(PerpetualMuseOfChansonsPower.POWER_ID) != null) {
                int revelry = Furina.getRevelry();
                fac += (int) (revelry / 10) * 0.5;
            }
            FurinaHelper.damage(mon, FurinaHelper.getPlayer(), (int) (this.amount * fac), DamageInfo.DamageType.NORMAL);
        }
    }
}
