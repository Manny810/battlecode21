package basicplayer;
import battlecode.common.*;


public class Slanderer {

    static void runSlanderer(RobotController rc) throws GameActionException {
        if (RobotPlayer.tryMove(RobotPlayer.randomDirection()))
            System.out.println("I moved!");
    }
}
