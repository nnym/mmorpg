package transfarmer.game.classes;

import org.fusesource.jansi.Ansi;
import transfarmer.game.utils.Formatter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Entity implements Serializable {
    private static final long serialVersionUID = 1L;

    public Entity(String name, int height, int gold, float health, float[] pos) {
        this.name = name;
        this.height = height;
        this.gold = gold;
        this.health = this.maxHealth = health;
        this.pos = pos;
        this.formattedName = Formatter.ansiFormat("@|green %s|@", name).toString();
    }

    public Entity(String name, int level, User user) {
        this.name = name;
        this.level = level;
        this.pos = randomizePos(user);
        randomizeAttributes();
    }

    public Entity() {}

    public float distance(Entity target) {
        float[] dist2D = distance2D(target);
        return (float) Math.hypot(dist2D[0], dist2D[1]);
    }

    public float[] distance2D(Entity target) {
        return new float[]{target.pos[0] - pos[0], target.pos[1] - pos[1]};
    }

    public void showInfo() {
        System.out.printf("name: %s\nhealth: %.2f/%.2f\nheight: %d cm\n"
                       +  "gold coins in possession: %d\ngold coins earned: %d\nposition: (%.2f, %.2f)\n",
                          name, health, maxHealth, height, gold, goldEarned, pos[0], pos[1]);
    }

    public void randomizeAttributes() {
        Random prng = new Random();

        height = 40 + prng.nextInt(81);
        gold = prng.nextInt(11 + level);
        health = maxHealth = 4 +  prng.nextFloat() * level * 1.25f;
    }

    public float[] randomizePos(User user) {
        Random prng = new Random();

        float[] pos = {user.pos[0], user.pos[1]};
        float xShift = prng.nextFloat() * 10, yShift = prng.nextFloat() * 10;
        pos[0] += prng.nextBoolean() ? xShift : -xShift;
        pos[1] += prng.nextBoolean() ? yShift : -yShift;

        if (World.pos.occupied(pos)) {
            return randomizePos(user);
        }

        return pos;
    }

    public void move(float[] target) {
        if (!World.pos.occupied(target)) {
            System.out.printf("(%.2f, %.2f) => (%.2f, %.2f)\n", pos[0], pos[1], target[0], target[1]);
            World.pos.remove(pos);
            World.pos.add(target);
            pos = target;
        } else {
            System.out.println("You cannot go there; that area is occupied.");
        }
    }

    public void seek(Entity target) {
        float[] entityPos = target.pos;
        float[] newPos = {entityPos[0] - (float) 0.1, entityPos[1]};

        while (World.pos.occupied(newPos)) {
            newPos[1] -= .01;
        }

        move(newPos);
    }

    public void say(String msg) {
        System.out.printf("%s: %s\n", name, msg);
    }

    public Entity nearest() {
        Entity nearest = null;
        float shortestDist = Float.MAX_VALUE;

        for (Entity entity : others()) {
            float currentDist = distance(entity);

            if (currentDist <= shortestDist) {
                shortestDist = currentDist;
                nearest = entity;
            }
        }

        return nearest;
    }

    public List<Entity> others() {
        List<Entity> others = new ArrayList<>();

        for (Entity entity : World.entities.list) {
            if (entity != this) {
                others.add(entity);
            }
        }

        return others;
    }

    public List<Entity> nearby(float distance) {
        List<Entity> nearby = new ArrayList<>();

        for (Entity entity : others()) {
            if (distance(entity) <= distance) {
                nearby.add(entity);
            }
        }

        return nearby;
    }

    public void addGold(int value) {
        gold += value;
        goldEarned += value;
        String coins = value == 1 ? "coin" : "coins";
        System.out.printf("%s gained %d gold %s.\n", name, value, coins);
    }

    public String getName() {
        return name;
    }

    public String formatName() {
        return formattedName;
    }

    public float[] getPos() {
        return pos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity)) return false;
        Entity entity = (Entity) o;
        return gold == entity.gold &&
                goldEarned == entity.goldEarned &&
                height == entity.height &&
                Float.compare(entity.maxHealth, maxHealth) == 0 &&
                Float.compare(entity.health, health) == 0 &&
                name.equals(entity.name) &&
                Arrays.equals(pos, entity.pos);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, gold, goldEarned, height, maxHealth, health);
        result = 31 * result + Arrays.hashCode(pos);
        return result;
    }

    protected String name, formattedName;
    protected int gold, goldEarned, height, level;
    protected float maxHealth, health;
    protected float[] pos;
}
