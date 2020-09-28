import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.Thread.setDefaultUncaughtExceptionHandler;

class Reader {
    int[] data;

    public Reader(Scanner scanner) {


        data = Arrays.stream(scanner.nextLine().split(" "))
                .mapToInt(Integer::parseInt).toArray();
        Scanner secondLine = new Scanner(scanner.nextLine());

        int X = secondLine.nextInt();
        int Y = secondLine.nextInt();
        int pacmanCount = secondLine.nextInt();
        int ghostCount = secondLine.nextInt();
        if (secondLine.hasNextLine()) {
            MainClass.log("\nGot message:" + secondLine.nextLine() + "\n");
        }

        char[][] fa = Stream.generate(() -> scanner.nextLine().substring(0, Y).toCharArray())
                .limit(X).toArray(char[][]::new);

        MainClass.fieldController = new FieldController(fa[0].length, fa.length);

        for (int i = 0; i <= 10; i++){
            System.err.println("Hello world!");
            int a = 2 + 3;
            double b
        }

        for (int i = 0; i < fa.length; i++) {
            for (int j = 0; j < fa[i].length; j++) {
                switch (fa[i][j]) {
                    case ' ':
                        MainClass.fieldController.fields[j][i] = Field.SPACE;
                        break;
                    case '+':
                        MainClass.fieldController.fields[j][i] = Field.ENERGIZER;
                        break;
                    case 'F':
                        MainClass.fieldController.fields[j][i] = Field.WALL;
                        break;
                    case '1':
                        MainClass.fieldController.fields[j][i] = Field.COIN;
                        break;
                    case 'G':
                        MainClass.fieldController.fields[j][i] = Field.GHOST_GATE;
                        break;
                    default:
                        MainClass.log("UNEXPECTED FIELD TYPE: " + fa[i][j]);
                }
            }
        }

        String[][] paa = Stream.generate(() -> scanner.nextLine().split(" "))
                .limit(pacmanCount).toArray(String[][]::new);

        MainClass.pacmanBots = new ArrayList<>();

        for (String[] aPaa : paa) {
            if (aPaa[1].equals("mlet")) {

                MainClass.pacman = new Pacman(
                        aPaa[1],
                        new Position(
                                Integer.parseInt(aPaa[3]),
                                Integer.parseInt(aPaa[2])
                        ),
                        Integer.parseInt(aPaa[4]),
                        Integer.parseInt(aPaa[5]),
                        aPaa[6],
                        false
                );

            } else {
                MainClass.pacmanBots.add(
                        new Pacman(
                                aPaa[1],
                                new Position(
                                        Integer.parseInt(aPaa[3]),
                                        Integer.parseInt(aPaa[2])
                                ),
                                Integer.parseInt(aPaa[4]),
                                Integer.parseInt(aPaa[5]),
                                aPaa[6],
                                false
                        )
                );
            }
        }

        String[][] ga = Stream.generate(() -> scanner.nextLine().split(" "))
                .limit(ghostCount).toArray(String[][]::new);
        MainClass.ghosts = new Ghost[ghostCount];

        for (int i = 0; i < ghostCount; i++) {
            MainClass.ghosts[i] = new Ghost(
                    ga[i][0].charAt(0),
                    new Position(
                            Integer.parseInt(ga[i][2]),
                            Integer.parseInt(ga[i][1])
                    ),
                    Integer.parseInt(ga[i][3]), // Eatable until
                    Integer.parseInt(ga[i][4])  // Stopped until
            );
        }
    }
}

class MainClass {

    public static Pacman pacman;
    public static ArrayList<Pacman> pacmanBots;
    public static Ghost[] ghosts;
    //public static Ghost[] previousGhosts;

    public static FieldController fieldController;
    public static int mennyiSzellemetEvettEbbenAGyorsitasban = 0;
    public static boolean[][] badpositions;
    private static long startTime;
    static int tick = 0;

    public static void main(String[] args) {
        startTime = System.nanoTime();


        setDefaultUncaughtExceptionHandler((t, e) -> log(t + " ERROR: " + Arrays.toString(e.getStackTrace())));

        MainClass.log("******************************************************************************************");
        MainClass.log("MEMORY: " + Math.round(getMemoryMB()) + "MB");
        MainClass.log("TIME: " + getRunningTimeS() + "s");

        //previousGhosts = new Ghost[0];


        for (tick = 0; true; tick++) {
            MainClass.log("TICK: " + tick);
            MainClass.log("TIME: " + getRunningTimeS() + "s");
            Reader read = new Reader(new Scanner(System.in));

            if (read.data[2] == -1) {
                break;
            }


            // GHOST-ok distance-einek kiszámítása
            for (Ghost ghost : ghosts) {
                ghost.calculateDistances();
            }

            // Konstruktorban nem lehet meghívni, mert akkor még nicsnenek ghostok
            pacman.setupEverything();

            //for (Pacman pacmanBot : pacmanBots) {
            //pacmanBot.setupEverything();
            //}

            char dir = Directions.getBestDirection(Directions.getDirectionScores(tick, pacmanBots.toArray(new Pacman[0]), pacman, fieldController, ghosts));

            /*previousGhosts = new Ghost[ghosts.length];
            for (int i = 0; i < previousGhosts.length; i++) {
                previousGhosts[i] = ghosts[i].copy();
            }*/

            System.err.print(dir);
            if (fieldController.isThereEatableGhost(pacman, Directions.getPositionByDirection(pacman.position, dir))) {
                mennyiSzellemetEvettEbbenAGyorsitasban++;
            }

            char dir2 = ' ';

            if (pacman.isFast()) {

                // INFÓK FRISSÍTÉSE ************************************************************************************

                // Amin állt, ott már nincs coin/+
                fieldController.setFieldAt(pacman.position, Field.SPACE);

                /*
                Ghost[] newGhosts = new Ghost[ghosts.length];
                for (int i = 0; i < ghosts.length; i++) {
                    newGhosts[i] = ghosts[i].copy();
                }*/

                for (Ghost ghost : ghosts) {
                    if (ghost.isEatable()) {
                        ghost.position = Directions.getPositionByDirection(ghost.position, Directions.getOppositeDirection(fieldController.getDirectionByPositions(ghost.position, pacman.position)));
                    } else {
                        ghost.position = Directions.getPositionByDirection(ghost.position, fieldController.getDirectionByPositions(ghost.position, pacman.position));
                    }
                    ghost.calculateDistances();
                }

                // Pacman firssítése
                pacman.position = Directions.getPositionByDirection(pacman.position, dir);
                //MainClass.log("ppdg");
                fieldController.setFieldAt(pacman.position, Field.SPACE);

                pacman.fastUntil--;
                /*if (previousGhosts != null) {
                    for (int i = 0; i < ghosts.length; i++) {

                        Ghost prevghost = MyMath.getGhostById(previousGhosts, ghosts[i].ghostId);
                        if (prevghost != null) {
                            Position prevPos = prevghost.position;
                            Position position = ghosts[i].position;

                            Position newPos = new Position(position.x + (position.x - prevPos.x), position.y + (position.y - prevPos.y));
                            if (fieldController.isThereWall(newPos)) {
                                newPos = position;
                            }
                            newGhosts[i].position = newPos;
                            newGhosts[i].calculateDistances();
                        }
                    }
                }*/
                pacman.setupEverything();


                dir2 = Directions.getBestDirection(Directions.getDirectionScores(tick, pacmanBots.toArray(new Pacman[0]), pacman, fieldController, ghosts));

                /*previousGhosts = new Ghost[ghosts.length];
                for (int i = 0; i < previousGhosts.length; i++) {
                    previousGhosts[i] = ghosts[i].copy();
                }*/

            } else {
                mennyiSzellemetEvettEbbenAGyorsitasban = 0;
            }


            // Megoldás kiírása:
            System.out.println(String.format("%d %d %d %c %c", read.data[0], read.data[1], read.data[2], dir, dir2));

            MainClass.log("");
        }
    }

