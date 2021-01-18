package basicplayer;

import battlecode.common.*;

import java.util.HashSet;
import java.util.Set;

public class Politician {

    static final MapLocation testTarget = new MapLocation(25927, 25299);
    static final int POLITICIAN_ACTION_RADIUS = 9;


    static void runPolitician(RobotController rc) throws GameActionException {
        MapLocation enlightmentCenterTarget = null;
        // If we have a target
        /**
        if (enlightmentCenterTarget != null){
            int distance = enlightmentCenterTarget.distanceSquaredTo(rc.getLocation());
            if (rc.canEmpower(distance)){ // if it can empower the neutral ec
                rc.empower(distance);
            }
            else if (distance > POLITICIAN_ACTION_RADIUS){ // if it's too far
                RobotPlayer.basicBugStraightLine(enlightmentCenterTarget);
            }
        } else { // doesn't have a target and needs to get one
            for (MapLocation neutralEC: RobotPlayer.neutralEnlightmentCenters){
                if (RobotPlayer.politicianAssignments.containsKey(neutralEC)){

                } else {
                    Set<Integer> set = new HashSet<>();
                    set.add(rc.getID());
                    RobotPlayer.politicianAssignments.put(neutralEC, set);
                    enlightmentCenterTarget = neutralEC;
                }
            }

        }
        **/
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
    }

    private void getTarget() {
    }

}
