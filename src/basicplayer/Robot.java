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


    public Direction findWallDirection(Direction[] blockedDirections) {
        if (blockedDirections.length == 1) { // Unit is bouncing off a cardinal direction
            return blockedDirections[0];
        } else  { // blockedDirections.length == 2 --> Unit is in a corner, where the wall is the direction in between the two blocked directions
            if (blockedDirections[0] == Direction.NORTH && blockedDirections[1] == Direction.WEST) { // Edge case with North and West --> return Northwest
                return RobotPlayer.directions[(blockedDirections[1].ordinal()+1)%8];
            } else {
                return RobotPlayer.directions[(blockedDirections[0].ordinal() + 1) % 8];
            }
        }
    }

    public Direction bounceOffMapBoundary(Direction attemptedDirection, Direction wallDirection) {
        switch(attemptedDirection) {
            case NORTH:
                switch (wallDirection) {
                    case NORTH: case NORTHWEST:
                        return Direction.EAST;
                    case NORTHEAST:
                        return Direction.SOUTH;
                }
            case NORTHEAST:
                switch (wallDirection) {
                    case NORTH: case NORTHWEST:
                        return Direction.SOUTHEAST;
                    case NORTHEAST:
                        return Direction.SOUTH;
                }
            case NORTHWEST:
                switch (wallDirection) {
                    case NORTH: case NORTHEAST:
                        return Direction.SOUTHWEST;
                    case NORTHWEST:
                        return Direction.EAST;
                }
            case SOUTH:
                switch (wallDirection) {
                    case SOUTH: case SOUTHEAST:
                        return Direction.WEST;
                    case SOUTHWEST:
                        return Direction.NORTH;
                }
            case SOUTHWEST:
                switch (wallDirection) {
                    case SOUTH: case SOUTHEAST:
                        return Direction.NORTHWEST;
                    case SOUTHWEST:
                        return Direction.NORTH;
                }
            case SOUTHEAST:
                switch (wallDirection) {
                    case SOUTH: case SOUTHWEST:
                        return Direction.NORTHEAST;
                    case SOUTHEAST:
                        return Direction.NORTH;
                }
            case EAST:
                switch (wallDirection) {
                    case EAST: case NORTHEAST:
                        return Direction.SOUTH;
                    case SOUTHEAST:
                        return Direction.WEST;
                }
            case WEST:
                switch (wallDirection) {
                    case WEST: case SOUTHWEST:
                        return Direction.NORTH;
                    case NORTHWEST:
                        return Direction.EAST;
                }
            default:
                return Direction.CENTER;
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