    public static void log(String text) {
        /*try {
            URL url = new URL("https://ambrusweb11.hu/pacman/log.php");
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);

            Map<String, String> arguments = new HashMap<>();
            arguments.put("message", text);
            StringJoiner sj = new StringJoiner("&");
            for (Map.Entry<String, String> entry : arguments.entrySet())
                sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                        + URLEncoder.encode(entry.getValue(), "UTF-8"));
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(out);
            }
        } catch (Exception ignored) {

        }*/

    }

    public static double getRunningTimeS() {
        return (System.nanoTime() - startTime) / 1000000000.0;
    }

    public static double getMemoryMB() {
        return ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000.0);
    }
}

class Directions {
    public static final char RIGHT = '>';
    public static final char UP = '^';
    public static final char LEFT = '<';
    public static final char DOWN = 'v';

    public static char[] directions = new char[]{'>', '^', '<', 'v'};

    public static char getRandom() {
        Random random = new Random();
        return directions[random.nextInt(4)];
    }

    public static char getOppositeDirection(char direction) {
        switch (direction) {
            case RIGHT:
                return Directions.LEFT;
            case UP:
                return Directions.DOWN;
            case LEFT:
                return Directions.RIGHT;
            case DOWN:
                return Directions.UP;
            default:
                MainClass.log("DEFAULT CASE! DIR: " + direction);
                return Directions.RIGHT;
        }
    }

    public static char getBestDirection(double[] directionScores) {
        for (int i = 0; i < 4; i++) {
            MainClass.log(Directions.get(i) + " -> " + directionScores[i]);
        }
        double bestScore = -10000000;
        char bestDirection = '^';
        for (int i = 0; i < 4; i++) {
            if (directionScores[i] > bestScore) {
                bestScore = directionScores[i];
                bestDirection = Directions.get(i);
            }
        }
        return bestDirection;
    }

    public static Position getDeltaPosByDirection(char direction) {
        switch (direction) {
            case RIGHT:
                return new Position(1, 0);
            case UP:
                return new Position(0, -1);
            case LEFT:
                return new Position(-1, 0);
            case DOWN:
                return new Position(0, 1);
            default:
                MainClass.log("DEFAULT CASE! DIR: " + direction);
                return new Position(0, -1);
        }
    }

    public static Position getPositionByDirection(Position position, char direction) {
        Position deltaPos = getDeltaPosByDirection(direction);
        return MainClass.fieldController.checkTeleportPosition(new Position(position.x + deltaPos.x, position.y + deltaPos.y));
    }

    public static int getIndexByDirection(char dir) {
        for (int i = 0; i < directions.length; i++) {
            if (directions[i] == dir) return i;
        }
        return 0;
    }

