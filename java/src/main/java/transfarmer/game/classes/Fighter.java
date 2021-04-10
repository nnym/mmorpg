package transfarmer.game.classes;

import static org.fusesource.jansi.Ansi.ansi;
import static transfarmer.game.utils.IO.ansiPrintf;

import java.util.Random;

public class Fighter extends Entity {
    private static final long serialVersionUID = 1L;

    public Fighter(String name, int height, int gold, float health, float damage,
                   float precision, float speed, float[] pos) {
        super(name, height, gold, health, pos);

        this.damage = damage;
        this.precision = precision;
        this.speed = speed;
    }

    public Fighter() {}

    @Override
    public void showInfo() {
        super.showInfo();

        System.out.printf("\ndamage: %.2f\nprecision: %.2f\nattack speed: %.2f attacks/second\nkills: %d\n",
                          damage, precision, speed, kills);
    }

    public void randomizeAttributes() {
        super.randomizeAttributes();
        Random prng = new Random();

        damage = 1 + prng.nextFloat() * level * 0.2f;
        precision = 0.3f + prng.nextFloat() * (0.3f + level < 24 ? level / 60f : 0.4f);
        speed = 0.3f + prng.nextFloat() * (0.3f + level / 10f);
    }

    public void hit() {
        System.out.printf("%s inflicted %.2f damage to themselves.\n", formattedName, damage);
        health -= damage;
    }

    public void hit(Entity target) {
        System.out.printf("%s inflicted %.2f damage to %s.\n", formattedName, damage, target.formattedName);
        target.health -= damage;
    }

    public boolean attack(Entity target) {
        if (distance(target) <= 0.5) {
            if (precision >= new Random().nextFloat()) {
                hit(target);

                if (target.health <= 0) {
                    kills++;
                    World.entities.remove(target);
                }
            } else {
                System.out.println(ansi().render(String.format("@|red %s|@ missed @|green %s|@.", name, target.name)));
            }

            return true;
        }

        return false;
    }

    public Thread autoattack(Entity target) {
        class Autoattack implements Runnable {
            public void run() {
                while (attack) {
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

    int kills;
    float damage, precision, speed;
}
