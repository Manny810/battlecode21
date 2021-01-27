package betterplayer;
import battlecode.common.*;
import com.sun.tools.javac.util.List;

import java.util.ArrayList;
import java.util.Objects;

public class Muckraker extends Robot {

    static final int MUCKRAKER_SENSOR_RADIUS_SQUARED = 30;
    Direction spawnDirection;
    Direction muckrakerDirection;


    public Muckraker(RobotController rc) throws GameActionException {
        super(rc);
    }


    private void exploreRadiallyOutward() throws GameActionException {
        // Get the direction that it was spawned in
        int currentID = rc.getID();
        int originEnlightenmentCenterID = enlightmentCenterId;
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2);
        MapLocation originECLocation = null;
        for (RobotInfo robot : nearbyRobots) {
            if (robot.ID == originEnlightenmentCenterID) {
                originECLocation = robot.location;
                spawnDirection = rc.getLocation().directionTo(originECLocation).opposite();
                muckrakerDirection = spawnDirection;
                System.out.println("My initial starting direction is in this direction : " + spawnDirection);
                break;
            }
        }

        while (true) {
            senseSquares();

            if (rc.isReady()) {
                Team enemy = rc.getTeam().opponent();
                int actionRadius = rc.getType().actionRadiusSquared;
                RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
                for (RobotInfo attackableRobot : attackable) {
                    if (rc.canExpose(attackableRobot.ID)) {
                        rc.expose(attackableRobot.ID);
                    }
                    if (attackableRobot.type == RobotType.ENLIGHTENMENT_CENTER && attackableRobot.team == allyTeam) {
                        MapLocation ecLocation = attackableRobot.location;
                        Direction oppositeDirection = rc.getLocation().directionTo(ecLocation).opposite();
                        muckrakerDirection = spawnDirection;
                    }
                }
                //System.out.println(rc.onTheMap(rc.getLocation().add(muckrakerDirection)));
                if (rc.onTheMap(rc.getLocation().add(muckrakerDirection))) { // Location is on the map
                    //System.out.println("It's on the map bro");
                    if (rc.canMove(muckrakerDirection)) { // Can move in designated directoin
                        //System.out.println("Can move");
                        rc.move(muckrakerDirection);
                        rc.detectNearbyRobots();
                        RobotInfo[] sensedRobots = rc.senseNearbyRobots();
                        for (RobotInfo robot : sensedRobots) {
                            if (robot.team == Team.NEUTRAL) {
                                MapLocation neutralECLocation = robot.location;
                                // add neutralECLocation to some dictionary
                            } else if (robot.team == allyTeam.opponent() && robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                                MapLocation enemyECLocation = robot.location;
                            }
                        }
                    } else if (rc.isLocationOccupied(rc.getLocation().add(muckrakerDirection))) { // Location is being occupied so can't move
                        //System.out.println("Can it move past a unit " + rc.isLocationOccupied(rc.getLocation().add(muckrakerDirection).add(muckrakerDirection)));
                        //System.out.println("The location " + rc.getLocation().add(muckrakerDirection).add(muckrakerDirection));
                        for (int i = 0; i < 8; i++) {
                            int newOrdinalForNextDirection = (rc.getID() % 2 == 0) ? muckrakerDirection.ordinal() - 1 : muckrakerDirection.ordinal() + 1;
                            int ordinalDirectionModded;
                            if (newOrdinalForNextDirection < 0) {
                                ordinalDirectionModded = RobotPlayer.directions.length + newOrdinalForNextDirection;
                            } else {
                                ordinalDirectionModded = newOrdinalForNextDirection % 8;
                            }
                            Direction newDirection = RobotPlayer.directions[ordinalDirectionModded];
                            if (rc.canMove(newDirection)) {
                                rc.move(newDirection);
                                muckrakerDirection = newDirection;
                                break;
                            }
                        }
                    }
                } else { // Location not on the map
                        //System.out.println("Its not on the map");
                        ArrayList<Integer> blockedDirectionsOrdinals = new ArrayList<>();
                        int blockedDirCounter = 0;
                        for (Direction dir : RobotPlayer.cardinalDirections) {
                            if (!(rc.onTheMap(rc.getLocation().add(dir)))) {
                                blockedDirectionsOrdinals.add(dir.ordinal());
                            }
                        }
                        //System.out.println("BLOCKED DIRECTIONS NUMBERS" + blockedDirectionsOrdinals);
                        Direction[] blockedDirections = new Direction[blockedDirectionsOrdinals.size()];
                        for (int i = 0; i < blockedDirectionsOrdinals.size(); i++) {
                            blockedDirections[i] = RobotPlayer.directions[blockedDirectionsOrdinals.get(i)];
                        }
                        Direction blockingWallDirection = findWallDirection(blockedDirections);
                        Direction newDirection = bounceOffMapBoundary(muckrakerDirection, blockingWallDirection);
                        muckrakerDirection = newDirection;
                        //System.out.println("New direction " + muckrakerDirection);

                        if (rc.canMove(muckrakerDirection)) {
                            rc.move(muckrakerDirection);
                        }
                    }
            } else {
                //System.out.println("Not ready");
                Clock.yield();
            }



        }
    }


    @Override
    public void run() throws GameActionException {
        exploreRadiallyOutward();
    }

    @Override
    public int getSenseRadiusSquared() {
        return MUCKRAKER_SENSOR_RADIUS_SQUARED;
    }
}
