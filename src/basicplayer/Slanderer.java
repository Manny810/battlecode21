package basicplayer;
import battlecode.common.*;
import com.sun.tools.javac.util.List;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class Slanderer extends Robot {

//    static final ArrayList<Direction> diagonalDirections =new ArrayList<>(List.of(Direction.NORTHEAST,
//            Direction.SOUTHEAST,
//            Direction.NORTHWEST,
//            Direction.SOUTHWEST));
    static final int SLANDERER_SENSOR_RADIUS_SQUARED = 20;

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

    private void positionAroundEC() throws GameActionException {
        // Get the direction that it was spawned in
        int currentID = rc.getID();
        int originEnlightenmentCenterID = RobotPlayer.getEnlightenmentCenterIds().get(currentID);
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(1);
        MapLocation originECLocation = null;
        for (RobotInfo robot : nearbyRobots) {
            if (robot.ID == originEnlightenmentCenterID) {
                originECLocation = robot.location;
            }
        }
        Direction slandererDirection = Objects.requireNonNull(rc.getLocation().directionTo(originECLocation)).opposite();
        System.out.println(slandererDirection);

        // Edge cases after being spawned from EC -- move directly north or south from spawn OR move away from EC
        if ((slandererDirection == Direction.NORTHEAST || slandererDirection == Direction.NORTHWEST) && rc.canMove(Direction.NORTH)) {
            rc.move(Direction.NORTH);
        } else if ((slandererDirection == Direction.SOUTHEAST || slandererDirection == Direction.SOUTHWEST) && rc.canMove(Direction.SOUTH)){
            rc.move(Direction.SOUTH);
        }
        else if (rc.canMove(slandererDirection)) {
            rc.move(slandererDirection);
        }

        while (true) {
            if (!(slandererDirection == Direction.NORTHEAST || slandererDirection == Direction.NORTHWEST || slandererDirection == Direction.SOUTHEAST || slandererDirection == Direction.SOUTHWEST )) { // On highway path
                for (int i = 0; i < 2; i++) { //Coded to move only to first exit of highway
                    if (rc.canMove(slandererDirection)) {
                        rc.move(slandererDirection);
                    }
                }

                // if ID is even, go on CCW branch, otherwise go on CW branch
                slandererDirection = (rc.getID()%2 == 0) ? RobotPlayer.directions[slandererDirection.ordinal()-1] : RobotPlayer.directions[slandererDirection.ordinal()+1];
            }
            else if (rc.canMove(Direction.NORTH)) {
                rc.move(Direction.NORTH);
                break;
            } else if (rc.canMove(Direction.SOUTH)) {
                rc.move(Direction.SOUTH);
                break;
            } else {
                rc.move(slandererDirection);
            }
            Clock.yield();
        }
    }

    @Override
    public void run() throws GameActionException {
        getSensedSquares();
        positionAroundEC();
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
