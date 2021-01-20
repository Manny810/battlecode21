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
    MapLocation enlightmentCenterTarget;

    public Politician(RobotController rc) throws GameActionException {
        super(rc);
        round = 0;
        enlightmentCenterTarget = null;
    }

    @Override
    public void run() throws GameActionException {
        getSensedSquares();
        System.out.println(neutralEnlightmentCenters);
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

        } else if (enlightmentCenterTarget != null){
            int distance = enlightmentCenterTarget.distanceSquaredTo(rc.getLocation());
            if (rc.canEmpower(distance)){ // if it can empower the neutral ec
                rc.empower(distance);
            }
            else if (distance > POLITICIAN_ACTION_RADIUS){ // if it's too far
                RobotPlayer.basicBugStraightLine(enlightmentCenterTarget);
            }
        } else { // doesn't have a target and needs to get one
            for (MapLocation neutralEC: neutralEnlightmentCenters){
                if (!politicianAssignments.containsKey(neutralEC)){
                    Set<Integer> set = new HashSet<>();
                    set.add(rc.getID());
                    politicianAssignments.put(neutralEC, set);
                    enlightmentCenterTarget = neutralEC;
                    System.out.println("POOPOO " + rc.getID() + ": " + neutralEC.toString());
                }
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
