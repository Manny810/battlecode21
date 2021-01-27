package betterplayer;
import battlecode.common.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public strictfp class RobotPlayer {
    static RobotController rc;


    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };


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

    static final Direction[] cardinalDirections = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST,
    };

    static final Direction[] ordinalDirections = {
            Direction.NORTHEAST,
            Direction.SOUTHEAST,
            Direction.SOUTHWEST,
            Direction.NORTHWEST
    };

    static final int[] turningPriority = {1, -1, 2, -2, 3, -3, 4};

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
        betterplayer.RobotPlayer.rc = rc;

        turnCount = 0;
        Robot robot;
        switch (rc.getType()) {
            case ENLIGHTENMENT_CENTER:
                robot = new EnlightenmentCenter(rc);
                break;
            case POLITICIAN:
                robot = new Politician(rc);
                break;
            case SLANDERER:
                robot = new Slanderer(rc);
                break;
            case MUCKRAKER:
                robot = new Muckraker(rc); 
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + rc.getType());
        }
        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            System.out.println(turnCount);
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                robot.run();
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }


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



    private static final double passabilityThreshold = 0.5;
    static Direction bugDirection = null;



    static void basicBugStraightLine(MapLocation targetLocation, boolean checkWithinSenseRadius) throws GameActionException {
        MapLocation startingLocation = rc.getLocation();
        boolean tracingObstacle = false;
        while (true) {
            Direction d = rc.getLocation().directionTo(targetLocation);
            if (rc.getLocation().equals(targetLocation) || (checkWithinSenseRadius && (rc.getLocation().isWithinDistanceSquared(targetLocation, rc.getType().actionRadiusSquared - 1 )))) { // Has reached target location or at least within sensor radius
                System.out.println("I have reached the target location");
                // perform some action based on the unit
                break;
            } else if (rc.isReady()) { // Moving on the line  towards targetLocation
                if (tracingObstacle) {
                    System.out.println("Now tracing obstacle");
                    for (int i = 0; i < 8; i++) {
                        System.out.println(bugDirection);
                        if (rc.canMove(bugDirection) &&
                                rc.sensePassability(rc.getLocation().add(bugDirection)) >= passabilityThreshold &&
                                (rc.sensePassability(rc.getLocation().add(bugDirection).add(Utilities.leftHandSideForCurrentDirection(bugDirection))) < passabilityThreshold ||
                                        (rc.isLocationOccupied(rc.getLocation().add(bugDirection).add(Utilities.leftHandSideForCurrentDirection(bugDirection)))))) { // Check if there's obstacle to left while tracing
                            if (rc.isLocationOccupied(rc.getLocation().add(bugDirection).add(Utilities.leftHandSideForCurrentDirection(bugDirection)))){
                                if ((Utilities.checkTypeAtLocation(rc, d, RobotType.MUCKRAKER) && Utilities.checkFlagAtLocation(rc, d))) {
                                    System.out.println("---------------------LOCATION OCCUPIED BY SOMETHING------------------");
                                    Clock.yield();
                                } else {
                                    tracingObstacle = false;
                                }
                            }
                            if (Utilities.doIntersect(startingLocation, targetLocation, rc.getLocation(), rc.getLocation().add(bugDirection)) || Utilities.doIntersect(startingLocation, targetLocation, rc.getLocation(), rc.getLocation().add(bugDirection).add(bugDirection)) ) {
                                tracingObstacle = false;
                                startingLocation = rc.getLocation();
                            }
                            rc.move(bugDirection);
                            break;
                        }
                        bugDirection = bugDirection.rotateRight();
                    }
                    bugDirection = bugDirection.rotateLeft();
                }
                else if (rc.canMove(d) && rc.sensePassability(rc.getLocation().add(d)) >= passabilityThreshold) {
                    rc.move(d);
                    System.out.println("Moved on the line towards target" + d);
                    bugDirection = null;
                }
                else if ((Utilities.checkTypeAtLocation(rc, d, RobotType.MUCKRAKER) && Utilities.checkFlagAtLocation(rc, d)) &&
                        rc.isLocationOccupied((rc.getLocation()).add(d)) && rc.sensePassability(rc.getLocation().add(d)) >= passabilityThreshold) {
                    Clock.yield();
                }
                else { // Can't move towards targetLocation
                    tracingObstacle = true;
                    if (bugDirection == null) {
                        bugDirection = d.rotateRight();
                    }
                    for (int i = 0; i < 8; i++) {
                        if (rc.canMove(bugDirection) && rc.sensePassability(rc.getLocation().add(bugDirection)) >= passabilityThreshold) {
                            rc.move(bugDirection);
                            break;
                        }
                        bugDirection = bugDirection.rotateRight();
                    }
                    bugDirection = bugDirection.rotateLeft();
                }
                Clock.yield();
            }
        }
    }


    static void basicBugStraightLineWithIgnoreObstacle (MapLocation targetLocation, boolean checkWithinSenseRadius) throws GameActionException {
        MapLocation startingLocation = rc.getLocation();
        boolean tracingObstacle = false;
        boolean bypassObstacle = false;
        while (true) {
            Direction d = rc.getLocation().directionTo(targetLocation);
            int previousDistanceToTarget = rc.getLocation().distanceSquaredTo(targetLocation);
            if (rc.getLocation().equals(targetLocation) || (checkWithinSenseRadius && (rc.getLocation().isWithinDistanceSquared(targetLocation, rc.getType().actionRadiusSquared - 1 )))) { // Has reached target location or at least within sensor radius
                System.out.println("I have reached the target location");
                // perform some action based on the unit
                break;
            } else if (rc.isReady() && !bypassObstacle) { // Moving on the line towards targetLocation
                System.out.println("START OF ROUND  " + bugDirection );
                if (tracingObstacle) {
                    System.out.println("Now tracing obstacle");
                    for (int i = 0; i < 8; i++) {
                        System.out.println(bugDirection);
                        if (rc.canMove(bugDirection) &&
                                rc.sensePassability(rc.getLocation().add(bugDirection)) >= passabilityThreshold &&
                                (rc.sensePassability(rc.getLocation().add(bugDirection).add(Utilities.leftHandSideForCurrentDirection(bugDirection))) < passabilityThreshold ||
                                        (rc.isLocationOccupied(rc.getLocation().add(bugDirection).add(Utilities.leftHandSideForCurrentDirection(bugDirection)))))) { // Check if there's obstacle to left while tracing
                            if (rc.isLocationOccupied(rc.getLocation().add(bugDirection).add(Utilities.leftHandSideForCurrentDirection(bugDirection)))){
                                if ((Utilities.checkTypeAtLocation(rc, d, RobotType.MUCKRAKER) && Utilities.checkFlagAtLocation(rc, d))) {
                                    System.out.println("---------------------LOCATION OCCUPIED BY SOMETHING------------------");
                                    Clock.yield();
                                } else {
                                    tracingObstacle = false;
                                }
                            }
                            if (Utilities.doIntersect(startingLocation, targetLocation, rc.getLocation(), rc.getLocation().add(bugDirection)) || Utilities.doIntersect(startingLocation, targetLocation, rc.getLocation(), rc.getLocation().add(bugDirection).add(bugDirection)) ) {
                                tracingObstacle = false;
                                startingLocation = rc.getLocation();
                            }
                            rc.move(bugDirection);
                            break;
                        }
                        bugDirection = bugDirection.rotateRight();
                    }
                    int currentDistanceToTarget = rc.getLocation().distanceSquaredTo(targetLocation);
                    if (currentDistanceToTarget > previousDistanceToTarget) {
                        bypassObstacle = true;
                        System.out.println("------Bypass-----");
                    }
                    bugDirection = bugDirection.rotateLeft();
                }
                else if ((rc.canMove(d) && rc.sensePassability(rc.getLocation().add(d)) >= passabilityThreshold)) {
                    rc.move(d);
                    System.out.println("Moved on the line towards target" + d);
                    bugDirection = null;
                }
                else if ((Utilities.checkTypeAtLocation(rc, d, RobotType.MUCKRAKER) && Utilities.checkFlagAtLocation(rc, d)) &&
                        rc.isLocationOccupied((rc.getLocation()).add(d)) && rc.sensePassability(rc.getLocation().add(d)) >= passabilityThreshold) {
                    Clock.yield();
                }
                else { // Can't move towards targetLocation
                    tracingObstacle = true;
                    if (bugDirection == null) {
                        bugDirection = d.rotateRight();
                    }
                    for (int i = 0; i < 8; i++) {
                        if (rc.canMove(bugDirection) && rc.sensePassability(rc.getLocation().add(bugDirection)) >= passabilityThreshold) {
                            rc.move(bugDirection);
                            break;
                        }
                        bugDirection = bugDirection.rotateRight();
                    }
                    bugDirection = bugDirection.rotateLeft();
                }
            } else if (rc.isReady() && bypassObstacle) {
                double currentPassability = rc.sensePassability(rc.getLocation());
                if (rc.canMove(d)) {
                    System.out.println("bypass while move towards target  "  +d );
                    rc.move(d);
//                    bugDirection = null;
                    bugDirection = d;
                } else if (rc.isLocationOccupied((rc.getLocation()).add(d))) { // Location is occupied by some unit
                    System.out.println("something in the way");

                    if (Utilities.checkTypeAtLocation(rc, d , RobotType.MUCKRAKER) && Utilities.checkFlagAtLocation(rc, d)) { // If it is a movable unit (politician/muckraker)
                        System.out.println("something in the way");
                        Clock.yield();
                    } else { // blocked by an immovable unit
                        for (int i : turningPriority) {
                            int nextOrdinal = bugDirection.ordinal() + i;
                            int nextOrdinalModded;
                            if (nextOrdinal < 0) {
                                nextOrdinalModded = RobotPlayer.directions.length + nextOrdinal;
                            } else {
                                nextOrdinalModded = nextOrdinal % 8;
                            }
                            Direction nextDirection = RobotPlayer.directions[nextOrdinalModded];
                            bugDirection = nextDirection;
                            if (rc.canMove(bugDirection)) {
                                rc.move(bugDirection);
                                break;
                            }
                        }
                    }
                }
                if (currentPassability < passabilityThreshold && rc.sensePassability(rc.getLocation()) >= passabilityThreshold) {
                    bypassObstacle = false;
                }
            }
            Clock.yield();
        }
    }

}

