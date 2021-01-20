package basicplayer;
import battlecode.common.*;
import com.sun.tools.javac.util.List;

import java.util.ArrayList;
import java.util.Objects;

public class Muckraker extends Robot {

    static final int MUCKRAKER_SENSOR_RADIUS_SQUARED = 30;
    Direction spawnDirection;
    Direction muckrakerDirection;


    public Muckraker(RobotController rc) throws GameActionException {
        super(rc);
    }



    private void exploreRadiallyOutward() throws GameActionException {
        // Get the direction that it was spawned in
        int currentID = rc.getID();
        int originEnlightenmentCenterID = enlightmentCenterId;
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2);
        MapLocation originECLocation = null;
        for (RobotInfo robot : nearbyRobots) {
            if (robot.ID == originEnlightenmentCenterID) {
                originECLocation = robot.location;
                spawnDirection = rc.getLocation().directionTo(originECLocation).opposite();
                muckrakerDirection = spawnDirection;
                System.out.println("My initial starting direction is in this direction : " + spawnDirection);
                break;
            }
        }

        while (true) {
            if (rc.onTheMap(rc.getLocation().add(muckrakerDirection))) {
                if (rc.canMove(muckrakerDirection)) {
                    rc.move(muckrakerDirection);
                    rc.detectNearbyRobots();
                    RobotInfo[] sensedRobots = rc.senseNearbyRobots();
                    for (RobotInfo robot : sensedRobots) {
                        if (robot.team == Team.NEUTRAL) {
                            MapLocation neutralECLocation = robot.location;
                            // add neutralECLocation to some dictionary
                        } else if (robot.team == allyTeam.opponent() && robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                            MapLocation enemyECLocation = robot.location;
                        }
                    }
                }
            } else { // Location not on the map
                ArrayList<Integer> blockedDirectionsOrdinals = new ArrayList<>();
                int blockedDirCounter = 0;
                for (Direction dir : RobotPlayer.cardinalDirections) {
                    if (!(rc.onTheMap(rc.getLocation().add(dir)))) {
                        blockedDirectionsOrdinals.add(dir.ordinal());
                    }
                }
                System.out.println("BLOCKED DIRECTIONS NUMBERS" + blockedDirectionsOrdinals);
                Direction[] blockedDirections = new Direction[blockedDirectionsOrdinals.size()];
                for (int i = 0 ; i < blockedDirectionsOrdinals.size(); i++) {
                    blockedDirections[i] = RobotPlayer.directions[blockedDirectionsOrdinals.get(i)];
                }
                System.out.println("BLOCKED DIRECTIONS : " + blockedDirections);
                Direction blockingWallDirection = findWallDirection(blockedDirections);
                Direction[] newDirections = bounceOffMapBoundary(muckrakerDirection, blockingWallDirection);
                System.out.println("WHAT IS THE WALL ? " + blockingWallDirection);
                System.out.println("WHAT NEW DIRECTIONS CAN I TRAVEL IN " + newDirections);
                if (newDirections.length == 1) {
                    muckrakerDirection = newDirections[0];
                } else if (newDirections.length == 2){
                    muckrakerDirection = (rc.getID()%2 ==0) ? newDirections[0] : newDirections[1];
                }
                if (rc.canMove(muckrakerDirection)) {
                    rc.move(muckrakerDirection);
                }
            }
            Clock.yield();
        }


    }


    @Override
    public void run() throws GameActionException {
//        getSensedSquares();

        exploreRadiallyOutward();
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
//        if (RobotPlayer.tryMove(RobotPlayer.randomDirection()))
//            System.out.println("I moved!");
    }

    @Override
    public int getSenseRadiusSquared() {
        return MUCKRAKER_SENSOR_RADIUS_SQUARED;
    }
}
