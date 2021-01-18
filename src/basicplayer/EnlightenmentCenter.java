package basicplayer;
import battlecode.common.*;
import java.util.HashMap;
import java.util.Map;


public class EnlightenmentCenter extends Robot {

    static final int ENLIGHTMENT_CENTER_SENSOR_RADIUS_SQUARED = 40;

    static final Direction[] directions = RobotPlayer.directions;
    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static final int POLITICIAN_RATIO = 3;
    static final int SLANDERER_RATIO = 1;
    static final int MUCKRAKER_RATIO = 6;
    static final int TOTAL_RATIO = POLITICIAN_RATIO + SLANDERER_RATIO + MUCKRAKER_RATIO + 1;

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

    @Override
    public int getSenseRadiusSquared() {
        return ENLIGHTMENT_CENTER_SENSOR_RADIUS_SQUARED;
    }

    @Override
    public void run() throws GameActionException {
        getSensedSquares();
        int id = this.rc.getID();


        int total = slandererCount + politicianCount + muckrakerCount + 1;

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
                rc.buildRobot(toBuild,dir, influence);
                if (slandererCount/total < SLANDERER_RATIO/TOTAL_RATIO){
                    slandererCount++;
                } else if (muckrakerCount/total < MUCKRAKER_RATIO/TOTAL_RATIO){
                    muckrakerCount++;
                } else {
                    politicianCount++;
                }
                
            } else {
                break;
            }
        }
    }

}
