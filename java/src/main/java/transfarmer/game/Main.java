package transfarmer.game;

import org.fusesource.jansi.Ansi;

import transfarmer.game.classes.*;
import transfarmer.game.classes.World.Save;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class Main {
    protected volatile static boolean run = true;

    public static void main(String[] args) {
        ("@|blue Enter your username.|@").;
        String name = World.scanner.nextLine();
        User user = load(name);

        Ansi help = ansiFormat("\n@|blue help|@: display this message.\n"
                    + "@|blue attack|@: try to attack the entity whose name follows this command.\n"
                    + "@|blue distance|@: output the distance between %s and a specified target.\n"
                    + "@|blue information|@: view information about %s or another entity.\n"
                    + "@|blue look|@: search for nearby entities.\n"
                    + "@|blue move|@: move %s.\n"
                    + "@|blue say|@: make %s say whatever succeeds this command.\n"
                    + "@|blue seek|@: move %s to a specified or the nearest entity.\n"
                    + "@|blue exit|@: exit this game.\n",
                    user.formatName(), user.formatName(), user.formatName(),
                    user.formatName(), user.formatName());

        ansiPrintln("Enter \"@|blue help|@\" for a list of commands. Append the name of a command for more details.");

        while (run) {
            List<String> words = new ArrayList<>(Arrays.asList(World.scanner.nextLine().toLowerCase().split(" ")));

            if (words.isEmpty()) {
                continue;
            }

            String verb = words.remove(0);

            switch (verb) {
                case "help":
                    if (words.isEmpty()) {
                        System.out.println(help);
                    } else {
                        String command = words.remove(0);

                        if (command.equals("move")) {
                            System.out.println("This command takes cardinal direction and (optionally) distance. "
                                             + "Below is an example.\n\tmove north 3.2");
                        }
                    }

                    break;
                case "attack":
                    Entity target;

                    if (words.isEmpty()) {
                        if (user.others().isEmpty()) {
                            System.out.printf("%s tries to attack the air.\n", name);
                            continue;
                        }

                        target = user.nearest();

                        if (user.distance(target) > 0.5) {
                            System.out.printf("%s is too far from the nearest other entity.", user.formatName());
                            continue;
                        }
                    } else {
                        String targetName = String.join(" ", words);

                        if (targetName.equals(user.getName())) {
                            user.hit();
                            continue;
                        }

                        target = World.entities.get(targetName);

                        if (target == null) {
                            ansiPrintf("\"@|yellow %s|@\" does not exist.\n", targetName);
                            continue;
                        }
                    }

                    Thread autoattack = user.autoattack(target);
                    autoattack.start();

                    for (Entity entity : user.nearby(0.5f)) {
                        if (entity instanceof Fighter) {
                            ((Fighter) entity).autoattack(user).start();
                        }
                    }

                    try {
                        autoattack.join();
                    } catch (InterruptedException ignored) {}

                    break;
                case "distance":
                    if (!words.isEmpty()) {
                        target = World.entities.get(String.join(" ", words));
                    } else if (!user.others().isEmpty()) {
                        target = user.nearest();
                    } else {
                        System.out.printf("%s does not see anybody near themselves.\n", user.formatName());
                        continue;
                    }

                    float dist = user.distance(target);
                    System.out.printf("distance to %s: %.2f m\n", target.formatName(), dist);
                    break;
                case "information":
                    if (words.isEmpty()) {
                        target = user;
                    } else {
                        String targetName = String.join(" ", words);
                        target = World.entities.get(targetName);

                        if (target == null) {
                            ansiPrintf("\"@|yellow %s|@\" does not exist.", targetName);
                            continue;
                        }
                    }

                    target.showInfo();
                    break;
                case "look":
                    user.look();
                    break;
                case "say":
                    user.say(String.join(" ", words));
                    break;
                case "seek":
                    if (words.isEmpty()) {
                        if (!user.others().isEmpty()) {
                            target = user.nearest();
                        } else {
                            System.out.printf("%s does not see anybody near them.\n", user.formatName());
                            continue;
                        }
                    } else {
                        String targetName = String.join(" ", words);

                        if (targetName.equals(user.getName())) {
                            System.out.printf("%s is already at their location.", user.formatName());
                            continue;
                        }

                        target = World.entities.get(targetName);

                        if (target == null) {
                            ansiPrintf("\"@|yellow %s|@\" does not exist.\n", targetName);
                            continue;
                        }
                    }

                    user.seek(target);
                    break;
                case "move":
                    if (words.isEmpty()) {
                        ansiPrintln("\"@|blue move|@\" usage: move DIRECTION [DISTANCE]");
                        continue;
                    }

                    List<String> directions = new ArrayList<String>() {{
                        add("north");
                        add("east");
                        add("south");
                        add("west");
                    }};

                    String direction = words.remove(0);

                    if (!directions.contains(direction)) {
                        ansiPrintf("\"@|yellow %s|@\" is not a cardinal direction.", direction);
                        continue;
                    }

                    float distance;

                    if (words.isEmpty()) {
                        distance = 1;
                    } else {
                        try {
                            distance = Float.parseFloat(words.get(0));
                        } catch (NullPointerException ignored) {
                            System.out.println("Distance must be a numerical value.");
                            continue;
                        }
                    }

                    float[] pos = user.getPos();

                    switch (direction) {
                        case "north":
                            user.move(new float[]{pos[0], pos[1] + distance});
                            break;
                        case "east":
                            user.move(new float[]{pos[0] + distance, pos[1]});
                            break;
                        case "south":
                            user.move(new float[]{pos[0], pos[1] - distance});
                            break;
                        case "west":
                            user.move(new float[]{pos[0] - distance, pos[1]});
                    }

                    break;
                case "exit":
                    run = false;
                    return;
                case "":
                    continue;
                default:
                    System.out.printf("\"@|yellow %s|@\" is not a valid command.", verb);
            }
        }
    }

    public static User load(String name) {
        class Autosave implements Runnable {
            public void run() {
                while (run) {
                    save(new Save());

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }

        File saveFile = new File(String.format("saves/%s.save", name));

        try {
            FileInputStream fileIn = new FileInputStream(saveFile);
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            Save save = (Save) objIn.readObject();
            World.entities.list = save.entities;
            World.users.list = save.users;
            World.users.current = save.user;
            World.pos.list = save.pos;
        } catch (IOException | ClassNotFoundException ignored) {
            World.users.current = new User(name);
            System.out.printf("Generating new world for %s.\n", World.users.current.formatName());
            World.users.add(World.users.current);
            Random prng = new Random();

            for (int i = 0; i <= prng.nextInt(5); i++) {
                World.spawnGoblin(World.users.list.get(prng.nextInt(World.users.list.size())));
            }

            System.out.println("Done.");
        }

        Thread save = new Thread(new Autosave());
        save.start();

        return World.users.current;
    }

    public static void save(Save save) {
        try {
            FileOutputStream fileOut = new FileOutputStream(save.file);
            ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
            objOut.writeObject(save);
            objOut.close();
            fileOut.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}