    public static double[] getDirectionScores(int tick, Pacman[] pacmanBots, Pacman pacman, FieldController fieldController, Ghost[] ghosts) {
        double[] directionScores = new double[]{0, 0, 0, 0};

        // COINOK ÉRTÉKELÉSE *******************************************************************************************

        for (int i = 0; i < pacman.coins.length; i++) {
            int targetDir = getIndexByDirection(fieldController.getDirectionByPositions(pacman.position, pacman.coins[i]));
            directionScores[targetDir] += 20.0 / pacman.getDistance(pacman.coins[i]);
        }


        // ENERGIZER ***************************************************************************************************
        boolean isRemainingEnergizer = false;

        ArrayList<Position> energizers = new ArrayList<>();

        for (int j = 0; j < fieldController.getWidth(); j++) {
            for (int k = 0; k < fieldController.getHeight(); k++) {
                Position energizerPosition = new Position(j, k);
                if (fieldController.isThereEnergizer(energizerPosition)) {
                    isRemainingEnergizer = true;
                    energizers.add(energizerPosition);
                }
            }
        }
        if (isRemainingEnergizer) {
            int bestDirection = 0;
            int bestDistance = 10000;

            for (int i = 0; i < 4; i++) {
                for (Position energizer : energizers) {
                    if (!fieldController.isThereWall(getPositionByDirection(pacman.position, Directions.directions[i])) && pacman.getDistance(energizer) < bestDistance) {
                        bestDistance = pacman.getDistance(energizer);
                        bestDirection = fieldController.getDirectionIndexByPositions(pacman.position, energizer);
                    }
                }
            }
            directionScores[bestDirection] += 100;
        }
        // JUTALMAZÁS *********************************************************************************************
        for (int i = 0; i < 4; i++) {
            MainClass.log("irany " + Directions.get(i));

            Position newPos;
            newPos = Directions.getPositionByDirection(pacman.position, Directions.get(i));
            Pacman newPacman = new Pacman(newPos, pacman.fastUntil <= 0 ? 0 : pacman.fastUntil - 1, pacman.currentScore, pacman.plus, false);
            newPacman.calculateDistances();

            // COINOK ÉRTÉKELÉSE


            // Ha WALL-ra lépne ****************************************************************************************
            if (fieldController.isThereWall(newPos) || fieldController.isThereGhostGate(newPos)) {
                directionScores[i] -= 100000; // Nem jó
            }


            // FÉLÉS SZELLEMEKTŐL || SZELLEMEVÉS ***********************************************************************

            if (pacman.isFast()) {
                //MainClass.log("FAST");
                for (Ghost ghost : ghosts) {

                    ghost.calculateDistances();
                    // Ha ehető a szellem:
                    if (ghost.eatableUntil >= ghost.getDistance(pacman.position)) {
                        if (pacman.getDistance(ghost.position) < 12) {
                            if (!fieldController.isThereWall(newPos) && !fieldController.isThereGhostGate(newPos)) {
                                if (newPacman.getDistance(ghost.position) < pacman.getDistance(ghost.position)) {
                                    directionScores[i] += (1000 * Math.pow(2, MainClass.mennyiSzellemetEvettEbbenAGyorsitasban)) / newPacman.getDistance(ghost.position);
                                }
                            }
                        }

                    } else {  // Ha nem ehető a szellem: (félünk!!)
                        if (ghost.getDistance(pacman.position) < 6) {
                            if (!fieldController.isThereWall(newPos) && !fieldController.isThereGhostGate(newPos)) {
                                if (ghost.getDistance(newPacman.position) < 2) {
                                    // Ha életveszélybe kerülnénk:
                                    directionScores[i] -= 1000000;
                                }
                            }
                        }
                    }

                }
            } else { // Ha lassúk vagyunk
                for (Ghost ghost : ghosts) {

                    ghost.calculateDistances();

                    //MainClass.log(ghost.ghostId + " szellem tavolsaga: " + ghost.getDistance(pacman.position));
                    if (ghost.getDistance(pacman.position) < 6) {
                        if (!fieldController.isThereWall(newPos) && !fieldController.isThereGhostGate(newPos)) {
                            // Ha életveszélybe kerülnénk:
                            if (ghost.getDistance(newPacman.position) < 2) {
                                directionScores[i] -= 1000000;
                                //MainClass.log("eletveszely " + ghost.getDistance(newPacman.position));
                            }
                        }
                    }
                }
            }

            for (Pacman pacmanBot : pacmanBots) {
                if (pacmanBot.currentScore < pacman.currentScore) {
                    if (pacmanBot.isFast() && !pacman.isFast()) {
                        if (pacman.getDistance(pacmanBot.position) < 5) {
                            pacmanBot.calculateDistances();
                            if (pacmanBot.getDistance(newPos) < 4) {
                                directionScores[i] -= ((double) pacman.currentScore - (double) pacmanBot.currentScore) / 2.0;
                            }
                        }
                    } else {
                        if (pacman.getDistance(pacmanBot.position) < 3) {
                            pacmanBot.calculateDistances();
                            if (pacmanBot.getDistance(newPos) < 2) {
                                directionScores[i] -= ((double) pacman.currentScore - (double) pacmanBot.currentScore) / 2.0;
                            }
                        }
                    }

                }
            }
            // TODO Nekünk kedves pacmanekhez menés


            // ZSÁKUTCÁK ***********************************************************************************************


            /*if (pacman.relativeDeadEnds[newPos.x][newPos.y]) {
                directionScores[i] -= 1000;
            }*/
            if (pacman.obstacles[newPos.x][newPos.y]) {
                directionScores[i] -= 10000;
            }
            if (pacman.dead_end_road[newPos.x][newPos.y]) {
                directionScores[i] -= 10000;
            }

            /*int lolTick = 178;
            if (tick == lolTick) {
                MainClass.log("NEWPPACMAN---: " + newPacman.position);

                MainClass.log("crosses: " + Arrays.toString(newPacman.crosses));
                MainClass.log("---");
                MainClass.log("G" + ghosts[0].ghostId + " distances: " + MyMath.matrixOut(ghosts[0].distances));

                MainClass.log("G" + ghosts[1].ghostId + " distances: " + MyMath.matrixOut(ghosts[1].distances));

                MainClass.log("WC");
                for (int x = 0; x < fieldController.getWidth(); x++) {
                    for (int y = 0; y < fieldController.getHeight(); y++) {
                        System.err.print(Pacman.wallCount(new Position(x, y)) + " ");
                    }
                    System.err.print("\n");
                }

                MainClass.log("WCDER");
                for (int x = 0; x < fieldController.getWidth(); x++) {
                    for (int y = 0; y < fieldController.getHeight(); y++) {
                        System.err.print(Pacman.wallCountWithDER(newPacman.dead_end_road, new Position(x, y)) + " ");
                    }
                    System.err.print("\n");
                }
            }*/


            /*if (!fieldController.isThereWall(newPos)) {


                if (Pacman.wallCount(newPacman.position) == 2) {
                    //MainClass.log("CROSSES: " + Arrays.toString(newPacman.crosses));

                    boolean isChecked = false;

                    for (Ghost ghost1 : ghosts) {
                        for (Ghost ghost2 : ghosts)
                            if (!isChecked && ghost1.ghostId != ghost2.ghostId) {
                                if (newPacman.crosses[0].crossType == CrossType.DEAD_END) {
                                    if (!isChecked && newPacman.crosses[1].crossType == CrossType.DEAD_END) {
                                        directionScores[i] -= 1001;
                                        isChecked = true;
                                    } else if (ghost1.getDistance(newPacman.crosses[1].position) < 2 + newPacman.getDistance(newPacman.crosses[1].position)) {
                                        directionScores[i] -= 1002;
                                        isChecked = true;
                                        //MainClass.log("DEg");

                                    }
                                } else if (newPacman.crosses[1].crossType == CrossType.DEAD_END) {
                                    if (newPacman.crosses[0].crossType == CrossType.DEAD_END) {
                                        directionScores[i] -= 1003;
                                        isChecked = true;
                                        //MainClass.log("dDE");

                                    } else if (ghost1.getDistance(newPacman.crosses[0].position) < 2 + newPacman.getDistance(newPacman.crosses[0].position)) {
                                        directionScores[i] -= 1004;
                                        isChecked = true;
                                        //MainClass.log("gDE");
                                    }
                                } else {
                                    if (//ghost1.eatableUntil == 0 && ghost2.eatableUntil == 0 &&
                                            ghost1.getDistance(newPacman.crosses[1].position) < 2 + newPacman.getDistance(newPacman.crosses[1].position) &&
                                                    ghost2.getDistance(newPacman.crosses[0].position) < 2 + newPacman.getDistance(newPacman.crosses[0].position)
                                    ) {
                                        directionScores[i] -= 1005;
                                        isChecked = true;
                                    }
                                }
                            }
                    }

                } else if (Pacman.wallCount(newPacman.position) == 1 || Pacman.wallCount(newPacman.position) == 0) {


                    boolean[] jo_utak_e = new boolean[]{true, true, true, true};

                    for (int l = 0; l < 4; l++) {
                        Position newNewPos;
                        newNewPos = Directions.getPositionByDirection(newPacman.position, Directions.get(l));
                        Pacman newNewPacman = new Pacman(newPacman.id, newNewPos, newPacman.fastUntil, newPacman.currentScore, newPacman.plus);

                        if (!fieldController.isThereWall(newNewPos) && !fieldController.isThereGhostGate(newNewPos)) {
                            for (Ghost ghost1 : ghosts) {
                                for (Ghost ghost2 : ghosts)
                                    if (ghost1.ghostId != ghost2.ghostId) {

                                        if (newNewPacman.crosses[0].crossType == CrossType.DEAD_END) {
                                            if (newNewPacman.crosses[1].crossType == CrossType.DEAD_END) {
                                                jo_utak_e[l] = false;
                                            } else if (ghost1.eatableUntil < pacman.fastUntil && ghost1.getDistance(newNewPacman.crosses[1].position) < 4 + newNewPacman.getDistance(newNewPacman.crosses[1].position)) {
                                                jo_utak_e[l] = false;
                                            }
                                        } else if (newNewPacman.crosses[1].crossType == CrossType.DEAD_END) {
                                            if (newNewPacman.crosses[0].crossType == CrossType.DEAD_END) {
                                                jo_utak_e[l] = false;
                                            } else if (ghost1.eatableUntil < pacman.fastUntil && ghost1.getDistance(newNewPacman.crosses[0].position) < 4 + newNewPacman.getDistance(newNewPacman.crosses[0].position)) {
                                                jo_utak_e[l] = false;
                                            }
                                        } else {
                                            if (ghost1.eatableUntil < pacman.fastUntil && ghost2.eatableUntil == 0 &&
                                                    ghost1.getDistance(newNewPacman.crosses[1].position) < 4 + newNewPacman.getDistance(newNewPacman.crosses[1].position) &&
                                                    ghost2.getDistance(newNewPacman.crosses[0].position) < 4 + newNewPacman.getDistance(newNewPacman.crosses[0].position)
                                            ) {
                                                jo_utak_e[l] = false;
                                            }
                                        }
                                    }
                            }
                        } else {
                            jo_utak_e[l] = false;
                        }
                    }

                    boolean van_e_jo_ut = false;
                    for (boolean jo_ut_e : jo_utak_e) {
                        if (jo_ut_e) {
                            van_e_jo_ut = true;
                        }
                    }
                    if (!van_e_jo_ut) {
                        directionScores[i] -= 1006;
                    }

                }
                // 1 lik
                else if (Pacman.wallCount(newPacman.position) == 3) {


                    for (int l = 0; l < 4; l++) {
                        Position newNewPos;
                        newNewPos = Directions.getPositionByDirection(newPacman.position, Directions.get(l));
                        Pacman newNewPacman = new Pacman(newPacman.id, newNewPos, newPacman.fastUntil, newPacman.currentScore, newPacman.plus);

                        if (!fieldController.isThereWall(newNewPos) && !fieldController.isThereGhostGate(newNewPos)) {
                            for (Ghost ghost : ghosts) {

                                ghost.calculateDistances();

                                if (newNewPacman.crosses[0].crossType == CrossType.NORMAL_CROSS) {
                                    if (newNewPacman.getDistance(newNewPacman.crosses[0].position) + 4 > ghost.getDistance(newNewPacman.crosses[0].position)) {
                                        directionScores[i] -= 1007;
                                    }
                                } else { // crosses 1 -> normal_cross
                                    if (newNewPacman.getDistance(newNewPacman.crosses[1].position) + 4 > ghost.getDistance(newNewPacman.crosses[1].position)) {
                                        directionScores[i] -= 1008;
                                    }
                                }
                            }


                        }
                    }
                }
            }*/
        }

        MainClass.log("MEMORY: " + Math.round(MainClass.getMemoryMB()) + "MB");

        return directionScores;
    }

