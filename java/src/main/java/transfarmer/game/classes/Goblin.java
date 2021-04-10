package transfarmer.game.classes;

import transfarmer.game.utils.Formatter;

import java.util.Random;

import static transfarmer.game.classes.World.adjs;
import static transfarmer.game.classes.World.advs;

public class Goblin extends Fighter {
    private static final long serialVersionUID = 1L;

    public Goblin(String name, int height, int gold, float health, float damage,
                  float precision, float speed, float[] pos) {
        super(name, height, gold, health, damage, precision, speed, pos);
    }

    public Goblin(int level, User user) {
        this.level = level;
        this.name = randomizeName();
        this.pos = randomizePos(user);
        randomizeAttributes();

        formattedName = Formatter.ansiFormat("@|red %s|@", name).toString();
    }

    public String randomizeName() {
        Random prng = new Random();

        String adv = advs[prng.nextInt(advs.length)];
        String adj = adjs[prng.nextInt(adjs.length)];
        String name = String.format("%s %s goblin", adv, adj);

        if (World.entities.get(name) != null) {
            return randomizeName();
        }

        return name;
    }
    public Thread autoattack(Entity target) {
        class Autoattack implements Runnable {
            public void run() {
                while (attack) {
                    if (!World.entities.list.contains(Goblin.this)) {
                        return;
                    }

                    try {
                        attack = attack(target);
                    } catch (NullPointerException exception) {
                        exception.printStackTrace();
                        return;
                    }

                    try {
                        Thread.sleep(Math.round(1000 / speed));
                    } catch (InterruptedException ignored) {
                    }
                }
            }

            public volatile boolean attack = true;
        }

        return new Thread(new Autoattack());
    }
}
