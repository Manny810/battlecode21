package basicplayer;
import battlecode.common.*;


public class Slanderer {

    static final int MUCKRAKER_EFFECT = 20;
    static final int POLITICIAN_EFFECT = 10;
    static final int CONFIRMED_SLANDERER_EFFECT = 5;
    static final int ENEMY_ENLIGHTMENT_CENTER_EFFECT = 5;
    static final int ENLIGHTMENT_CENTER_EFFECT = 1;

    static double findCosine(MapLocation p1, MapLocation p2){
        double x = p2.x - p1.x;
        double hypotenuse = Math.pow(p1.distanceSquaredTo(p2),.5);
        return x/hypotenuse;
    }

    static double findSine(MapLocation p1, MapLocation p2){
        double y = p2.y - p1.y;
        double hypotenuse = Math.pow(p1.distanceSquaredTo(p2), .5);
        return y/hypotenuse;
    }

    static void runSlanderer(RobotController rc) throws GameActionException {

        double horizontalForce = 0.0;
        double verticalForce = 0.0;
        MapLocation curLocation = rc.getLocation();

        for (MapLocation muckRakerLocation: RobotPlayer.enemyMuckRaker){
            double cosine = findCosine(curLocation, muckRakerLocation);
            double sine = findSine(curLocation, muckRakerLocation);
            int distanceSquared = curLocation.distanceSquaredTo(muckRakerLocation);
            horizontalForce += cosine*MUCKRAKER_EFFECT/distanceSquared;
            verticalForce += sine*MUCKRAKER_EFFECT/distanceSquared;
        }

        for (MapLocation politicianLocation: RobotPlayer.enemyPoliticians){
            double cosine = findCosine(curLocation, politicianLocation);
            double sine = findSine(curLocation, politicianLocation);
            int distanceSquared = curLocation.distanceSquaredTo(politicianLocation);
            horizontalForce += cosine*POLITICIAN_EFFECT/distanceSquared;
            verticalForce += sine*POLITICIAN_EFFECT/distanceSquared;
        }

        for (MapLocation slandererLocation: RobotPlayer.confirmedSlanderers){
            double cosine = findCosine(curLocation, slandererLocation);
            double sine = findSine(curLocation, slandererLocation);
            int distanceSquared = curLocation.distanceSquaredTo(slandererLocation);
            horizontalForce += cosine*CONFIRMED_SLANDERER_EFFECT/distanceSquared;
            verticalForce += sine*CONFIRMED_SLANDERER_EFFECT/distanceSquared;
        }

        for (MapLocation enemyEnlightmentCenter: RobotPlayer.enemyEnlightmentCenters){
            double cosine = findCosine(curLocation, enemyEnlightmentCenter);
            double sine = findSine(curLocation, enemyEnlightmentCenter);
            int distanceSquared = curLocation.distanceSquaredTo(enemyEnlightmentCenter);
            horizontalForce += cosine*ENEMY_ENLIGHTMENT_CENTER_EFFECT/distanceSquared;
            verticalForce += sine*ENEMY_ENLIGHTMENT_CENTER_EFFECT/distanceSquared;
        }

        Integer enlightmentCenterId = RobotPlayer.enlightmentCenterIds.get(rc.getID());
        MapLocation enlightmentCenterLocation = rc.senseRobot(enlightmentCenterId).getLocation();
        double cosine = findCosine(curLocation, enlightmentCenterLocation);
        double sine = findSine(curLocation, enlightmentCenterLocation);
        int distanceSquared = curLocation.distanceSquaredTo(enlightmentCenterLocation);
        horizontalForce += cosine*ENLIGHTMENT_CENTER_EFFECT/distanceSquared;
        verticalForce += sine*ENLIGHTMENT_CENTER_EFFECT/distanceSquared;

        
        if (RobotPlayer.tryMove(RobotPlayer.randomDirection()))
            System.out.println("I moved!");
    }
}