    // Gets the nth direction
    public static char get(int i) {
        return directions[i];
    }
}

class Pacman {
    public String id;
    public int fastUntil, currentScore;
    public String plus;
    public Position position;
    public boolean[][] obstacles;
    public boolean[][] dead_end_road;
    public boolean[][] relativeDeadEnds;

    int[][] distances;
    Position[] coins;

    Cross[] crosses;

    Pacman(Position position, int fastUntil, Integer currentScore, String plus) {
        this.position = position;
        this.fastUntil = fastUntil;
        this.currentScore = currentScore;
        this.plus = plus;

        calculateDistances();
        calculateDER();
        setCrosses();
        setObstacles();
        calculateRDEs();
    }

    Pacman(Position position, int fastUntil, Integer currentScore, String plus, boolean calculateThings) {
        this.position = position;
        this.fastUntil = fastUntil;
        this.currentScore = currentScore;
        this.plus = plus;

        if (calculateThings) {
            calculateDistances();
            calculateDER();
            setCrosses();
            setObstacles();
            calculateRDEs();
        }
    }

    Pacman(String id, Position position, int fastUntil, int currentScore, String plus) {
        this.id = id;
        this.position = position;
        this.fastUntil = fastUntil;
        this.currentScore = currentScore;
        this.plus = plus;

        calculateDistances();
        calculateDER();
        setCrosses();
        setObstacles();
        calculateRDEs();
    }

