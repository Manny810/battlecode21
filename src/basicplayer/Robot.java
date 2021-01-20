package basicplayer;

import battlecode.common.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Robot {

    RobotController rc;
    Team allyTeam;
    Team enemyTeam;
    Integer enlightmentCenterId;

    // a set of MapLocations with detected but unknown bots
    static Set<MapLocation> detectedBots = new HashSet<>();

    // a set of map locations with known muckrakers
    static Set<MapLocation> enemyMuckRaker = new HashSet<>();

    // a set of map locations with known slanderers
    static Set<MapLocation> confirmedSlanderers = new HashSet<>();

    // a set of map locations of politicians or potentially slanderers
    static Set<MapLocation> enemyPoliticians = new HashSet<>();

    // a set of map locations with neutral enlightment centers
    static Set<MapLocation> neutralEnlightmentCenters = new HashSet<>();

    // a set of map locations with enemy enlightment centers
    static Set<MapLocation> enemyEnlightmentCenters = new HashSet<>();

    // a dictionary mapping a map location to it's passability
    static Map<MapLocation, Double> passabilities = new HashMap<>();


    private Direction bounceOffMapBoundary(Direction currentDirection, Direction wallDirection) {
        switch(currentDirection) {
            case NORTH:
                return Direction.SOUTH;
            case NORTHEAST:
                return Direction.SOUTHEAST;
            case EAST:
                return Direction.WEST;
            case SOUTHEAST:
                return Direction.NORTHEAST;
            case SOUTH:
                return Direction.NORTH;
            case NORTHWEST:
                return Direction.SOUTHWEST;
            
            default:
                return currentDirection;

        }
    }

    public Robot(RobotController robotController) throws GameActionException {
        rc = robotController;
        allyTeam = rc.getTeam();
        enemyTeam = allyTeam.opponent();

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2);
        for (RobotInfo robot: nearbyRobots){
            if (robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER)){
                enlightmentCenterId = robot.getID();
            }
        }
    }

    public abstract void run() throws GameActionException;
    public abstract int getSenseRadiusSquared();

    public Set<MapLocation> getSensedSquares() throws GameActionException {
        Set<MapLocation> newDetectedBots = new HashSet<>();
        Set<MapLocation> newNeutralEnlightmentCenters = new HashSet<>();
        Set<MapLocation> newEnemyEnlightmentCenters = new HashSet<>();
        Set<MapLocation> newEnemyMuckRaker = new HashSet<>();
        Set<MapLocation> newConfirmedSlanderers = new HashSet<>();
        Set<MapLocation> newEnemyPoliticians = new HashSet<>();

        // detect robots
        MapLocation[] curDetectedRobots = rc.detectNearbyRobots();

        for (MapLocation robotLocation: curDetectedRobots){
            newDetectedBots.add(robotLocation);
        }

        // sense robots
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot: robots){
            int id = robot.getID();
            RobotType type = robot.getType();
            Team team = robot.getTeam();
            MapLocation location = robot.getLocation();

            // NOTE: This could create an error if the location is not in detected. This should never happen because all robots that are sensed should have already been detected.
            newDetectedBots.remove(location);

            // if we saw a neutral piece -> neutral Enlightment Center
            if (team.equals(Team.NEUTRAL)){
                newNeutralEnlightmentCenters.add(location);
            }
            // if the robot is an enemy
            else if (!rc.getTeam().equals(team)){
                switch(type){
                    case ENLIGHTENMENT_CENTER: newEnemyEnlightmentCenters.add(location); break;
                    case MUCKRAKER: newEnemyMuckRaker.add(location); break;
                    case SLANDERER: newConfirmedSlanderers.add(location); break;
                    case POLITICIAN: newEnemyPoliticians.add(location); break;
                }

            }
        }
        detectedBots.addAll(newDetectedBots);
        neutralEnlightmentCenters.addAll(newNeutralEnlightmentCenters);
        enemyEnlightmentCenters.addAll(newEnemyEnlightmentCenters);
        enemyMuckRaker.addAll(newEnemyMuckRaker);
        confirmedSlanderers.addAll(newConfirmedSlanderers);
        enemyPoliticians.addAll(newEnemyPoliticians);

        // sense passabilities
        MapLocation curLocation = rc.getLocation();
        int radiusSquared = getSenseRadiusSquared();
        Set<MapLocation> sensedSquares = new HashSet<>();
        curLocation.translate(-radiusSquared, -radiusSquared);
        /**
        for (int x = 0; x <= 2*radiusSquared; x++){
            for (int y = 0; y <= 2*radiusSquared; y++){
                // Check all sets of robots to erase any information that is no longer valid
                if (detectedBots.contains(curLocation) && !newDetectedBots.contains(curLocation)) detectedBots.remove(curLocation);
                if (neutralEnlightmentCenters.contains(curLocation) && !newNeutralEnlightmentCenters.contains(curLocation)) neutralEnlightmentCenters.remove(curLocation);
                if (enemyEnlightmentCenters.contains(curLocation) && !newEnemyEnlightmentCenters.contains(curLocation)) enemyEnlightmentCenters.remove(curLocation);
                if (enemyMuckRaker.contains(curLocation) && !newEnemyMuckRaker.contains(curLocation)) enemyMuckRaker.remove(curLocation);
                if (confirmedSlanderers.contains(curLocation) && !newConfirmedSlanderers.contains(curLocation)) confirmedSlanderers.remove(curLocation);
                if (enemyPoliticians.contains(curLocation) && !newEnemyPoliticians.contains(curLocation)) enemyPoliticians.remove(curLocation);

                // if passability has not been sensed, add that value to the map
                if (rc.canSenseLocation(curLocation)){
                    if (!passabilities.containsKey(curLocation)) {
                        double passability = rc.sensePassability(curLocation);
                        passabilities.put(curLocation, passability);
                    }
                }

                // check to see if spaces we though robots were in are no longer there

                curLocation.translate(1,0);
            }
            curLocation.translate(-2*radiusSquared, 1);
        }
         **/

        return sensedSquares;
    }
}
