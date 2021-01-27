package betterplayer;

import battlecode.common.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static betterplayer.RobotPlayer.rc;

public class Politician extends Robot {

    static final int POLITICIAN_SENSOR_RADIUS_SQUARED = 25;
    static Map<MapLocation, Set<Integer>> politicianAssignments = new HashMap<>();

    static final int POLITICIAN_ACTION_RADIUS = 9;
    MapLocation targetLocation;

    public Politician(RobotController rc) throws GameActionException {
        super(rc);

        MapLocation location = getCommandFromEC();
        if (location != null){
            targetLocation = location;
            System.out.println("GOT A NEW TARGET BRO at " + location.toString());
        } else {
            targetLocation = null;
        }
    }

    @Override
    public void run() throws GameActionException {
        senseSquares();
        MapLocation location = getCommandFromEC();
        System.out.println(rc.getFlag(enlightmentCenterId));
        System.out.println("EC ID: " + enlightmentCenterId);

        int distance = enlightmentCenterLocation.distanceSquaredTo(rc.getLocation());

        if (targetLocation == null && location != null){
            targetLocation = location;
            System.out.println("GOT A NEW TARGET BRO at " + location.toString());
        }
        // If we have a target
        if (targetLocation != null){
            System.out.println("My target Location Bro: " + targetLocation.toString());
//            RobotPlayer.basicBugStraightLine(targetLocation, true);
            RobotPlayer.basicBugStraightLineWithIgnoreObstacle(targetLocation, true);

            Team enemy = rc.getTeam().opponent();
            int actionRadius = rc.getType().actionRadiusSquared;
            RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
            if ((attackable.length != 0 || targetLocation.distanceSquaredTo(rc.getLocation()) <= actionRadius) && rc.canEmpower(actionRadius)) {
                System.out.println("empowering...");
                rc.empower(actionRadius);
                System.out.println("empowered");
                return;
            }
        } else if (distance < 5) {

            Direction direction = rc.getLocation().directionTo(enlightmentCenterLocation).opposite();
            if (rc.canMove(direction)) {
                rc.move(direction);
            }
        }


    }

    @Override
    public int getSenseRadiusSquared() {
        return POLITICIAN_SENSOR_RADIUS_SQUARED;
    }

}