    Pacman(String id, Position position, int fastUntil, int currentScore, String plus, boolean calculateThings) {
        this.id = id;
        this.position = position;
        this.fastUntil = fastUntil;
        this.currentScore = currentScore;
        this.plus = plus;

        if (calculateThings) {
            calculateDistances();
            calculateDER();
            setCrosses();
            setObstacles();
            calculateRDEs();
        }
    }

    public void setupEverything() {
        calculateDistances();
        calculateDER();
        setCrosses();
        setObstacles();
        calculateRDEs();
    }

    public void setCrosses() {

        crosses = new Cross[2];


        for (int crossCount = 0; crossCount < crosses.length; crossCount++) {
            Position currPos = this.position;
            Position nextPos = Directions.getPositionByDirection(this.position, nthDirection(this.position, crossCount));

            // Addig megyünk, amíg ki nem érünk a folyosóból egy kereszteződésbe
            while ((/*dead_end_road[nextPos.x][nextPos.y] && */
                    wallCountWithDER(dead_end_road, nextPos) == 2 && !MainClass.fieldController.isThereEnergizer(nextPos)) /*||
                    (!dead_end_road[nextPos.x][nextPos.y] && wallCountWithDER(dead_end_road, nextPos) == 2 && !MainClass.fieldController.isThereEnergizer(nextPos))*/) {

                //MainClass.log("("+currPos.x+","+currPos.y+") "+szomszedoszsakutca);
                //Ha nem currPos-ból jött
                if (Directions.getPositionByDirection(nextPos, nthDirection(nextPos, 0)).equals(currPos)) {
                    currPos = nextPos;
                    nextPos = Directions.getPositionByDirection(nextPos, nthDirection(nextPos, 1));
                }
                //Különben a másik járható irányba megy
                else {
                    currPos = nextPos;
                    nextPos = Directions.getPositionByDirection(nextPos, nthDirection(nextPos, 0));
                }

            }


            //Ha kilépett a folyósóból, ott a kereszteződés
            if (MainClass.fieldController.isThereEnergizer(currPos)) {
                crosses[crossCount] = new Cross(CrossType.NORMAL_CROSS, currPos);
            } else if (wallCountWithDER(dead_end_road, currPos) == 3) {
                //Zsákutca vége
                crosses[crossCount] = new Cross(CrossType.DEAD_END, currPos);
            } else {
                //Normál kereszteződés
                crosses[crossCount] = new Cross(CrossType.NORMAL_CROSS, currPos);
            }
        }
    }

    /**
     * N-edik járható irány (0-tól számozva)
     */
    public char nthDirection(Position position, int n) {
        int i = 0;//jelenleg vizsgált irány
        int n2 = n;//hártalévő irányok száma, amerre nincs fal

        //Ha az i-edik irányba nincs fal, arra lehet menni
        while (n2 >= 0 && i < 4) {
            Position position1 = Directions.getPositionByDirection(position, Directions.get(i));
            if (!MainClass.fieldController.isThereWall(position1) &&
                    !MainClass.fieldController.isThereGhostGate(position1) &&
                    !dead_end_road[position1.x][position1.y]) {
                n2--;
            }
            i++;
        }
        return Directions.get(i - 1);
    }

    void calculateDistances() {
        coins = new Position[4];
        int coinCount = 0;

        distances = new int[MainClass.fieldController.getWidth()][MainClass.fieldController.getHeight()];
        ArrayList<Position> knownDistancePositions = new ArrayList<>();
        for (int i = 0; i < MainClass.fieldController.getWidth(); i++) {
            for (int j = 0; j < MainClass.fieldController.getHeight(); j++) {
                distances[i][j] = -1;
            }
        }

        distances[this.position.x][this.position.y] = 0;


        knownDistancePositions.add(position);
        int d = 1;
        boolean allChanged = false;
        while (!allChanged) {
            allChanged = true;
            ArrayList<Position> newKnownDistancePositions = new ArrayList<>();
            for (Position position : knownDistancePositions) {
                for (char direction : Directions.directions) {
                    Position newPosition = Directions.getPositionByDirection(position, direction);
                    newPosition = MainClass.fieldController.checkTeleportPosition(newPosition);
                    boolean wall = MainClass.fieldController.isThereWall(newPosition) || MainClass.fieldController.isThereGhostGate(newPosition);
                    if (!wall && distances[newPosition.x][newPosition.y] == -1) {
                        distances[newPosition.x][newPosition.y] = d;
                        if (MainClass.fieldController.isThereCoin(newPosition) && coinCount != 4) {
                            coins[coinCount] = newPosition;
                            coinCount++;
                        }
                        newKnownDistancePositions.add(newPosition);
                        allChanged = false;
                    }
                }
            }
            knownDistancePositions = newKnownDistancePositions;
            d++;
        }
    }

    int getDistance(Position position) {
        return distances[position.x][position.y];
    }

    public boolean isFast() {
        return fastUntil > 0;
    }

    @Override
    public String toString() {
        return "Pacman{" +
                "  position: " + position.toString() +
                ", fastUntil=" + fastUntil +
                ", currentScore=" + currentScore +
                ", plus='" + plus + '\'' +
                '}';
    }

    static int wallCount(Position position) {
        if (MainClass.fieldController.isThereWall(position)) {
            return -1;
        }
        int wallSum = 0;
        for (int i = 0; i < 4; i++) {
            Position pos = Directions.getPositionByDirection(position, Directions.get(i));
            if (MainClass.fieldController.isThereWall(pos) ||
                    MainClass.fieldController.isThereGhostGate(pos)) {
                wallSum++;
            }
        }
        return wallSum;
    }

