package basicplayer;
import battlecode.common.*;
import java.util.HashMap;
import java.util.Map;


public class EnlightenmentCenter extends Robot {

    static final int ENLIGHTMENT_CENTER_SENSOR_RADIUS_SQUARED = 40;
    boolean earlyGame = true;


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

    static int counter = 0;

    public EnlightenmentCenter(RobotController rc) throws GameActionException {
        super(rc);
    }


    private void runEarlyGameStrat() throws GameActionException {
        RobotType toBuild = RobotType.SLANDERER;
        int influence = SLANDERER_INFLUENCE;

        for (Direction dir : RobotPlayer.ordinalDirections) { // Build one slanderer in the beginning
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                rc.buildRobot(toBuild, dir, influence);
                slandererCount++;
                break;
            }
        }

        toBuild = RobotType.MUCKRAKER;
        influence = MUCKRAKER_INFLUENCE;
        int initialMuckrakerCount = 0;

        while (initialMuckrakerCount < 8) {
            System.out.println("Make muckrakers");
            if (rc.isReady()) {
                if (rc.canBuildRobot(toBuild, RobotPlayer.directions[initialMuckrakerCount], influence)) {
                    rc.buildRobot(toBuild, RobotPlayer.directions[initialMuckrakerCount], influence);
                    muckrakerCount++;
                }
                initialMuckrakerCount++;
            } else {
                Clock.yield();
            }
        }

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


            int id = this.rc.getID();


            double total = slandererCount + politicianCount + muckrakerCount + 1.0;

            RobotType toBuild;
            int influence;
            if (slandererCount / total <= SLANDERER_RATIO / TOTAL_RATIO) {
                toBuild = RobotType.SLANDERER;
                influence = SLANDERER_INFLUENCE;
            } else if (muckrakerCount / total <= MUCKRAKER_RATIO / TOTAL_RATIO) {
                toBuild = RobotType.MUCKRAKER;
                influence = MUCKRAKER_INFLUENCE;
            } else {
                toBuild = RobotType.POLITICIAN;
                influence = POLITICIAN_INFLUENCE;
            }

            for (Direction dir : directions) {
                if (rc.canBuildRobot(toBuild, dir, influence)) {
                    rc.buildRobot(toBuild, dir, influence);
                    if (slandererCount / total < SLANDERER_RATIO / TOTAL_RATIO) {
                        slandererCount++;
                    } else if (muckrakerCount / total < MUCKRAKER_RATIO / TOTAL_RATIO) {
                        muckrakerCount++;
                    } else {
                        politicianCount++;
                    }
                }
            }
        }

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

}
