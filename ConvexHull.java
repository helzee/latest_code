import java.util.LinkedList;

public class ConvexHull {

   private LinkedList<Point> convexHull = new LinkedList();

   public ConvexHull() {

   }

   public ConvexHull(Point p) {
      rightConvexHull.add(p);
      leftConvexHull.add(p);

   }

   public ConvexHull(Point p1, Point p2) {
      rightConvexHull.add(p1);
      leftConvexHull.add(p1);
      rightConvexHull.add(p2);
      leftConvexHull.add(p2);

   }

   // when starting a stitch, we need to figure out which points are lowest to
   // start the convex hull
   public void determineConvexHullBottom(Point lpoint, Point rpoint) {
      if (lpoint.y < rpoint.y) {
         rightConvexHull.add(lpoint);
         leftConvexHull.add(lpoint);

      } else if (lpoint.y == rpoint.y) {
         leftConvexHull.add(lpoint);
         rightConvexHull.add(rpoint);

      } else {
         leftConvexHull.add(rpoint);
         rightConvexHull.add(rpoint);

      }
   }

   public void determineConvexHullTop(Point lpoint, Point rpoint) {
      if (lpoint.y > rpoint.y) {
         rightConvexHull.add(lpoint);
         leftConvexHull.add(lpoint);

      } else if (lpoint.y == rpoint.y) {
         leftConvexHull.add(lpoint);
         rightConvexHull.add(rpoint);

      } else {
         leftConvexHull.add(rpoint);
         rightConvexHull.add(rpoint);

      }
   }

}
