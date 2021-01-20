package basicplayer;
import battlecode.common.*;

import java.util.Objects;

public class Muckraker extends Robot {

    static final int MUCKRAKER_SENSOR_RADIUS_SQUARED = 30;

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
            }
        }
        Direction muckrakerDirection = Objects.requireNonNull(rc.getLocation().directionTo(originECLocation)).opposite();
        System.out.println(muckrakerDirection);

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

            }
            Clock.yield();
        }


    }


    @Override
    public void run() throws GameActionException {

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
