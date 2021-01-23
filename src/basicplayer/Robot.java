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


    static final int NO_FLAG_CODE = 0;
    static final int NEUTRAL_EC_FLAG_CODE = 1;
    static final int ENEMY_EC_FLAG_CODE = 2;
    static final int ENEMY_POLITICIAN_FLAG_CODE = 3;
    static final int ENEMY_SLANDERER_FLAG_CODE = 4;
    static final int ENEMY_MUCKRAKER_FLAG_CODE = 5;
    

    // a set of MapLocations with detected but unknown bots
    static Set<MapLocation> detectedBots = new HashSet<>();

    // a set of map locations with known muckrakers
    static Set<RobotInfo> enemyMuckRaker = new HashSet<>();

    // a set of map locations with known slanderers
    static Set<RobotInfo> confirmedSlanderers = new HashSet<>();

    // a set of map locations of politicians or potentially slanderers
    static Set<RobotInfo> enemyPoliticians = new HashSet<>();

    // a set of map locations with neutral enlightment centers
    static Set<RobotInfo> neutralEnlightmentCenters = new HashSet<>();

    // a set of map locations with enemy enlightment centers
    static Set<RobotInfo> enemyEnlightmentCenters = new HashSet<>();


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

    public Direction[] bounceOffMapBoundary(Direction attemptedDirection, Direction wallDirection) {
        switch(attemptedDirection) {
            case NORTH:
                switch (wallDirection) {
                    case NORTH:
                        return new Direction[]{Direction.WEST, Direction.EAST};
                    case NORTHWEST:
                        return new Direction[]{Direction.EAST};
                    case NORTHEAST:
                        return new Direction[]{Direction.SOUTH};
                }
            case NORTHEAST:
                switch (wallDirection) {
                    case NORTH: case NORTHWEST:
                        return new Direction[]{Direction.SOUTHEAST};
                    case NORTHEAST:
                        return new Direction[]{Direction.SOUTH};
                }
            case NORTHWEST:
                switch (wallDirection) {
                    case NORTH: case NORTHEAST:
                        return new Direction[]{Direction.SOUTHWEST};
                    case NORTHWEST:
                        return new Direction[]{Direction.SOUTH};
                }
            case SOUTH:
                switch (wallDirection) {
                    case SOUTH:
                        return new Direction[]{Direction.WEST, Direction.EAST};
                    case SOUTHEAST:
                        return new Direction[]{Direction.WEST};
                    case SOUTHWEST:
                        return new Direction[]{Direction.NORTH};
                }
            case SOUTHWEST:
                switch (wallDirection) {
                    case SOUTH: case SOUTHEAST:
                        return new Direction[]{Direction.NORTHWEST};
                    case SOUTHWEST:
                        return new Direction[]{Direction.NORTH};
                }
            case SOUTHEAST:
                switch (wallDirection) {
                    case SOUTH: case SOUTHWEST:
                        return new Direction[]{Direction.NORTHEAST};
                    case SOUTHEAST:
                        return new Direction[]{Direction.NORTH};
                }
            case EAST:
                switch (wallDirection) {
                    case EAST:
                        return new Direction[]{Direction.NORTH, Direction.SOUTH};
                    case SOUTHEAST:
                        return new Direction[]{Direction.WEST};
                    case NORTHEAST:
                        return new Direction[]{Direction.SOUTH};
                }
            case WEST:
                switch (wallDirection) {
                    case WEST:
                        return new Direction[]{Direction.NORTH, Direction.SOUTH};
                    case SOUTHWEST:
                        return new Direction[]{Direction.NORTH};
                    case NORTHWEST:
                        return new Direction[]{Direction.EAST};
                }
            default:
                return new Direction[]{};

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

    public void senseSquares() throws GameActionException {
        // detect robots
        MapLocation[] curDetectedRobots = rc.detectNearbyRobots();

        for (MapLocation robotLocation: curDetectedRobots){
            detectedBots.add(robotLocation);
        }

        // sense robots
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot: robots){
            int id = robot.getID();
            RobotType type = robot.getType();
            Team team = robot.getTeam();
            MapLocation location = robot.getLocation();

            // NOTE: This could create an error if the location is not in detected. This should never happen because all robots that are sensed should have already been detected.
            detectedBots.remove(location);

            // if we saw a neutral piece -> neutral Enlightment Center
            if (team.equals(Team.NEUTRAL)){
                neutralEnlightmentCenters.add(robot);
            }
            // if the robot is an enemy
            else if (!rc.getTeam().equals(team)){
                switch(type){
                    case ENLIGHTENMENT_CENTER: enemyEnlightmentCenters.add(robot); break;
                    case MUCKRAKER: enemyMuckRaker.add(robot); break;
                    case SLANDERER: confirmedSlanderers.add(robot); break;
                    case POLITICIAN: enemyPoliticians.add(robot); break;
                }

            }
        }

        if (neutralEnlightmentCenters.size() != 0){
            for (RobotInfo robot: neutralEnlightmentCenters){
                flagRobot(robot);
                neutralEnlightmentCenters.remove(robot);
                break;
            }
        } else if (enemyEnlightmentCenters.size() != 0){
            for (RobotInfo robot: enemyEnlightmentCenters){
                flagRobot(robot);
                enemyEnlightmentCenters.remove(robot);
                break;
            }
        } else if (enemyPoliticians.size() != 0){
            for (RobotInfo robot: enemyPoliticians){
                flagRobot(robot);
                enemyPoliticians.remove(robot);
                break;
            }
        } else if (confirmedSlanderers.size() != 0){
            for (RobotInfo robot: confirmedSlanderers){
                flagRobot(robot);
                confirmedSlanderers.remove(robot);
                break;
            }
        } else if (enemyMuckRaker.size() != 0){
            for (RobotInfo robot: enemyMuckRaker){
                flagRobot(robot);
                enemyMuckRaker.remove(robot);
                break;
            }
        } else {
            rc.setFlag(0);
        }

    }

    public void flagRobot(RobotInfo robot) throws GameActionException {
        MapLocation location = robot.getLocation();
        RobotType type = robot.getType();
        Team team = robot.getTeam();
        int flag = locationToFlag(location) + 128 * 128 * typeToFlag(type, team);
        if (rc.canSetFlag(flag)) {
            rc.setFlag(flag);
        }
    }

    public int typeToFlag(RobotType type, Team team){
        if (team.equals(Team.NEUTRAL)){
            return NEUTRAL_EC_FLAG_CODE;
        }
        switch (type) {
            case ENLIGHTENMENT_CENTER:
                return ENEMY_EC_FLAG_CODE;
            case POLITICIAN:
                return ENEMY_POLITICIAN_FLAG_CODE;
            case SLANDERER:
                return ENEMY_SLANDERER_FLAG_CODE;
            case MUCKRAKER:
                return ENEMY_MUCKRAKER_FLAG_CODE;
        }

        return 0;
    }

    public int locationToFlag(MapLocation location){
        int x = location.x;
        int y = location.y;
        int encodedLocation = (x%128) * 128 + (y%128);
        return encodedLocation;
    }

    public MapLocation getLocationFromFlag(int flag){
        int y = flag % 128;
        int x = (flag / 128) % 128;
        int extraInformation = flag / 128 / 128;

        MapLocation currentLocation = rc.getLocation();
        int offsetX = currentLocation.x / 128;
        int offsetY = currentLocation.y / 128;
        MapLocation actualLocation = new MapLocation(offsetX * 128 + x, offsetY * 128 + y);

        MapLocation alternative = actualLocation.translate(-128, 0);
        if(rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(128, 0);
        if(rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }

        alternative = actualLocation.translate(0, -128);
        if(rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }

        alternative = actualLocation.translate(0, 128);
        if(rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }

        return actualLocation;
    }



    public MapLocation getCommandFromEC() throws GameActionException {
        int flag = rc.getFlag(enlightmentCenterId);
        if ((flag / 2^23) == 0){
            return null;
        } else {
            MapLocation location = getLocationFromFlag(flag);
            int extraInformation = getExtraInfoFromFlag(flag);

            int idHash = extraInformation % 256;
            if (extraInformation / 256 == 0){
                return location;
            } else {
                if (rc.getID()/256 == idHash){
                    return location;
                }
                else{
                    return null;
                }
            }
        }
    }

    public Integer getExtraInfoFromFlag(int flag) {
        return flag / 128 / 128;
    }


}
