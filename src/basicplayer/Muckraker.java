package basicplayer;
import battlecode.common.*;

public class Muckraker extends Robot {

    static final int MUCKRAKER_SENSOR_RADIUS_SQUARED = 30;

    public Muckraker(RobotController rc) throws GameActionException {
        super(rc);
    }

    static void exploreRadiallyOutward(RobotController rc) throws GameActionException {
    }


    @Override
    public void run() throws GameActionException {
        getSensedSquares();
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        if (RobotPlayer.tryMove(RobotPlayer.randomDirection()))
            System.out.println("I moved!");
    }

    @Override
    public int getSenseRadiusSquared() {
        return MUCKRAKER_SENSOR_RADIUS_SQUARED;
    }
}
