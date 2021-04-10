package transfarmer.game.classes;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class World {
    public static class entities {
        public static void add(Entity entity) {
            list.add(entity);
            pos.add(entity.getPos());
        }

        public static Entity get(String name) {
            for (Entity entity : list) {
                if (entity.getName().equals(name)) {
                    return entity;
                }
            }

            return null;
        }

        public static void remove(Entity target) {
            list.remove(target);
        }

        public static List<Entity> list = new ArrayList<>();
    }

    public static class users {
        public static void add(User user) {
            list.add(user);
            entities.add(user);
        }


        public static void remove(User user) {
            list.remove(user);
            entities.remove(user);
        }

        public static User current;
        public static List<User> list = new ArrayList<>();
    }

    public static class pos {
        public static void add(float[] pos) {
            list.add(pos);
        }

        public static boolean occupied(float[] target) {
            for (float[] occupied : list) {
                if (Math.hypot(target[0] - occupied[0], target[1] - occupied[1]) <= 0.1) {
                    return true;
                }
            }

            return false;
        }

        public static void remove(float[] pos) {
            list.remove(pos);
        }

        public static List<float[]> list = new ArrayList<>();
    }

    public enum attributes {
        HEALTH("health"),
        DAMAGE("damage"),
        ATTACKSPEED("attack speed"),
        PRECISION("precision");

        private String name;

        attributes(String name) {
            this.name = name;
        }

        public static attributes get(String value) {
            for (attributes attribute : attributes.values()) {
                if (attribute.name.equals(value)) {
                    return attribute;
                }
            }

            return null;
        }
    }

    public static class Save implements Serializable {
        private static final long serialVersionUID = 2L;

        public Save() {
            if (!file.exists()) {
                try {
                    file.getParentFile().mkdir();
                    file.createNewFile();
                } catch (IOException ignored) {
                    System.out.println("A save file cannot be made.");
                }
            }

        }

        public List<Entity> entities = World.entities.list;
        public List<User> users = World.users.list;
        public List<float[]> pos = World.pos.list;
        public User user = World.users.current;
        public transient File file = new File(String.format("saves/%s.save", World.users.current.name));
    }

    public static void spawnGoblin(@NotNull User user) {
            entities.add(new Goblin(user.level, user));
    }

    public static Scanner scanner = new Scanner(System.in);
    protected static final String[] advs = {"frustratingly", "shockingly", "annoyingly", "nerve-wrackingly"};
    protected static final String[] adjs = {"useless", "overwhelming", "underwhelming",
                                          "bad", "egregious", "indifferent", "mad"};
}
