package basicplayer;
import battlecode.common.*;
import com.sun.tools.javac.util.List;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class Slanderer extends Robot {

    static final int SLANDERER_SENSOR_RADIUS_SQUARED = 20;
    boolean movedAwayFromEC = false;
    Direction spawnDirection;
    Direction slandererDirection;
    boolean inPosition = false;

    int highwayExit = 0;
    int highwayCounter = 0;
    boolean exitedHighway = false;

    public Slanderer(RobotController rc) throws GameActionException {
        super(rc);
    }


    static double findCosine(MapLocation p1, MapLocation p2){
        double x = p2.x - p1.x;
        double hypotenuse = Math.pow(p1.distanceSquaredTo(p2),.5);
        return x/hypotenuse;
    }

    static double findSine(MapLocation p1, MapLocation p2){
        double y = p2.y - p1.y;
        double hypotenuse = Math.pow(p1.distanceSquaredTo(p2), .5);
        return y/hypotenuse;
    }

    private Direction[] getDirectionForOrganization (Direction spawnDir) {
        // slandererDirection = (rc.getID()%2 == 0) ? RobotPlayer.directions[slandererDirection.ordinal()-1] : RobotPlayer.directions[slandererDirection.ordinal()+1];
        switch (spawnDir) {
            case NORTH: case NORTHEAST: case NORTHWEST: case SOUTH: case SOUTHEAST: case SOUTHWEST:
                return new Direction[]{Direction.NORTH, Direction.SOUTH};
            case WEST: case EAST:
                return new Direction[]{Direction.EAST, Direction.WEST};
            default:
                return new Direction[]{spawnDir};
        }

    }

    private void moveOneUnitAwayFromEC() throws GameActionException {
        // Get the direction that it was spawned in
        int currentID = rc.getID();
        int originECID = enlightmentCenterId;
        System.out.println(originECID);
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2);
        MapLocation originECLocation = null;
        for (RobotInfo robot : nearbyRobots) {
            if (robot.ID == originECID) {
                originECLocation = robot.location;
                spawnDirection = rc.getLocation().directionTo(originECLocation).opposite();
                slandererDirection = spawnDirection;
                System.out.println("My initial starting direction is in this direction : " + spawnDirection);
                break;
            }
        }

        System.out.println(spawnDirection);

        // Edge cases after being spawned from EC -- move directly north or south from spawn OR move away from EC
        if ((spawnDirection == Direction.NORTHEAST || spawnDirection == Direction.NORTHWEST) && rc.canMove(Direction.NORTH)) {
            rc.move(Direction.NORTH);
            movedAwayFromEC = true;
            inPosition = true;
        } else if ((spawnDirection == Direction.SOUTHEAST || spawnDirection == Direction.SOUTHWEST) && rc.canMove(Direction.SOUTH)){
            rc.move(Direction.SOUTH);
            movedAwayFromEC = true;
            inPosition = true;
        }
        else if (rc.canMove(spawnDirection)) {
            rc.move(spawnDirection);
            movedAwayFromEC = true;
        }

    }

    @Override
    public void run() throws GameActionException {
//        getSensedSquares();
        if (!movedAwayFromEC) {
            System.out.println("Currently moving one unit away from the EC");
            moveOneUnitAwayFromEC();
        }
        else if (!inPosition) {
            if ((slandererDirection != Direction.NORTHEAST && slandererDirection != Direction.NORTHWEST && slandererDirection != Direction.SOUTHEAST && slandererDirection != Direction.SOUTHWEST )) { // On highway path
                if (highwayCounter < highwayExit) {
                    System.out.println("Moving on highway");
                    if (rc.canMove(spawnDirection)) {
                        rc.move(spawnDirection);
                        highwayCounter++;
                        System.out.println("Counter : " +highwayCounter + " Exit :" + highwayExit );
                    }
                } else if (highwayCounter == highwayExit) {
                    // if ID is even, go on CCW branch, otherwise go on CW branch
                    System.out.println("At the exit, current direction is " + slandererDirection);
                    System.out.println((spawnDirection.ordinal()-1)%8);
                    int ordinalDirection = (rc.getID()%2 == 0) ? (spawnDirection.ordinal()-1) : (spawnDirection.ordinal()+1);
                    int ordinalDirectionModded;
                    if (ordinalDirection < 0) {
                        ordinalDirectionModded = RobotPlayer.directions.length + ordinalDirection;
                    } else {
                        ordinalDirectionModded = ordinalDirection%8;
                    }
                    slandererDirection = RobotPlayer.directions[ordinalDirectionModded];
                    System.out.println("Turning towards the " + slandererDirection + " direction");

                    while (true) { // Leave the highway
                        if (rc.canMove(slandererDirection)) {
                            rc.move(slandererDirection);
                            break;
                        }
                        Clock.yield();
                    }
                }
            }
            else { // On a diagonal
                while(true) {
                    for (Direction direction : getDirectionForOrganization(spawnDirection)) {
                        System.out.println("Trying to move north or south");
                        if (rc.canMove(direction)) {
                            System.out.println(direction);
                            rc.move(direction);
                            inPosition = true;
                            break;
                        }
                    }
                    if (!inPosition) {
                        if (rc.canMove(slandererDirection)) {
                            rc.move(slandererDirection);
                        }
                    } else {
                        break;
                    }
                    Clock.yield();
                }
            }
        }

//        positionAroundEC();
//        double horizontalForce = 0.0;
//        double verticalForce = 0.0;
//        MapLocation curLocation = rc.getLocation();
//
//        for (MapLocation muckRakerLocation: RobotPlayer.enemyMuckRaker){
//            double cosine = findCosine(curLocation, muckRakerLocation);
//            double sine = findSine(curLocation, muckRakerLocation);
//            int distanceSquared = curLocation.distanceSquaredTo(muckRakerLocation);
//            horizontalForce += cosine*MUCKRAKER_EFFECT/distanceSquared;
//            verticalForce += sine*MUCKRAKER_EFFECT/distanceSquared;
//        }
//
//        for (MapLocation politicianLocation: RobotPlayer.enemyPoliticians){
//            double cosine = findCosine(curLocation, politicianLocation);
//            double sine = findSine(curLocation, politicianLocation);
//            int distanceSquared = curLocation.distanceSquaredTo(politicianLocation);
//            horizontalForce += cosine*POLITICIAN_EFFECT/distanceSquared;
//            verticalForce += sine*POLITICIAN_EFFECT/distanceSquared;
//        }
//
//        for (MapLocation slandererLocation: RobotPlayer.confirmedSlanderers){
//            double cosine = findCosine(curLocation, slandererLocation);
//            double sine = findSine(curLocation, slandererLocation);
//            int distanceSquared = curLocation.distanceSquaredTo(slandererLocation);
//            horizontalForce += cosine*CONFIRMED_SLANDERER_EFFECT/distanceSquared;
//            verticalForce += sine*CONFIRMED_SLANDERER_EFFECT/distanceSquared;
//        }
//
//        for (MapLocation enemyEnlightmentCenter: RobotPlayer.enemyEnlightmentCenters){
//            double cosine = findCosine(curLocation, enemyEnlightmentCenter);
//            double sine = findSine(curLocation, enemyEnlightmentCenter);
//            int distanceSquared = curLocation.distanceSquaredTo(enemyEnlightmentCenter);
//            horizontalForce += cosine*ENEMY_ENLIGHTMENT_CENTER_EFFECT/distanceSquared;
//            verticalForce += sine*ENEMY_ENLIGHTMENT_CENTER_EFFECT/distanceSquared;
//        }
//
//        Integer enlightmentCenterId = RobotPlayer.enlightmentCenterIds.get(rc.getID());
//        MapLocation enlightmentCenterLocation = rc.senseRobot(enlightmentCenterId).getLocation();
//        double cosine = findCosine(curLocation, enlightmentCenterLocation);
//        double sine = findSine(curLocation, enlightmentCenterLocation);
//        int distanceSquared = curLocation.distanceSquaredTo(enlightmentCenterLocation);
//        horizontalForce += cosine*ENLIGHTMENT_CENTER_EFFECT/distanceSquared;
//        verticalForce += sine*ENLIGHTMENT_CENTER_EFFECT/distanceSquared;
//
//
//        if (RobotPlayer.tryMove(RobotPlayer.randomDirection()))
//            System.out.println("I moved!");
    }

    @Override
    public int getSenseRadiusSquared() {
        return SLANDERER_SENSOR_RADIUS_SQUARED;
    }
}
