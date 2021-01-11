package basicplayer;
import battlecode.common.*;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public strictfp class RobotPlayer {
    static RobotController rc;

    // a dictionary mapping a map location to it's passability
    static Map<MapLocation, Double> passabilities = new HashMap<>();

    // a set of MapLocations with detected but unknown bots
    static Set<MapLocation> detectedBots = new HashSet<>();

    // a set of map locations with known muckrakers
    static Set<MapLocation> enemyMuckRaker = new HashSet<>();

    // a set of map locations with known slanderers
    static Set<MapLocation> confirmedSlanderers = new HashSet<>();

    // a set of map locations of politicians or potentially slanderers
    static Set<MapLocation> enemyPoliticians = new HashSet<>();

    // a set of map locations with neutral enlightment centers
    static Set<MapLocation> neutralEnlightmentCenters = new HashSet<>();

    // a set of map locations with enemy enlightment centers
    static Set<MapLocation> enemyEnlightmentCenters = new HashSet<>();

    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static final int ENLIGHTMENT_CENTER_SENSOR_RADIUS_SQUARED = 40;
    static final int POLITICIAN_SENSOR_RADIUS_SQUARED = 40;
    static final int SLANDERER_SENSOR_RADIUS_SQUARED = 40;
    static final int MUCKRAKER_SENSOR_RADIUS_SQUARED = 40;


    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static int turnCount;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // update passabilities field with what the robot sees
//        getSensedSquares();

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        basicplayer.RobotPlayer.rc = rc;

        turnCount = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: EnlightenmentCenter.runEnlightenmentCenter(rc); break;
                    case POLITICIAN:           Politician.runPolitician(rc);          break;
                    case SLANDERER:            Slanderer.runSlanderer(rc);           break;
                    case MUCKRAKER:            Muckraker.runMuckraker(rc);           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

//    static void runEnlightenmentCenter() throws GameActionException {
//        RobotType toBuild = randomSpawnableRobotType();
//        int influence = 50;
//        for (Direction dir : directions) {
//            if (rc.canBuildRobot(toBuild, dir, influence)) {
//                rc.buildRobot(toBuild, dir, influence);
//            } else {
//                break;
//            }
//        }
//    }

//    static void runPolitician() throws GameActionException {
//        Team enemy = rc.getTeam().opponent();
//        int actionRadius = rc.getType().actionRadiusSquared;
//        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
//        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
//            System.out.println("empowering...");
//            rc.empower(actionRadius);
//            System.out.println("empowered");
//            return;
//        }
//        if (tryMove(randomDirection()))
//            System.out.println("I moved!");
//    }

//    static void runSlanderer() throws GameActionException {
//        if (tryMove(randomDirection()))
//            System.out.println("I moved!");
//    }

//    static void runMuckraker() throws GameActionException {
//        Team enemy = rc.getTeam().opponent();
//        int actionRadius = rc.getType().actionRadiusSquared;
//        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
//            if (robot.type.canBeExposed()) {
//                // It's a slanderer... go get them!
//                if (rc.canExpose(robot.location)) {
//                    System.out.println("e x p o s e d");
//                    rc.expose(robot.location);
//                    return;
//                }
//            }
//        }
//        if (tryMove(randomDirection()))
//            System.out.println("I moved!");
//    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    static Set<MapLocation> getSensedSquares() throws GameActionException {
        Set<MapLocation> newDetectedBots = new HashSet<>();
        Set<MapLocation> newNeutralEnlightmentCenters = new HashSet<>();
        Set<MapLocation> newEnemyEnlightmentCenters = new HashSet<>();
        Set<MapLocation> newEnemyMuckRaker = new HashSet<>();
        Set<MapLocation> newConfirmedSlanderers = new HashSet<>();
        Set<MapLocation> newEnemyPoliticians = new HashSet<>();

        // detect robots
        MapLocation[] curDetectedRobots = rc.detectNearbyRobots();

        for (MapLocation robotLocation: curDetectedRobots){
            newDetectedBots.add(robotLocation);
        }

        // sense robots
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot: robots){
            int id = robot.getID();
            RobotType type = robot.getType();
            Team team = robot.getTeam();
            MapLocation location = robot.getLocation();

            // NOTE: This could create an error if the location is not in detected. This should never happen because all robots that are sensed should have already been detected.
            newDetectedBots.remove(location);

            // if we saw a neutral piece -> neutral Enlightment Center
            if (team == Team.NEUTRAL){
                newNeutralEnlightmentCenters.add(location);
            }
            // if the robot is an enemy
            else if (!rc.getTeam().equals(team)){
                switch(type){
                    case ENLIGHTENMENT_CENTER: newEnemyEnlightmentCenters.add(location); break;
                    case MUCKRAKER: newEnemyMuckRaker.add(location); break;
                    case SLANDERER: newConfirmedSlanderers.add(location); break;
                    case POLITICIAN: newEnemyPoliticians.add(location); break;
                }

            }
        }
        detectedBots.addAll(newDetectedBots);
        neutralEnlightmentCenters.addAll(newNeutralEnlightmentCenters);
        enemyEnlightmentCenters.addAll(newEnemyEnlightmentCenters);
        enemyMuckRaker.addAll(newEnemyMuckRaker);
        confirmedSlanderers.addAll(newConfirmedSlanderers);
        enemyPoliticians.addAll(newEnemyPoliticians);

        // sense passabilities
        MapLocation curLocation = rc.getLocation();
        int radiusSquared = getSenseRadiusSquared();
        Set<MapLocation> sensedSquares = new HashSet<>();
        curLocation.translate(-radiusSquared, -radiusSquared);
        for (int x = 0; x <= 2*radiusSquared; x++){
            for (int y = 0; y <= 2*radiusSquared; y++){
                // Check all sets of robots to erase any information that is no longer valid
                if (detectedBots.contains(curLocation) && !newDetectedBots.contains(curLocation)) detectedBots.remove(curLocation);
                if (neutralEnlightmentCenters.contains(curLocation) && !newNeutralEnlightmentCenters.contains(curLocation)) neutralEnlightmentCenters.remove(curLocation);
                if (enemyEnlightmentCenters.contains(curLocation) && !newEnemyEnlightmentCenters.contains(curLocation)) enemyEnlightmentCenters.remove(curLocation);
                if (enemyMuckRaker.contains(curLocation) && !newEnemyMuckRaker.contains(curLocation)) enemyMuckRaker.remove(curLocation);
                if (confirmedSlanderers.contains(curLocation) && !newConfirmedSlanderers.contains(curLocation)) confirmedSlanderers.remove(curLocation);
                if (enemyPoliticians.contains(curLocation) && !newEnemyPoliticians.contains(curLocation)) enemyPoliticians.remove(curLocation);

                // if passability has not been sensed, add that value to the map
                if (rc.canSenseLocation(curLocation)){
                    if (!passabilities.containsKey(curLocation)) {
                        double passability = rc.sensePassability(curLocation);
                        passabilities.put(curLocation, passability);
                    }
                }

                // check to see if spaces we though robots were in are no longer there

                curLocation.translate(1,0);
            }
            curLocation.translate(-2*radiusSquared, 1);
        }

        return sensedSquares;
    }

    static Integer getSenseRadiusSquared(){
        switch (rc.getType()) {
            case ENLIGHTENMENT_CENTER: return ENLIGHTMENT_CENTER_SENSOR_RADIUS_SQUARED;
            case POLITICIAN:           return POLITICIAN_SENSOR_RADIUS_SQUARED;
            case SLANDERER:            return SLANDERER_SENSOR_RADIUS_SQUARED;
            case MUCKRAKER:            return MUCKRAKER_SENSOR_RADIUS_SQUARED;
        }
        return null;
    }

