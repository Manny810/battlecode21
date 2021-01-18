package basicplayer;

import battlecode.common.*;

import java.util.HashSet;
import java.util.Set;

import static basicplayer.RobotPlayer.rc;

public class Politician extends Robot {


    static final MapLocation testTarget = new MapLocation(25927, 25299);
    static final int POLITICIAN_ACTION_RADIUS = 9;

    public Politician(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        MapLocation enlightmentCenterTarget = getTarget();

        // If we have a target
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
                if (!RobotPlayer.politicianAssignments.containsKey(neutralEC)){
                    Set<Integer> set = new HashSet<>();
                    set.add(rc.getID());
                    RobotPlayer.politicianAssignments.put(neutralEC, set);
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

    private MapLocation getTarget() {
        int id = rc.getID();
        for (MapLocation neutralEc: RobotPlayer.politicianAssignments.keySet()){
            if (RobotPlayer.politicianAssignments.get(neutralEc).contains(id)){
                return neutralEc;
            }
        }
        return null;
    }

}
