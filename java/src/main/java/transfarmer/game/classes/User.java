package transfarmer.game.classes;

import static transfarmer.game.classes.World.attributes.*;
import static transfarmer.game.utils.IO.ansiPrintf;

import transfarmer.game.classes.World.attributes;

import java.util.HashMap;
import java.util.Random;


public class User extends Fighter {
    private static final long serialVersionUID = 1L;

    public User(String name, int height, int gold, float health, float damage,
                float precision, float speed, float[] pos) {
        super(name, height, gold, health, damage, precision, speed, pos);

        attributePoints.put(HEALTH, 0);
        attributePoints.put(DAMAGE, 0);
        attributePoints.put(ATTACKSPEED, 0);
        attributePoints.put(PRECISION, 0);
        attributeIncrease.put(HEALTH, 2f);
        attributeIncrease.put(DAMAGE, .2f);
        attributeIncrease.put(ATTACKSPEED, .05f);
        attributeIncrease.put(PRECISION, .01f);
    }

    public User(String name) {
        this(name, 140 + new Random().nextInt(61), 0, 30, 1, 0.85f, 1, new float[]{0, 0});
    }

    @Override
    public boolean attack(Entity target) {
        if (distance(target) > 0.5) {
            System.out.printf("%s is not in range.\n", target.formattedName);
            return false;
        }

        if (precision >= new Random().nextFloat()) {
            hit(target);

            if (target.health <= 0) {
                kill(target);

                if (target.gold == 0) {
                    System.out.printf("%s did not drop any money.\n", target.formattedName);
                } else {
                    System.out.printf("%s dropped %d gold coins. Will %s take them?\n",
                            target.formattedName, target.gold, formattedName);
                    String selection = World.scanner.nextLine();

                    if (!"nope".contains(selection)) {
                        addGold(target.gold);
                    } else {
                        addXp(target.gold);
                    }
                }

                return false;
            } else {
                ansiPrintf("remaining @|red health|@: %.2f\n", target.health);
            }
        } else {
            System.out.printf("%s missed %s.\n", formattedName, target.formattedName);
        }

        System.out.println();
        return true;
    }

    @Override
    public void showInfo() {
        super.showInfo();
        System.out.printf("\nlevel: %d\nexperience points: %d/%d\n", level, xp, nextLevel);
    }

    public void kill(Entity victim) {
        World.entities.remove(victim);
        System.out.printf("%s killed %s.\n", formattedName, victim.formattedName);
        float multiplier = 1;

        if (victim instanceof Fighter) {
            multiplier = ((Fighter) victim).damage;
        }

        addXp(Math.round(victim.maxHealth * multiplier));
        kills++;
    }

    public void look() {
        if (nearby(10).isEmpty()) {
            int goblins = new Random().nextInt(5);

            for (int i = 0; i <= goblins; i++) {
                World.spawnGoblin(this);
            }
        }

        for (Entity entity : nearby(10)) {
            float[] entityPos = entity.getPos();
            System.out.printf("position of %s: (%.2f, %.2f)\n",
                               entity.formattedName, entityPos[0], entityPos[1]);
        }
    }

    public void addXp(int value) {
        xp += value;
        String points = xp == 1 ? "point" : "points";
        System.out.printf("%s gained %d experience %s.\n", formattedName, value, points);

        if (xp >= nextLevel) {
            levelUp();
        }
    }

    public void levelUp() {
        level += 1;
        xp = 0;
        nextLevel *= 1 + (float) level / 4;

        System.out.printf("%s leveled up.\ncurrent level: %d\n", formattedName, level);
        chooseAttribute();
    }

    public void chooseAttribute() {
        String input;

        System.out.printf("Choose one of the following attributes to increase.\n"
                        + "health by %.2f\n"
                        + "damage by %.2f\n"
                        + "attack speed by %.2f\n"
                        + "precision by %.2f\n",
                        attributeIncrease.get(HEALTH), attributeIncrease.get(DAMAGE),
                        attributeIncrease.get(ATTACKSPEED), attributeIncrease.get(PRECISION));
        input = World.scanner.nextLine().toLowerCase();

        if (attributes.get(input) == null) {
            System.out.printf("\"%s\" is not a valid attribute.\n", input);
            chooseAttribute();
        } else if (input.equals("precision") && precision == 1) {
            System.out.printf("%s's precision is already the maximum.", formattedName);
            chooseAttribute();
        } else {
            attributes attribute = attributes.get(input);
            float increase = attributeIncrease.get(attribute);
            increaseAttribute(attribute);
            System.out.printf("%s's %s was increased by %.2f\n", formattedName, input, increase);
        }
    }

    public void increaseAttribute(attributes attribute) {
        switch (attribute) {
            case HEALTH:
                health += attributeIncrease.get(HEALTH);
                attributePoints.replace(attribute, attributePoints.get(attribute) + 1);
                attributeIncrease.replace(attribute, attributeIncrease.get(attribute)
                                       * (1 + (float) attributePoints.get(attribute) / 10));
                break;
            case DAMAGE:
                damage += attributeIncrease.get(DAMAGE);
                attributePoints.replace(attribute, attributePoints.get(attribute) + 1);
                attributeIncrease.replace(attribute, attributeIncrease.get(attribute)
                                       * (1 + (float) attributePoints.get(attribute) / 10));
                break;
            case ATTACKSPEED:
                speed += attributeIncrease.get(ATTACKSPEED);
                attributePoints.replace(attribute, attributePoints.get(attribute) + 1);
                attributeIncrease.replace(attribute, attributeIncrease.get(attribute)
                                        + (float) attributePoints.get(attribute) / 100);
            case PRECISION:
                precision += attributeIncrease.get(PRECISION);
                attributePoints.replace(attribute, attributePoints.get(attribute) + 1);
                attributeIncrease.replace(attribute, attributeIncrease.get(attribute) + .01f);
                break;
        }

    }

    protected int xp, nextLevel = 20;
    protected HashMap<attributes, Integer> attributePoints = new HashMap<>(3, 1);
    protected HashMap<attributes, Float> attributeIncrease = new HashMap<>(3, 1);
}
