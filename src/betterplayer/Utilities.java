package betterplayer;
import battlecode.common.*;

public class Utilities {

    /**
     * Given three colinear points p, q, r, the function checks if point q lies on the line segment 'pr'
     * @param p a MapLocation object
     * @param q a MapLocation object
     * @param r a MapLocation object
     * @return true if q lies on the line segment 'pr' ; false otherwise
     */
    static boolean onLineSegment(MapLocation p, MapLocation q, MapLocation r) {
        return (q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x)
                && q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y)); //
    }

    /**
     * Find the orientation of the ordered triplet (p, q, r).
     * This function returns the following int values
     * 0 == p, q, and r are collinear
     * 1 == Clockwise orientation
     * 2 == Coounterclockwise orientation
     * @param p a MapLocation object
     * @param q a MapLocation object
     * @param r a MapLocation object
     * @return an integer representing the orinetation of the three points
     */
    static int findOrientation(MapLocation p, MapLocation q, MapLocation r) {
        final int val = (q.y - p.y) * (r.x - q.x) -
                  (q.x - p.x) * (r.y - q.y);

        if (val == 0) return 0; // colinear

        return (val > 0)? 1: 2; // clockwise or counterclockwise
    }

    /**
     * Function that returns true if the line segment 'p1q1' and 'p2q2' intersect
     * See {@linktourl https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/} for details of
     * @param p1 a Maplocation object
     * @param q1 a Maplocation object
     * @param p2 a Maplocation object
     * @param q2 a Maplocation object
     * @return true if line segment 'p1q1' and 'p2q2' intersect; false otherwise
     */
    static boolean doIntersect(MapLocation p1, MapLocation q1, MapLocation p2, MapLocation q2) {
        final int o1 = findOrientation(p1, q1, p2);
        final int o2 = findOrientation(p1, q1, q2);
        final int o3 = findOrientation(p2, q2, p1);
        final int o4 = findOrientation(p2, q2, q1);

        // General case:
        if (o1 != o2 && o3 != o4) return true;

        // Special edge cases
        // p1, q1 and p2 are colinear and p2 lies on segment p1q1
        if (o1 == 0 && onLineSegment(p1, p2, q1)) return true;

        // p1, q1 and q2 are colinear and q2 lies on segment p1q1
        if (o2 == 0 && onLineSegment(p1, q2, q1)) return true;

        // p2, q2 and p1 are colinear and p1 lies on segment p2q2
        if (o3 == 0 && onLineSegment(p2, p1, q2)) return true;

        // p2, q2 and q1 are colinear and q1 lies on segment p2q2
        if (o4 == 0 && onLineSegment(p2, q1, q2)) return true;

        return false;

    }


    static Direction leftHandSideForCurrentDirection(Direction bugDirection) {
        if (bugDirection.ordinal()%2 == 0) { // bugDirection is cardinal (N,E,S,W)
            return bugDirection.rotateLeft().rotateLeft();
        } else { //bugDirection is ordinal (NE,SE,SW,NW)
            return bugDirection.rotateLeft().rotateLeft().rotateLeft();
        }
    }

    static boolean checkTypeAtLocation(RobotController rc, Direction dir, RobotType type) throws GameActionException {
        RobotInfo robot = rc.senseRobotAtLocation(rc.getLocation().add(dir));
        if (robot != null) {
            return (robot.type.equals(type) && robot.getTeam().equals(rc.getTeam()));
        } else {
            return false;
        }
    }

    static boolean checkFlagAtLocation(RobotController rc, Direction dir) throws GameActionException {
        RobotInfo robot = rc.senseRobotAtLocation(rc.getLocation().add(dir));
        if (robot!= null) {
            int id = robot.ID;
            int flag = rc.getFlag(id);
            return (flag != Robot.SLANDERER_FLAG_CODE) || !robot.getType().equals(RobotType.ENLIGHTENMENT_CENTER);
        }
        return true;

    }

}

