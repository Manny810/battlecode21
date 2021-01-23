package basicplayer;

import battlecode.common.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static basicplayer.RobotPlayer.rc;

public class Politician extends Robot {

    static final int POLITICIAN_SENSOR_RADIUS_SQUARED = 25;
    int round;
    static Map<MapLocation, Set<Integer>> politicianAssignments = new HashMap<>();

    static final int POLITICIAN_ACTION_RADIUS = 9;
    MapLocation targetLocation;

    public Politician(RobotController rc) throws GameActionException {
        super(rc);
        round = 0;
        targetLocation = null;
    }

    @Override
    public void run() throws GameActionException {
        senseSquares();
        MapLocation location = getCommandFromEC();
        if (location != null){
            targetLocation = location;
        }
        // If we have a target
        if (round < 2){
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(8);
            for (RobotInfo robot: nearbyRobots){
                if (robot.getID() == enlightmentCenterId){
                    Direction direction = rc.getLocation().directionTo(robot.getLocation()).opposite();
                    if (rc.canMove(direction)){
                        rc.move(direction);
                        round++;
                    }
                }
            }

        } else if (targetLocation != null){
            RobotPlayer.basicBugStraightLine(targetLocation);

            Team enemy = rc.getTeam().opponent();
            int actionRadius = rc.getType().actionRadiusSquared;
            RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
            if ((attackable.length != 0 || targetLocation.distanceSquaredTo(rc.getLocation()) <= actionRadius) && rc.canEmpower(actionRadius)) {
                System.out.println("empowering...");
                rc.empower(actionRadius);
                System.out.println("empowered");
                return;
            }
        }

        /**
        RobotPlayer.basicBugStraightLine(testTarget);

        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
        if (RobotPlayer.tryMove(RobotPlayer.randomDirection()))
            System.out.println("I moved!");
         **/
    }

    @Override
    public int getSenseRadiusSquared() {
        return POLITICIAN_SENSOR_RADIUS_SQUARED;
    }

}
