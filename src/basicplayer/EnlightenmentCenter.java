package basicplayer;
import battlecode.common.*;

public class EnlightenmentCenter {


    static final Direction[] directions = RobotPlayer.directions;
    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static final int POLITICIAN_RATIO = 3;
    static final int SLANDERER_RATIO = 1;
    static final int MUCKRAKER_RATIO = 6;
    static final int TOTAL_RATIO = POLITICIAN_RATIO + SLANDERER_RATIO + MUCKRAKER_RATIO;

    static final int SLANDERER_INFLUENCE = 100;
    static final int POLITICIAN_INFLUENCE = 10;
    static final int MUCKRAKER_INFLUENCE = 1;

    static int counter = 0;
    static void runEnlightenmentCenter(RobotController rc) throws GameActionException {
        int id = rc.getID();
        int slanderer = RobotPlayer.slandererCount.get(id);
        int politician = RobotPlayer.politicianCount.get(id);
        int muckraker = RobotPlayer.muckrakerCount.get(id);

        int total = slanderer + politician + muckraker;

        RobotType toBuild;
        int influence;
        if (slanderer/total < SLANDERER_RATIO/TOTAL_RATIO){
            toBuild = RobotType.SLANDERER;
            influence = SLANDERER_INFLUENCE;
        } else if (muckraker/total < MUCKRAKER_RATIO/TOTAL_RATIO){
            toBuild = RobotType.MUCKRAKER;
            influence = MUCKRAKER_INFLUENCE;
        } else {
            toBuild = RobotType.POLITICIAN;
            influence = POLITICIAN_INFLUENCE;
        }

        for (Direction dir: directions) {
            if (rc.canBuildRobot(toBuild, dir, influence) && rc.getTeam().equals(Team.A) && counter == 0) {
                rc.buildRobot(toBuild,dir, influence);
                // get the robot's id that you just built and register it in enlightmentCenterIds
                MapLocation newLocation = rc.adjacentLocation(dir);
                RobotInfo[] nearbyRobots = rc.senseNearbyRobots(1);
                for (RobotInfo robot : nearbyRobots){
                    if (robot.getLocation().equals(newLocation)){
                        RobotPlayer.enlightmentCenterIds.put(id, rc.getID());
                    }
                }
                counter++;
            } else {
                break;
            }
        }
    }

    static RobotType spawnRobot() {
        return spawnableRobot[0];
    }
}
