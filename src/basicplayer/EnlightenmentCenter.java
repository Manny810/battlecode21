package basicplayer;
import battlecode.common.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class EnlightenmentCenter extends Robot {

    static final int ENLIGHTMENT_CENTER_SENSOR_RADIUS_SQUARED = 40;

    static final Direction[] directions = RobotPlayer.directions;
    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static final double POLITICIAN_RATIO = 3.0;
    static final double SLANDERER_RATIO = 1.0;
    static final double MUCKRAKER_RATIO = 6.0;
    static final double TOTAL_RATIO = POLITICIAN_RATIO + SLANDERER_RATIO + MUCKRAKER_RATIO + 1.0;

    static final int SLANDERER_INFLUENCE = 100;
    static final int POLITICIAN_INFLUENCE = 50;
    static final int MUCKRAKER_INFLUENCE = 1;

    int slandererCount = 0;
    int politicianCount = 0;
    int muckrakerCount = 0;

    Set<MapLocation> neutralECLocations = new HashSet<>();
    Set<MapLocation> enemyECLocations = new HashSet<>();
    Set<MapLocation> enemyMuckrakerLocations = new HashSet<>();
    Set<MapLocation> enemySlandererLocations = new HashSet<>();
    Set<MapLocation> enemyPoliticianLocations = new HashSet<>();

    Set<Integer> slandererIds = new HashSet<>();
    Set<Integer> politicianIds = new HashSet<>();
    Set<Integer> muckrakerIds = new HashSet<>();

    Set<Integer> freePoliticians = new HashSet<>();

    Map<MapLocation, Integer> assignedPerson = new HashMap<>();
    Map<Integer, MapLocation> assignedLocation = new HashMap<>();

    static int counter = 0;
    boolean start;

    public EnlightenmentCenter(RobotController rc) throws GameActionException {
        super(rc);
        start = true;
    }

    @Override
    public int getSenseRadiusSquared() {
        return ENLIGHTMENT_CENTER_SENSOR_RADIUS_SQUARED;
    }

    @Override
    public void run() throws GameActionException {
//        getSensedSquares();


        double total = slandererCount + politicianCount + muckrakerCount + 1.0;

        RobotType toBuild;
        int influence;
        if (slandererCount/total <= SLANDERER_RATIO/TOTAL_RATIO){
            toBuild = RobotType.SLANDERER;
            influence = SLANDERER_INFLUENCE;
        } else if (muckrakerCount/total <= MUCKRAKER_RATIO/TOTAL_RATIO){
            toBuild = RobotType.MUCKRAKER;
            influence = MUCKRAKER_INFLUENCE;
        } else {
            toBuild = RobotType.POLITICIAN;
            influence = POLITICIAN_INFLUENCE;
        }

        for (Direction dir: directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                rc.buildRobot(toBuild, dir, influence);
                MapLocation newRobot = rc.getLocation().add(dir);
                RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2);
                int newRobotId = -1;
                for (RobotInfo robot: nearbyRobots){
                    if (robot.getLocation().equals(newRobot)){
                        newRobotId = robot.getID();
                    }
                }
                if (slandererCount / total < SLANDERER_RATIO / TOTAL_RATIO) {
                    slandererCount++;
                    slandererIds.add(newRobotId);
                } else if (muckrakerCount / total < MUCKRAKER_RATIO / TOTAL_RATIO) {
                    muckrakerCount++;
                    muckrakerIds.add(newRobotId);
                } else {
                    politicianCount++;
                    freePoliticians.add(newRobotId);
                    politicianIds.add(newRobotId);
                }
            }
        }
        for (int id : muckrakerIds){
            if (rc.canGetFlag(id)){
                readRobots(id);
            } else {
                muckrakerIds.remove(id);
            }
        }
        for (int id : politicianIds){
            if (rc.canGetFlag(id)){
                readRobots(id);
            } else {
                politicianIds.remove(id);
                MapLocation location = assignedLocation.get(id);
                assignedLocation.remove(id);
                assignedPerson.remove(location);
            }
        }
        setECFlag(); 

////      Testing out diagonal directions
//        RobotType toBuild = RobotType.SLANDERER;
//        int influence = 100;
//
//        for (Direction dir: directions) {
//            if (rc.canBuildRobot(toBuild, directions[(dir.ordinal()+1)%8], influence)) {
//                rc.buildRobot(toBuild, directions[(dir.ordinal()+1)%8], influence);
//            }
//            Clock.yield();
//            Clock.yield();
//            Clock.yield();
//        }

    }

    private void setECFlag() throws GameActionException {
        if (neutralECLocations.size() != 0){
            for (MapLocation location: neutralECLocations){
                if (!assignedPerson.containsKey(location)){
                    for (Integer id: freePoliticians){
                        assignedPerson.put(location, id);
                        assignedLocation.put(id, location);

                        int flag = 0;
                        flag += locationToFlag(location); // location
                        flag += (id % 256) * 128 * 128; // id of politician to move
                        flag += 1 * 128 * 128 * 256; // specialized command
                        flag += 1 * 128 * 128 * 256 * 2; // is a command

                        rc.setFlag(flag);

                        break;
                    }
                    break;
                }
            }
        } else if (enemyECLocations.size() != 0){
            for (MapLocation location: enemyECLocations){
                if (!assignedPerson.containsKey(location)){
                    for (Integer id: freePoliticians){
                        assignedPerson.put(location, id);
                        assignedLocation.put(id, location);

                        int flag = 0;
                        flag += locationToFlag(location); // location
                        flag += (id % 256) * 128 * 128; // id of politician to move
                        flag += 1 * 128 * 128 * 256; // specialized command
                        flag += 1 * 128 * 128 * 256 * 2; // is a command

                        rc.setFlag(flag);
                        break;
                    }
                    break;
                }
            }
        } else {
            rc.setFlag(0);
        }
    }

    private void readRobots(int id) throws GameActionException {
        int flag = rc.getFlag(id);
        MapLocation location = getLocationFromFlag(flag);
        int extraInfo = getExtraInfoFromFlag(flag);

        if (extraInfo == 1){
            neutralECLocations.add(location);
        } else if (extraInfo == 2) {
            enemyECLocations.add(location);
        } else if (extraInfo == 3) {
            enemyPoliticianLocations.add(location);
        } else if (extraInfo == 4) {
            enemySlandererLocations.add(location);
        } else if (extraInfo == 5) {
            enemyMuckrakerLocations.add(location);
        }
    }

}
