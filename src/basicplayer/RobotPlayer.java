package basicplayer;
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

    static void basicBugStraightLine(MapLocation targetLocation) throws GameActionException {
        MapLocation startingLocation = rc.getLocation();
        boolean tracingObstacle = false;
        while (true) {
            Direction d = rc.getLocation().directionTo(targetLocation);
            System.out.println("Direction to target" + d);
            if (rc.getLocation().equals(targetLocation)) {
                System.out.println("I have reached the target location");
                // perform some action based on the unit
                break;
            } else if (rc.isReady()) { // Moving on the line  towards targetLocation
                if (tracingObstacle) {
                    System.out.println("Now tracing obstacle");
                    for (int i = 0; i < 8; i++) {
                        if (rc.canMove(bugDirection) &&
                                rc.sensePassability(rc.getLocation().add(bugDirection)) >= passabilityThreshold &&
                                (rc.sensePassability(rc.getLocation().add(bugDirection).add(bugDirection.rotateLeft().rotateLeft())) < passabilityThreshold ||
                                        (rc.isLocationOccupied(rc.getLocation().add(bugDirection).add(bugDirection.rotateLeft().rotateLeft()))) || rc.isLocationOccupied(rc.getLocation().add(bugDirection).add(bugDirection.rotateLeft().rotateLeft().rotateLeft())))) { // Check if there's obstacle to left while tracing
                            System.out.println("Will turn towards " + bugDirection);
                            System.out.println("Obstacle should be on this direction " + bugDirection.rotateLeft().rotateLeft());
                            System.out.println("Obstacle coord: " + rc.getLocation().add(bugDirection).add(bugDirection.rotateLeft().rotateLeft()));
                            System.out.println(Utilities.doIntersect(startingLocation, targetLocation, rc.getLocation(), rc.getLocation().add(bugDirection)));
                            if (Utilities.doIntersect(startingLocation, targetLocation, rc.getLocation(), rc.getLocation().add(bugDirection))) {
                                tracingObstacle = false;
                                startingLocation = rc.getLocation();
                            }
                            rc.move(bugDirection);
                            break;
                        }
                        bugDirection = bugDirection.rotateRight();
                    }
                    bugDirection.rotateLeft();
                }
                else if (rc.canMove(d) && rc.sensePassability(rc.getLocation().add(d)) >= passabilityThreshold) {
                    rc.move(d);
                    System.out.println("Moved on the line towards target" + d);
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
                    bugDirection = bugDirection.rotateLeft();
                }
                Clock.yield();
            }
        }
    }
}

