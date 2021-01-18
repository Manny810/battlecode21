package basicplayer;

import battlecode.common.*;

import java.util.HashMap;
import java.util.Map;

public abstract class Robot {

    RobotController rc;
    Team allyTeam;
    Integer enlightmentCenterId;

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

    }

    public abstract void run() throws GameActionException;
}
