package basicplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.Team;

public abstract class Robot {

    RobotController rc;
    Team allyTeam;


    public Robot(RobotController robotController) throws GameActionException {
        rc = robotController;
        allyTeam = rc.getTeam();

    }

    public abstract void run() throws GameActionException;
}
