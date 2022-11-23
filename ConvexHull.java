
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedSet;
import java.util.Vector;

/**
 * This Convex Hull class is specific to the voronoi algorithm.
 * It can only be constructed from 1 or 2 points. Any more points need to be
 * added through merges
 */
public class ConvexHull {

   // head of list is
   // bottom-most point of convex hull. The list iterates clockwise through the
   // hull with the last point in the list being counter-clockwise next to the
   // bottom-most point
   private Vector<Point> points;

   public ConvexHull(Point p) {

      points = new Vector<Point>();
      points.add(p);

   }

   // add points in correct order
   public ConvexHull(Point p1, Point p2) {
      points = new Vector<Point>();
      if (p1.y > p2.y) {
         points.add(p2);
         points.add(p1);

      } else {
         points.add(p1);
         points.add(p2);

      }

   }

   public Point getBottomPoint() {
      return points.get(0);
   }

   /*
    * Merge 2 convex hulls that are left and right of eachother. . The merged CH is
    * the left convex hull. This method
    * returns two linked lists which represent
    * the points removed from each convex hull in order of low to high. Each list
    * also contains a point from the new convex hull representing entrance and exit
    * points into the stitching (Note: not
    * lowest to highest. sometimes the points removed can start higher than the
    * lowest point on the CH)
    * The merge utilizes grahams algorithm
    * 
    * ALWAYS called merge from left convex hull.
    */
   public Vector<Vector<Point>> merge(ConvexHull right) {

      // 1. find the lowest point of each convex hull. Choose the lowest one (and
      // leftmost if both are same height) as origin. remove the origin from its
      // original convex hull
      Point origin;
      if (this.getBottomPoint().y <= right.getBottomPoint().y) {
         origin = this.getBottomPoint();

      } else {
         origin = right.getBottomPoint();

      }

      // EDGE CASE: check if one of thte hulls is empty after getting origin

      // 2. Sort all points based on their polar angle from the origin point.
      // I created a comparator function in this function so that it can access the
      // ORIGIN
      PriorityQueue<Point> sortedPoints = new PriorityQueue<Point>((Point a, Point b) -> {
         double distA = 0.0;
         double distB = 0.0;
         double polarA = polar_angle(origin, a, distA);
         double polarB = polar_angle(origin, b, distB);
         if (polarA < polarB) {
            return -1;
         } else if (polarA > polarB) {
            return 1;
         } else {
            return distA < distB ? -1 : 1;
         }
      });
      // As we sort points into priority queue, Track the leftmost and rightmost point
      // coords of each CH.

      int rightMostX = this.points.get(0).x;
      for (Point p : this.points) {
         if (p != origin) {
            sortedPoints.add(p);
         }

         if (p.x > rightMostX) {
            rightMostX = p.x;
         }
      }
      int leftMostX = right.points.get(0).x;
      for (Point p : right.points) {
         if (p != origin) {
            sortedPoints.add(p);
         }
         if (p.x < leftMostX) {
            leftMostX = p.x;
         }
      }

      // 3. Now that points are sorted. Run the graham algorithm.
      Vector<Vector<Point>> res = graham(sortedPoints, rightMostX);
      // add the origin to this convex hull
      this.points.add(0, origin);
      // Within the graham algorithm, store discarded points from each CH in their
      // own priority queue ordered based on lowest y value. Return the new convex
      // hull as a vector
      // Replace this convex hull (the left one) with the new one. Return the two
      // priority queues

      // go through the convex hull. add the bridge points.. points at the top and
      // bottom of each stitching
      Vector<Point> leftStitching = res.get(0);
      Vector<Point> rightStitching = res.get(1);

      PriorityQueue<Point> leftBridge = new PriorityQueue<>();
      PriorityQueue<Point> rightBridge = new PriorityQueue<>();

      for (int i = 1; i < points.size() + 1; i++) {
         Point a = points.get(i - 1);
         Point b = points.get(i % points.size());
         if (a.x <= rightMostX && b.x > rightMostX) {
            // point a is in left side and b is in right side
            if (leftBridge.peek() != a) {
               leftBridge.add(a);
            }
            if (rightBridge.peek() != b) {
               rightBridge.add(b);
            }

         } else if (a.x > rightMostX && b.x <= rightMostX) {
            // point a is in right side and b is in left side
            if (leftBridge.peek() != b) {
               leftBridge.add(b);
            }
            if (rightBridge.peek() != a) {
               rightBridge.add(a);
            }

         }
      }
      // now the bridges are added to the end of the stitching. This represents the
      // top of the stitching. We now need to move the lower bridge to the bottom of
      // the stitching
      leftStitching.add(0, leftBridge.poll());
      leftStitching.add(leftBridge.poll());
      rightStitching.add(0, rightBridge.poll());
      rightStitching.add(rightBridge.poll());

      return res;
   }

