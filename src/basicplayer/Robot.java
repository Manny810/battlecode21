package basicplayer;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;

import java.util.HashMap;
import java.util.Map;

public abstract class Robot {

    RobotController rc;
    Team allyTeam;
    Integer enlightmentCenterId;

    // a dictionary mapping a map location to it's passability
    static Map<MapLocation, Double> passabilities = new HashMap<>();



    public Robot(RobotController robotController) throws GameActionException {
        rc = robotController;
        allyTeam = rc.getTeam();

    }

    public abstract void run() throws GameActionException;
}
