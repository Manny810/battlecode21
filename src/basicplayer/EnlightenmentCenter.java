package basicplayer;
import battlecode.common.*;

public class EnlightenmentCenter {


    static final Direction[] directions = RobotPlayer.directions;
    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static int counter = 0;
    static void runEnlightenmentCenter(RobotController rc) throws GameActionException {
        RobotType toBuild = spawnRobot();
        int influence = 50;
        for (Direction dir: directions) {
            if (rc.canBuildRobot(toBuild, dir, influence) && rc.getTeam().equals(Team.A) && counter == 0) {
                rc.buildRobot(toBuild,dir, influence);
                // get the robot's id that you just built and register it in enlightmentCenterIds
                MapLocation newLocation = rc.adjacentLocation(dir);
                RobotInfo[] nearbyRobots = rc.senseNearbyRobots(1);
                for (RobotInfo robot : nearbyRobots){
                    if (robot.getLocation().equals(newLocation)){
                        int id = robot.getID();
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