    static int wallCountWithDER(boolean[][] DER, Position position) {
        if (MainClass.fieldController.isThereWall(position)) {
            return -1;
        }
        int wallSum = 0;
        for (int i = 0; i < 4; i++) {
            Position pos = Directions.getPositionByDirection(position, Directions.get(i));
            if (MainClass.fieldController.isThereWall(pos) ||
                    MainClass.fieldController.isThereGhostGate(pos) ||
                    DER[pos.x][pos.y]) {
                wallSum++;
            }
        }
        return wallSum;
    }

    void setObstacles() {

        obstacles = new boolean[MainClass.fieldController.getWidth()][MainClass.fieldController.getHeight()];
        for (int i = 0; i < MainClass.fieldController.getWidth(); i++) {
            for (int j = 0; j < MainClass.fieldController.getHeight(); j++) {
                obstacles[i][j] = false;

                if (MainClass.fieldController.isThereWall(new Position(i, j))
                        || dead_end_road[i][j]
                        || MainClass.fieldController.isThereGhostGate(new Position(i, j))) {
                    obstacles[i][j] = true;
                }
                for (Ghost ghost : MainClass.ghosts) {

                    if (((ghost.getDistance(new Position(i, j)) < this.getDistance(new Position(i, j)) + 1 && ghost.getDistance(new Position(i, j)) >= 0))
                            && ghost.eatableUntil <= (ghost.getDistance(new Position(i, j)))) {
                        obstacles[i][j] = true;
                        break;
                    }
                }
            }
        }
    }

    boolean isThereObstacle(Position position) {
        if (obstacles == null) {
            setObstacles();
        }
        position = MainClass.fieldController.checkTeleportPosition(position);
        return obstacles[position.x][position.y];
    }

