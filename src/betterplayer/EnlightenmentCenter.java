package betterplayer;
import battlecode.common.*;

import java.util.*;


public class EnlightenmentCenter extends Robot {

    static final int ENLIGHTMENT_CENTER_SENSOR_RADIUS_SQUARED = 40;
    boolean earlyGame = true;


    static final Direction[] directions = RobotPlayer.directions;
    final Set<Direction> playableDirections = new HashSet<>(Arrays.asList(directions));
    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static final double POLITICIAN_RATIO = 4.0;
    static final double SLANDERER_RATIO = 3.0;
    static final double MUCKRAKER_RATIO = 3.0;
    static final double TOTAL_RATIO = POLITICIAN_RATIO + SLANDERER_RATIO + MUCKRAKER_RATIO + 1.0;

    static final int SLANDERER_INFLUENCE = 85;
    static final int POLITICIAN_INFLUENCE = 50;
    static final int MUCKRAKER_INFLUENCE = 1;

    int slandererCount = 0;
    int politicianCount = 0;
    int muckrakerCount = 0;


    Set<MapLocation> neutralECLocations = new HashSet<>();
    Set<MapLocation> enemyECLocations = new HashSet<>();
    Set<MapLocation> teamECLocations = new HashSet<>();
    Set<MapLocation> enemyMuckrakerLocations = new HashSet<>();
    Set<MapLocation> enemySlandererLocations = new HashSet<>();
    Set<MapLocation> enemyPoliticianLocations = new HashSet<>();

    Set<Integer> slandererIds = new HashSet<>();
    Set<Integer> politicianIds = new HashSet<>();
    Set<Integer> muckrakerIds = new HashSet<>();

    Set<Integer> freePoliticians = new HashSet<>();

    MapLocation target = null;
    int selectedPolitician;

    Map<MapLocation, Set<Integer>> assignedPerson = new HashMap<>();
    Map<Integer, MapLocation> assignedLocation = new HashMap<>();
    Map<MapLocation, Integer> influenceMap = new HashMap<>();

    Map<Integer, MapLocation> spawnLocationPassabilities = new HashMap<>();

    static int counter = 0;
    boolean start;

    public EnlightenmentCenter(RobotController rc) throws GameActionException {
        super(rc);
        start = true;
        selectedPolitician = -1;
    }


    private void runEarlyGameStrat() throws GameActionException {
        RobotType toBuild = RobotType.SLANDERER;
        int influence = SLANDERER_INFLUENCE;
        int initialSlandererCount = 0;

        while (initialSlandererCount < 2) {
            if (rc.isReady()) {
                for (Direction dir : RobotPlayer.ordinalDirections) { // Build one slanderer in the beginning
                    if (rc.canBuildRobot(toBuild, dir, influence)) {
                        rc.buildRobot(toBuild, dir, influence);
                        MapLocation newRobot = rc.getLocation().add(dir);
                        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2);
                        int newRobotId = -1;
                        for (RobotInfo robot : nearbyRobots) {
                            if (robot.getLocation().equals(newRobot)) {
                                newRobotId = robot.getID();
                            }
                        }
                        slandererIds.add(newRobotId);
                        slandererCount++;
                        break;
                    }
                }
                initialSlandererCount++;
                influence = 63;
            }
        }

        toBuild = RobotType.MUCKRAKER;
        influence = MUCKRAKER_INFLUENCE;
        int initialMuckrakerCount = 0;