   /**
    * Performs the graham algorithm that scan all points from the bottom as
    * revolving a line leftward and eliminating non-convex points.
    *
    * @param q             a priority queue of points sorted by polar angle from
    *                      origin
    * @param rightBoundary the rightmost point's xvalue in the left side
    * @return two priority queues containing the discarded poitns from each side,
    *         sorted from lowest to highest
    */
   private Vector<Vector<Point>> graham(PriorityQueue<Point> q, int leftSidesBoundary) {
      Vector<Point> leftStitching = new Vector<Point>();
      Vector<Point> rightStitching = new Vector<Point>();
      Vector<Point> hull = new Vector<Point>();
      Point last2 = null, last1 = null, next = null;

      // choose the first three points as convex-hull points.
      for (int i = 0; i < 3 && q.size() > 0; i++) {
         last2 = last1;
         last1 = q.poll();
         hull.add(last1);
         // q.remove(0);
      }

      double prev_polar_angle = 0.0;
      while (q.size() > 0) {
         next = q.poll();

         while ((prev_polar_angle = leftturn(last2, last1, next, prev_polar_angle)) < 0) {
            // remove last1 that is no longer a convex point
            Point removed = hull.remove(hull.size() - 1);
            if (removed.x <= leftSidesBoundary) { // point belongs to left
               leftStitching.add(removed);
            } else {
               rightStitching.add(removed);
            }
            if (hull.size() < 2) // check for excpetions
               break;
            // back-track to see previous points were actually concave.
            last1 = hull.elementAt(hull.size() - 1);
            last2 = hull.elementAt(hull.size() - 2);
         }
         // now advance to the next point.
         last2 = hull.lastElement();
         last1 = next;
         hull.add(next);
      }

      q.addAll(hull);

      // replace the old left side's convex hull with the new one
      this.points = hull;

      Vector<Vector<Point>> res = new Vector<>();
      res.add(leftStitching);
      res.add(rightStitching);
      return res;
   }

   /**
    * Checks if B is hit by line that revolves left on A to B
    *
    * @param a the central point of a left-revovling line AC
    * @param b the point to check
    * @param c the destination to revolve line AC
    * @param p a's polar angle
    * @return b's polar angle if B is it by line AC that revolves left on A to B.
    *         Otherwise -1
    */
   double leftturn(Point a, Point b, Point c, double pa) {
      double distance = 0.0; // needed for polar_angle
      double pb = polar_angle(a, b, distance);
      double pc = polar_angle(a, c, distance);
      return (pb > pa && pc > pb) ? pb : -1;
   }

   /**
    * Computes the polar angle and distance between the origin o and the
    * the other point p.
    *
    * @param o        the orign point
    * @param p        the other point
    * @param distance the distance between o and p (to be returned)
    * @return the polar angle
    */
   public static double polar_angle(Point o, Point p, double distance) {
      double radian;
      double sin = 0.0;
      double arcsin = 0.0;
      if (o.x == p.x && o.y == p.y) {
         distance = 0;
         radian = 0;
      } else {
         int x = p.x - o.x;
         int y = p.y - o.y;

         distance = Math.sqrt(Math.pow(x, 2.0) +
               Math.pow(y, 2.0));
         sin = Math.abs(y / distance);
         arcsin = Math.asin(sin);
         if (x >= 0 && y >= 0) {
            // NE [+x, +y] (0 - 89)
            radian = arcsin;
         } else if (x < 0 && y >= 0) {
            // NW [-x, +y] (90 - 179)
            radian = Math.PI - arcsin;
         } else if (x < 0 && y < 0) {
            // SW [-x, -y] (180 - 269)
            radian = arcsin + Math.PI;
         } else {
            // SE [+x, -y] (270 - 359)
            radian = Math.PI * 2 - arcsin;
         }
      }

      return radian;
   }

   // WARNING this function alters the points by giving them lines not part of the
   // Voronoi diagram. This is only used for testing.
   public void draw() {

   }

}