//    static Set<MapLocation> getLine (MapLocation currentLocation, MapLocation targetLocation) {
//        final int currentX = currentLocation.x; final int currentY = currentLocation.y;
//        final int targetX = targetLocation.x; final int targetY = targetLocation.y;
//        final HashSet<MapLocation> coordinatesOnLine = new HashSet<>();
//        // Vertical line
//        if (currentX == targetX) {
//            range = Math.abs(targetY - currentY);
//            if (currentY < targetY) { // path goes downward
//                for (int i = 0; i <= range; i++) {
//                    coordinatesOnLine.add(new MapLocation(currentX, currentY + i));
//                }
//            } else if (currentY > targetY) { // path goes upward
//                for (int i = 0; i <= range; i++) {
//                    coordinatesOnLine.add(new MapLocation(currentX, currentY - i));
//                }
//            }
//        }
//
//        // Horizontal line
//        else if (currentY == targetY) {
//            range = Math.abs(targetX - currentX);
//            if (currentX < targetX) { // path goes to the right
//                for (int i = 0; i <= range; i++) {
//                    coordinatesOnLine.add(new MapLoctation(currentX + i, currentY));
//                }
//            } else if (currentX > targetX) { // path goes to the left
//                for (int i = 0; i <= range; i++) {
//                    coordinatesOnLine.add(new MapLocation(currentX - i, currentY));
//                }
//            }
//        }
//
//        else if (currentX != targetX && currentY != targetY) {
//            final double slopeOfLine = (double) (targetY - currentY) / (targetX - currentX);
//
//
//        }
//        int slopeOfLine =
//        return coordinatesOnLine;
//    }
//
//
//    static boolean checkIntersection(HashSet<MapLocation> originalLine, HashSet<MapLocation> currentLine) {
//
//    }

    private static final double passabilityThreshold = 0.5;
    static Direction bugDirection = null;
    private static MapLocation oldPositionOnTargetLine = null;




    static void basicBugStraightLine(MapLocation targetLocation) throws GameActionException {
        MapLocation startingLocation = rc.getLocation();
        boolean tracingObstacle = false;
        while (true) {
            Direction d = rc.getLocation().directionTo(targetLocation);
            System.out.println("Direction to target" + d);
            if (oldPositionOnTargetLine == null) { // Record initial location
                oldPositionOnTargetLine = rc.getLocation();
            }
            if (rc.getLocation().equals(targetLocation)) {
                System.out.println("I have reached the target location");
                // perform some action based on the unit
            } else if (rc.isReady()) { // Moving on the line  towards targetLocation
                if (tracingObstacle) {
                    System.out.println("Now tracing obstacle");
                    for (int i = 0; i < 8; i++) {
                        if (rc.canMove(bugDirection) &&
                                rc.sensePassability(rc.getLocation().add(bugDirection)) >= passabilityThreshold &&
                                rc.sensePassability(rc.getLocation().add(bugDirection).add(bugDirection.rotateLeft().rotateLeft())) < passabilityThreshold) { // Check if there's obstacle to left while tracing
                            System.out.println("Will turn towards " + bugDirection);
                            System.out.println("Obstacle should be on this direction " + bugDirection.rotateLeft().rotateLeft());
                            System.out.println("Obstacle coord: " + rc.getLocation().add(bugDirection).add(bugDirection.rotateLeft().rotateLeft()));
                            System.out.println(Utilities.doIntersect(startingLocation, targetLocation, rc.getLocation(), rc.getLocation().add(bugDirection)));
                            if (Utilities.doIntersect(startingLocation, targetLocation, rc.getLocation(), rc.getLocation().add(bugDirection))) {
                                tracingObstacle = false;
                            }
                            rc.move(bugDirection);
                            break;
                        }
                        bugDirection = bugDirection.rotateRight();
                    }
                }
                else if (rc.canMove(d) && rc.sensePassability(rc.getLocation().add(d)) >= passabilityThreshold) {
                    rc.move(d);
                    System.out.println("Moved on the line towards target" + d);
                    oldPositionOnTargetLine = rc.getLocation();
                    bugDirection = null;
                }
                else { // Can't move towards targetLocation
                    tracingObstacle = true;
                    if (bugDirection == null) {
                        bugDirection = d.rotateRight();
                        System.out.println("Cannot move towards target, rotating right");
                    }
                    for (int i = 0; i < 8; i++) {
                        System.out.println("bugDirection " + bugDirection + "Can move there "  + rc.canMove(bugDirection) + "Passability at that spot " + rc.sensePassability(rc.getLocation().add(bugDirection)));
                        if (rc.canMove(bugDirection) && rc.sensePassability(rc.getLocation().add(bugDirection)) >= passabilityThreshold) {
                            rc.move(bugDirection);
                            break;
                        }
                        bugDirection = bugDirection.rotateRight();
                    }
                }
                Clock.yield();
            }
        }
    }
}