        while (initialMuckrakerCount < 8) {
            senseEC();
            System.out.println("Make muckrakers");
            int assigned = setECFlag();
            if (assigned != -1){
                freePoliticians.remove(assigned);
            }
            if (rc.isReady()) {
                Direction dir = RobotPlayer.directions[initialMuckrakerCount];
                if (rc.canBuildRobot(toBuild, dir, influence)) {
                    rc.buildRobot(toBuild, dir, influence);
                    MapLocation newRobot = rc.getLocation().add(dir);
                    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2);
                    int newRobotId = -1;
                    for (RobotInfo robot : nearbyRobots) {
                        if (robot.getLocation().equals(newRobot)) {
                            newRobotId = robot.getID();
                        }
                    }
                    muckrakerCount++;
                    muckrakerIds.add(newRobotId);
                }
                initialMuckrakerCount++;
            } else {
                Clock.yield();
            }
        }


        senseEC();
        earlyGame = false;
    }

    @Override
    public int getSenseRadiusSquared() {
        return ENLIGHTMENT_CENTER_SENSOR_RADIUS_SQUARED;
    }

    @Override
    public void run() throws GameActionException {
//        getSensedSquares();

        if (earlyGame == true) {
            runEarlyGameStrat();
        } else {

            int assigned = setECFlag();
            if (assigned != -1) {
                freePoliticians.remove(assigned);
            }

            int bidAmount = (int) (slandererCount * .25);
            if (rc.canBid(bidAmount)) {
                System.out.println("Bidding " + bidAmount);
                rc.bid(bidAmount);
            }
//            int id = this.rc.getID();
            double total = slandererCount + politicianCount + muckrakerCount + 1.0;
            System.out.println("#Slanderers : " + slandererCount);
            System.out.println("#Politicians : " + politicianCount);
            System.out.println("#muckraker : " + muckrakerCount);
            System.out.println("Total : " + total);

            RobotType toBuild;
            int influence;
            boolean buildPolitician = (target != null);
            int robotInfluence = 0;
            if (buildPolitician){
                robotInfluence = Math.max(influenceMap.get(target), 150);
            }
            boolean first = false;
            if (buildPolitician && robotInfluence < rc.getInfluence()){
                toBuild = RobotType.POLITICIAN;
                influence = robotInfluence;
                first = true;
            }
            else if (slandererCount / total <= SLANDERER_RATIO / TOTAL_RATIO) {
                toBuild = RobotType.SLANDERER;
                influence = SLANDERER_INFLUENCE;
            } else if (muckrakerCount / total <= MUCKRAKER_RATIO / TOTAL_RATIO){
                toBuild = RobotType.MUCKRAKER;
                influence = MUCKRAKER_INFLUENCE;
            } else {
                toBuild = RobotType.POLITICIAN;
                influence = POLITICIAN_INFLUENCE;
            }

            for (Direction dir : playableDirections) {
                if (rc.canBuildRobot(toBuild, dir, influence)) {
                    rc.buildRobot(toBuild, dir, influence);
                    MapLocation newRobot = rc.getLocation().add(dir);
                    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2);
                    int newRobotId = -1;
                    for (RobotInfo robot : nearbyRobots) {
                        if (robot.getLocation().equals(newRobot)) {
                            newRobotId = robot.getID();
                        }
                    }
                    if (first){
                        politicianCount++;
                        freePoliticians.add(newRobotId);
                        politicianIds.add(newRobotId);
                        selectedPolitician = newRobotId;
                    }
                    else if (slandererCount / total <= SLANDERER_RATIO / TOTAL_RATIO) {
                        slandererCount++;
                        slandererIds.add(newRobotId);
                    } else if(muckrakerCount / total <= MUCKRAKER_RATIO / TOTAL_RATIO) {
                        muckrakerCount++;
                        muckrakerIds.add(newRobotId);
                    } else {
                        politicianCount++;
                        freePoliticians.add(newRobotId);
                        politicianIds.add(newRobotId);
                    }
                }
            }
            Set<Integer> remove = new HashSet<>();

            for (int id : muckrakerIds) {
                if (rc.canGetFlag(id)) {
                    readRobots(id);
                } else {
                    System.out.println("Couldn't read " + id);
                    remove.add(id);
                }
            }
            muckrakerIds.removeAll(remove);

            for (int id : politicianIds) {
                if (rc.canGetFlag(id)) {
                    readRobots(id);
                } else {
                    remove.add(id);
                    MapLocation location = assignedLocation.get(id);
                    assignedLocation.remove(id);
                    removePerson(location, id);
                }
            }
            politicianIds.removeAll(remove);

        }

    }

    private void senseEC() throws GameActionException {
        int senseRadius = (int) Math.floor(Math.pow(getSenseRadiusSquared(), .5));
        MapLocation north = rc.getLocation().translate(0,senseRadius);
        if (!rc.onTheMap(north)){
            playableDirections.remove(Direction.NORTH);
            playableDirections.remove(Direction.NORTHWEST);
            playableDirections.remove(Direction.NORTHEAST);
        }

        MapLocation east = rc.getLocation().translate(senseRadius,0);
        if (!rc.onTheMap(east)){
            playableDirections.remove(Direction.SOUTHEAST);
            playableDirections.remove(Direction.EAST);
            playableDirections.remove(Direction.NORTHEAST);
        }

        MapLocation south = rc.getLocation().translate(0,-senseRadius);
        if (!rc.onTheMap(south)){
            playableDirections.remove(Direction.SOUTHEAST);
            playableDirections.remove(Direction.SOUTH);
            playableDirections.remove(Direction.SOUTHWEST);
        }

        MapLocation west = rc.getLocation().translate(-senseRadius,0);
        if (!rc.onTheMap(north)){
            playableDirections.remove(Direction.WEST);
            playableDirections.remove(Direction.NORTHWEST);
            playableDirections.remove(Direction.SOUTHWEST);
        }
        System.out.println("Playable Directions: " + playableDirections.toString());
    }

    private int setECFlag() throws GameActionException {
        if (freePoliticians.size() == 0){
            System.out.println("No more politicians");
        }

        if (selectedPolitician != -1){
            assignPerson(target, selectedPolitician);
            assignedLocation.put(selectedPolitician, target);
            int flag = 0;
            flag += locationToFlag(target); // location
            flag += (selectedPolitician % 256) * 128 * 128; // id of politician to move
            flag += 1 * 128 * 128 * 256; // specialized command
            flag += 1 * 128 * 128 * 256 * 2; // is a command

            rc.setFlag(flag);
            System.out.println(assignedPerson);
            System.out.println("Location: " + target.toString());
            System.out.println("My Flag" + flag);

            int ret = selectedPolitician;
            selectedPolitician = -1;
            target = null;
            return ret;

        }

        if (target != null){
            if (freePoliticians.size() != 0) {
                for (int id : freePoliticians) {


                    assignPerson(target, id);

                    assignedLocation.put(id, target);

                    int flag = 0;
                    flag += locationToFlag(target); // location
                    flag += (id % 256) * 128 * 128; // id of politician to move
                    flag += 1 * 128 * 128 * 256; // specialized command
                    flag += 1 * 128 * 128 * 256 * 2; // is a command

                    rc.setFlag(flag);
                    System.out.println(assignedPerson);
                    System.out.println("Location: " + target.toString());
                    System.out.println("My Flag" + flag);

                    return id;


                }
            }
        }


        if (neutralECLocations.size() != 0){
            for (MapLocation location: neutralECLocations){
                if (assignedPerson.get(location).size() == 0){
                    for (Integer id: freePoliticians){
                        assignPerson(location, id);
                        assignedLocation.put(id, location);
                        target = location;
                        int flag = 0;
                        flag += locationToFlag(location); // location
                        flag += (id % 256) * 128 * 128; // id of politician to move
                        flag += 1 * 128 * 128 * 256; // specialized command
                        flag += 1 * 128 * 128 * 256 * 2; // is a command

                        rc.setFlag(flag);
                        System.out.println(assignedPerson);
                        System.out.println("Location: " + location.toString());
                        System.out.println("My Flag" + flag);

                        return id;

                    }

                }

            }
        }

        if (enemyECLocations.size() != 0){
            for (MapLocation location: enemyECLocations){
                if (assignedPerson.get(location).size() == 0){
                    for (Integer id: freePoliticians){

                        target = location;
                        assignPerson(location, id);
                        assignedLocation.put(id, location);

                        int flag = 0;
                        flag += locationToFlag(location); // location
                        flag += (id % 256) * 128 * 128; // id of politician to move
                        flag += 1 * 128 * 128 * 256; // specialized command
                        flag += 1 * 128 * 128 * 256 * 2; // is a command

                        rc.setFlag(flag);

                        System.out.println(assignedPerson);
                        System.out.println("Location: " + location.toString());
                        System.out.println("My Flag" + flag);
                        return id;
                    }

                }

            }
        }

        if (freePoliticians.size() != 0) {
            for (int id : freePoliticians) {
                if (neutralECLocations.size() != 0) {
                    for (MapLocation location : neutralECLocations) {

                        assignPerson(location, id);

                        assignedLocation.put(id, location);

                        int flag = 0;
                        flag += locationToFlag(location); // location
                        flag += (id % 256) * 128 * 128; // id of politician to move
                        flag += 1 * 128 * 128 * 256; // specialized command
                        flag += 1 * 128 * 128 * 256 * 2; // is a command

                        rc.setFlag(flag);
                        System.out.println(assignedPerson);
                        System.out.println("Location: " + location.toString());
                        System.out.println("My Flag" + flag);

                        return id;
                    }
                }
                if (enemyECLocations.size() != 0) {
                    for (MapLocation location : enemyECLocations) {

                        assignPerson(location, id);
                        assignedLocation.put(id, location);

                        int flag = 0;
                        flag += locationToFlag(location); // location
                        flag += (id % 256) * 128 * 128; // id of politician to move
                        flag += 1 * 128 * 128 * 256; // specialized command
                        flag += 1 * 128 * 128 * 256 * 2; // is a command

                        rc.setFlag(flag);
                        System.out.println(assignedPerson);
                        System.out.println("Location: " + location.toString());
                        System.out.println("My Flag" + flag);

                        return id;
                    }
                }

            }
        }

        System.out.println("Not setting flag to anything");
        System.out.println("enemyECLocations size: " + enemyECLocations.size());
        System.out.println("neutral EC Locations size: " + neutralECLocations.size());
        System.out.println(assignedPerson);
        rc.setFlag(0);
        return -1;
    }

    private void assignPerson(MapLocation location, int id) {
        Set<Integer> newSet = assignedPerson.get(location);
        newSet.add(id);
        assignedPerson.put(location, newSet);
    }

    private void removePerson(MapLocation location, int id) {
        System.out.println("Removed " + id + "From there post");
        if (assignedPerson.containsKey(location)) {
            Set<Integer> newSet = assignedPerson.get(location);
            newSet.remove(id);
            assignedPerson.put(location, newSet);
        }
    }

    private void readRobots(int id) throws GameActionException {
        int flag = rc.getFlag(id);
        MapLocation location = getLocationFromFlag(flag);
        int type = getTypeFromFlag(flag);
        int robotInfluence = getInfluenceFromFlag(flag);

        if (type == 1){
            System.out.println("Got neutral EC");
            System.out.println("ID: " + id);
            System.out.println("Extra Info: " + type);
            neutralECLocations.add(location);
            influenceMap.put(location, robotInfluence);
            if (!assignedPerson.containsKey(location)) {
                assignedPerson.put(location, new HashSet<>());
            }

        } else if (type == 2) {
            enemyECLocations.add(location);
            influenceMap.put(location, robotInfluence);
            if (!assignedPerson.containsKey(location)) {
                assignedPerson.put(location, new HashSet<>());
            }
            neutralECLocations.remove(location);
            teamECLocations.remove(location);
            System.out.println("Got enemy EC");
            System.out.println("ID: " + id);
            System.out.println("Extra Info: " + type);
        } else if (type == 3) {
            enemyPoliticianLocations.add(location);
            System.out.println("Got politician");
            System.out.println("ID: " + id);
            System.out.println("Extra Info: " + type);
        } else if (type == 4) {
            enemySlandererLocations.add(location);
            System.out.println("Got Slanderer");
            System.out.println("ID: " + id);
            System.out.println("Extra Info: " + type);
        } else if (type == 5) {
            enemyMuckrakerLocations.add(location);
        } else if (type == 6) {
            teamECLocations.add(location);
            neutralECLocations.remove(location);
            enemyECLocations.remove(location);

            assignedPerson.remove(location);
            if (location.equals(target)){
                target = null;
            }
        } else {
            System.out.println("Didn't read anything");
            System.out.println("ID: " + id);
            System.out.println("Extra Info: " + type);
        }
    }

}