    void calculateDER() {
        dead_end_road = new boolean[MainClass.fieldController.getWidth()][MainClass.fieldController.getHeight()];
        for (int i = 0; i < MainClass.fieldController.getWidth(); i++) {
            for (int j = 0; j < MainClass.fieldController.getHeight(); j++) {
                dead_end_road[i][j] = false;
            }
        }

        boolean allChanged = false;
        while (!allChanged) {
            allChanged = true;
            for (int i = 0; i < MainClass.fieldController.getWidth(); i++) {
                for (int j = 0; j < MainClass.fieldController.getHeight(); j++) {
                    int wallCount;

                    Position newPos = new Position(i, j);
                    if (!MainClass.fieldController.isThereWall(newPos) && !MainClass.fieldController.isThereGhostGate(new Position(i, j)) && !MainClass.fieldController.isThereEnergizer(newPos)) {
                        wallCount = Pacman.wallCount(new Position(i, j));
                    } else {
                        wallCount = -1;
                    }

                    if (wallCount == 3 && !dead_end_road[i][j] && !MainClass.fieldController.isThereEnergizer(newPos)) {
                        dead_end_road[i][j] = true;
                        allChanged = false;
                    }
                    if (wallCount == 2 && !dead_end_road[i][j] && !MainClass.fieldController.isThereEnergizer(newPos)) {
                        for (char dir : Directions.directions) {
                            Position position1 = Directions.getPositionByDirection(newPos, dir);
                            if (dead_end_road[position1.x][position1.y]) {
                                dead_end_road[i][j] = true;
                                allChanged = false;
                                break;
                            }
                        }
                    }
                    if (wallCount == 1 && !dead_end_road[i][j] && !MainClass.fieldController.isThereEnergizer(newPos)) {
                        for (char dir1 : Directions.directions) {
                            for (char dir2 : Directions.directions) {
                                if (dir1 != dir2) {
                                    Position position1 = Directions.getPositionByDirection(newPos, dir1);
                                    Position position2 = Directions.getPositionByDirection(newPos, dir2);
                                    if (dead_end_road[position1.x][position1.y] && dead_end_road[position2.x][position2.y]) {
                                        dead_end_road[i][j] = true;
                                        allChanged = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    void calculateRDEs() {
        /*relativeDeadEnds = new boolean[MainClass.fieldController.getWidth()][MainClass.fieldController.getHeight()];
        for (int i = 0; i < MainClass.fieldController.getWidth(); i++) {
            for (int j = 0; j < MainClass.fieldController.getHeight(); j++) {
                relativeDeadEnds[i][j] = false;
            }
        }

        boolean allChanged = false;
        while (!allChanged) {
            allChanged = true;
            for (int i = 0; i < MainClass.fieldController.getWidth(); i++) {
                for (int j = 0; j < MainClass.fieldController.getHeight(); j++) {
                    int wallCount;

                    Position newPos = new Position(i, j);
                    if (!obstacles[newPos.x][newPos.y] && !MainClass.fieldController.isThereEnergizer(newPos)) {
                        wallCount = Pacman.wallCountWithDER(dead_end_road, new Position(i, j));
                    } else {
                        wallCount = -1;
                    }

                    if (wallCount == 3 && !relativeDeadEnds[i][j] && !MainClass.fieldController.isThereEnergizer(newPos)) {
                        relativeDeadEnds[i][j] = true;
                        allChanged = false;
                    }
                    if (wallCount == 2 && !relativeDeadEnds[i][j] && !MainClass.fieldController.isThereEnergizer(newPos)) {
                        for (char dir : Directions.directions) {
                            Position position1 = Directions.getPositionByDirection(newPos, dir);
                            if (relativeDeadEnds[position1.x][position1.y]) {
                                relativeDeadEnds[i][j] = true;
                                allChanged = false;
                                break;
                            }
                        }
                    }
                    if (wallCount == 1 && !relativeDeadEnds[i][j] && !MainClass.fieldController.isThereEnergizer(newPos)) {
                        for (char dir1 : Directions.directions) {
                            for (char dir2 : Directions.directions) {
                                if (dir1 != dir2) {
                                    Position position1 = Directions.getPositionByDirection(newPos, dir1);
                                    Position position2 = Directions.getPositionByDirection(newPos, dir2);
                                    if (relativeDeadEnds[position1.x][position1.y] && relativeDeadEnds[position2.x][position2.y]) {
                                        relativeDeadEnds[i][j] = true;
                                        allChanged = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }*/
    }


}

class Ghost {
    char ghostId;
    int eatableUntil, stoppedUntil;
    int[][] distances;

    Cross[] crosses;

    Position position;

    public Ghost(char ghostId,
                 Position position,
                 int eatableUntil,
                 int stoppedUntil) {
        this.ghostId = ghostId;
        this.position = position;
        this.eatableUntil = eatableUntil;
        this.stoppedUntil = stoppedUntil;
    }


    public boolean isEatable() {
        return this.eatableUntil > 0;
    }

    void calculateDistances() {
        distances = new int[MainClass.fieldController.getWidth()][MainClass.fieldController.getHeight()];
        ArrayList<Position> knownDistancePositions = new ArrayList<>();

        setCrosses();

        // Distances feltöltése -1-ekkel
        for (int i = 0; i < MainClass.fieldController.getWidth(); i++) {
            for (int j = 0; j < MainClass.fieldController.getHeight(); j++) {
                distances[i][j] = -1;
            }
        }

        distances[this.position.x][this.position.y] = 0;

        int d = 1;
        int firstDeadEndD = 0;
        boolean allChanged = true;
        for (char direction : Directions.directions) {
            Position newPosition = Directions.getPositionByDirection(position, direction);


            if (!MainClass.fieldController.isThereWall(newPosition) && distances[newPosition.x][newPosition.y] == -1) {
                distances[newPosition.x][newPosition.y] = d;
                knownDistancePositions.add(newPosition);
                allChanged = false;
            }

        }


        d++;
        ArrayList<Position> newKnownDistancePositions;
        while (!allChanged) {

            allChanged = true;
            newKnownDistancePositions = new ArrayList<>();
            for (Position knownDistancePosition : knownDistancePositions) {
                for (int dirCount = 0; dirCount < Directions.directions.length; dirCount++) {
                    Position newPosition = Directions.getPositionByDirection(knownDistancePosition, Directions.get(dirCount));

                    if (!MainClass.fieldController.isThereWall(newPosition) && distances[newPosition.x][newPosition.y] == -1) {
                        //MainClass.log(knownDistancePositions.get(i).toString() + " " + Directions.get(j));
                        distances[newPosition.x][newPosition.y] = d;
                        newKnownDistancePositions.add(newPosition);
                        allChanged = false;
                        if (wallCount(newPosition) == 3 && firstDeadEndD == 0) {
                            firstDeadEndD = d;
                        }
                    }


                }
            }
            knownDistancePositions.addAll(newKnownDistancePositions);
            d++;
        }
    }

    char nthDirection(Position position, int n) {
        int i = 0;//jelenleg vizsgált irány
        int n2 = n;//hártalévő irányok száma, amerre nincs fal

        //Ha az i-edik irányba nincs fal, arra lehet menni
        while (n2 >= 0 && i < 4) {
            if (!MainClass.fieldController.isThereWall(Directions.getPositionByDirection(position, Directions.get(i)))) {
                n2--;
            }
            i++;
        }
        return Directions.get(i - 1);
    }

    void setCrosses() {
        crosses = new Cross[2];

        for (int crossCount = 0; crossCount < crosses.length; crossCount++) {
            Position currPos = this.position;
            Position nextPos = Directions.getPositionByDirection(this.position, nthDirection(this.position, crossCount));

            // Addig megyünk, amíg ki nem érünk a folyosóból egy kereszteződésbe
            while (wallCount(currPos) == 2) {

                //Ha nem currPos-ból jött
                if (MainClass.fieldController.checkTeleportPosition(Directions.getPositionByDirection(nextPos, nthDirection(nextPos, 0))).equals(currPos)) {
                    currPos = nextPos;
                    nextPos = Directions.getPositionByDirection(nextPos, nthDirection(nextPos, 1));
                }
                //Különben a másik járható irányba megy
                else {
                    currPos = nextPos;
                    nextPos = Directions.getPositionByDirection(nextPos, nthDirection(nextPos, 0));
                }
            }
            //Ha kilépett a folyósóból, ott a kereszteződés
            if (wallCount(currPos) == 3) {
                //Zsákutca vége
                crosses[crossCount] = new Cross(CrossType.DEAD_END, currPos);
            } else {
                //Normál kereszteződés
                crosses[crossCount] = new Cross(CrossType.NORMAL_CROSS, currPos);
            }
        }
    }

    int getDistance(Position position) {
        return distances[position.x][position.y];
    }

    Ghost copy() {
        return new Ghost(ghostId, position.copy(), eatableUntil, stoppedUntil);
    }

    @Override
    public String toString() {
        return "Ghost{" +
                "ghostId=" + ghostId +
                ", eatableUntil=" + eatableUntil +
                ", stoppedUntil=" + stoppedUntil +
                ", distances=" + Arrays.toString(distances) +
                ", position=" + position +
                '}';
    }

    int wallCount(Position position) {
        int wallSum = 0;
        for (int i = 0; i < 4; i++) {
            if (MainClass.fieldController.isThereWall(Directions.getPositionByDirection(position, Directions.get(i)))) {
                wallSum++;
            }
        }
        return wallSum;
    }
}

class FieldController {

    Field[][] fields;

    FieldController(int width, int height) {
        this.fields = new Field[width][height];
    }

    @Override
    public String toString() {
        return "FieldController{" +
                "fields=" + MyMath.matrixOut(fields) +
                '}';
    }


    void setFieldAt(Position position, Field field) {
        position = checkTeleportPosition(position);
        fields[position.x][position.y] = field;
    }

    boolean isThereGhost(Position position) {
        position = checkTeleportPosition(position);
        for (Ghost ghost : MainClass.ghosts) {
            if (ghost.eatableUntil == 0 && ghost.position.equals(position)) {
                return true;
            }
        }
        return false;
    }

    boolean isThereEatableGhost(Pacman pacman, Position position) {
        position = checkTeleportPosition(position);
        for (Ghost ghost : MainClass.ghosts) {
            if (pacman.isFast() && ghost.eatableUntil >= 1 && ghost.position.equals(position)) {
                return true;
            }
        }
        return false;
    }

    boolean isThereWall(Position position) {
        position = checkTeleportPosition(position);
        return fields[position.x][position.y] == Field.WALL;
    }

    boolean isThereGhostGate(Position position) {
        position = checkTeleportPosition(position);
        return fields[position.x][position.y] == Field.GHOST_GATE;
    }

    boolean isThereCoin(Position position) {
        position = checkTeleportPosition(position);
        return fields[position.x][position.y] == Field.COIN;
    }

    boolean isThereEnergizer(Position position) {
        position = checkTeleportPosition(position);
        //if (closest) MainClass.log("mi vagyunk");
        if (fields[position.x][position.y] == Field.ENERGIZER) {
            boolean closest = true;
            for (Ghost ghost : MainClass.ghosts) {
                //Ghost prevGhost = MyMath.getGhostById(MainClass.previousGhosts, ghost.ghostId);
                if (ghost.distances == null) {
                    MainClass.log("NULL volt...");
                    ghost.calculateDistances();
                }

                if (ghost.getDistance(position) < MainClass.pacman.getDistance(position)) {
                    closest = false;
                }
            }
            for (Pacman pacmanBot : MainClass.pacmanBots) {
                if (pacmanBot.distances == null) pacmanBot.calculateDistances();
                if (pacmanBot.getDistance(position) <= MainClass.pacman.getDistance(position)) {
                    closest = false;
                }
            }
            return closest;
        }
        return false;
    }

    public int getWidth() {
        return fields.length;
    }

    public int getHeight() {
        return fields[0].length;
    }

    public Position checkTeleportPosition(Position position) {
        if (position.x < 0) {
            position.x = MainClass.fieldController.getWidth() - 1;
        } else if (position.x > MainClass.fieldController.getWidth() - 1) {
            position.x = 0;
        }
        if (position.y < 0) {
            position.y = MainClass.fieldController.getHeight() - 1;
        } else if (position.y > MainClass.fieldController.getHeight() - 1) {
            position.y = 0;
        }
        return position;
    }

    char getDirectionByPositions(Position pacPosition, Position position2) {
        pacPosition = checkTeleportPosition(pacPosition);
        position2 = checkTeleportPosition(position2);
        Pacman origo = new Pacman(
                position2,
                MainClass.pacman.fastUntil - 1,
                0,
                "XXXX",
                false
        );

        origo.calculateDistances();
        Integer bestDistance = null;
        char bestDirection = '<';
        for (int i = 0; i < 4; i++) {
            //MainClass.log(i+". dir");
            Position newPosition = Directions.getPositionByDirection(pacPosition, Directions.get(i));
            newPosition = checkTeleportPosition(newPosition);
            if (isThereWall(newPosition) || isThereGhostGate(newPosition)) {
                continue;
            }
            if (bestDistance == null || origo.getDistance(newPosition) < bestDistance) {
                bestDistance = origo.getDistance(newPosition);
                bestDirection = Directions.get(i);
                //MainClass.log("bestDistance: " + bestDistance + " (" + bestDirection + ")");

            }

        }

        return bestDirection;
    }

    int getDirectionIndexByPositions(Position pacPosition, Position position2) {
        pacPosition = checkTeleportPosition(pacPosition);
        position2 = checkTeleportPosition(position2);
        Pacman origo = new Pacman(
                position2,
                MainClass.pacman.fastUntil - 1,
                0,
                "XXXX",
                false
        );

        origo.calculateDistances();
        Integer bestDistance = null;
        int bestDirection = 100;
        for (int i = 0; i < 4; i++) {
            //MainClass.log(i+". dir");
            Position newPosition = Directions.getPositionByDirection(pacPosition, Directions.get(i));
            newPosition = checkTeleportPosition(newPosition);
            if (isThereWall(newPosition) || isThereGhostGate(newPosition)) {
                continue;
            }
            if (bestDistance == null || origo.getDistance(newPosition) < bestDistance) {
                bestDistance = origo.getDistance(newPosition);
                bestDirection = i;
                //MainClass.log("bestDistance: " + bestDistance + " (" + bestDirection + ")");

            }

        }

        return bestDirection;
    }


}

enum Field {
    WALL, SPACE, COIN, ENERGIZER, GHOST_GATE
}

class Position {
    int x;
    int y;

    Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) {
            return false;
        }
        Position obj2 = (Position) obj;

        return obj2.x == this.x && obj2.y == this.y;

    }

    Position copy() {
        return new Position(x, y);
    }
}

class MyMath {
    static int max(int[] array) {
        Integer max = null;
        for (int item : array) {
            if (max == null || item > max) {
                max = item;
            }
        }
        return max == null ? 0 : max;
    }

    static int min(int[] array) {
        Integer min = null;
        for (int item : array) {
            if (min == null || item < min) {
                min = item;
            }
        }
        return min == null ? 0 : min;
    }

    static boolean arrayContains(char[] array, char item) {
        for (char item_ : array) {
            if (item == item_) return true;
        }
        return false;
    }

    static boolean arrayContains(boolean[] array, boolean item) {
        for (boolean item_ : array) {
            if (item == item_) return true;
        }
        return false;
    }

    static boolean arrayContainsById(Ghost[] array, Ghost item) {
        for (Ghost item_ : array) {
            if (item.ghostId == item_.ghostId) return true;
        }
        return false;
    }

    static Ghost getGhostById(Ghost[] ghosts, char id) {
        for (Ghost ghost : ghosts) {
            if (ghost.ghostId == id) return ghost;
        }
        return null;
    }

    static String matrixOut(int[][] matrix) {
        return Arrays.deepToString(matrix)
                .replace('[', '{')
                .replace(']', '}');
    }

    static String matrixOut(Field[][] matrix) {
        return Arrays.deepToString(matrix)
                .replace('[', '{')
                .replace(']', '}');
    }

    static String matrixOut(boolean[][] matrix) {
        return Arrays.deepToString(matrix)
                .replace('[', '{')
                .replace(']', '}')
                .replace("true", "1")
                .replace("false", "0");
    }
}

enum CrossType {
    DEAD_END, NORMAL_CROSS
}

class Cross {
    CrossType crossType;
    Position position;

    Cross(CrossType crossType, Position position) {
        this.crossType = crossType;
        this.position = position;
    }

    @Override
    public String toString() {
        return "Cross{" +
                "crossType=" + crossType +
                ", position=" + position +
                '}';
    }
}