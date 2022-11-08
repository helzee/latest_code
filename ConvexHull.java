import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This Convex Hull class is specific to the voronoi algorithm.
 * It can only be constructed from 1 or 2 points. Any more points need to be
 * added through merges
 */
public class ConvexHull {

   private class Node {
      Node next = null;
      Node prev = null;
      Point point = null;

      Node(Point p) {
         point = p;
      }
   }

   private final int PREV = 0;
   private final int NEXT = 1;

   // doubly linked list that stores points in convex hull. head of list is
   // bottom-most point of convex hull. The list iterates clockwise through the
   // hull with the last point in the list being counter-clockwise next to the
   // first point
   private Node bottomNode;

   public ConvexHull(Point p) {
      bottomNode = new Node(p);
      bottomNode.prev = bottomNode;
      bottomNode.next = bottomNode;

   }

   // add points in correct order
   public ConvexHull(Point p1, Point p2) {
      Node topNode;
      if (p1.y > p2.y) {
         bottomNode = new Node(p2);
         topNode = new Node(p1);

      } else {
         bottomNode = new Node(p1);
         topNode = new Node(p2);

      }
      bottomNode.prev = topNode;
      bottomNode.next = topNode;
      topNode.prev = bottomNode;
      topNode.next = bottomNode;

   }

   public Point getBottomPoint() {
      return bottomNode.point;
   }

   public Node getBottom() {
      return bottomNode;
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
    * 
    * ALWAYS called merge from left convex hull.
    */
   public ArrayList<LinkedList<Point>> merge(ConvexHull right) {
      LinkedList<Point> leftStitching = new LinkedList<Point>();
      LinkedList<Point> rightStitching = new LinkedList<Point>();

      ConvexHull left = this;

      // bottom most point of new merged convex hull
      Node merged = null;

      // use these nodes to create the new convex hull from left and right bottom
      Node mergedRight = null;
      Node mergedLeft = null;

      // determine which Convex hull starts lower. The merged convex hull will start
      // at the
      // lowest point. Then find the lowest bridge between the two
      if (left.getBottomPoint().y <= right.getBottomPoint().y) {
         merged = mergedLeft = left.getBottom();
         mergedRight = getCHBridge(right.getBottom(), mergedLeft.point, NEXT);

      } else {
         merged = mergedRight = right.getBottom();
         mergedLeft = getCHBridge(left.getBottom(), mergedRight.point, PREV);
      }

      Node topRight = mergedRight;
      Node topLeft = mergedLeft;

      // find the top of each CH
      while (topLeft.prev.point.y > topLeft.point.y) {
         topLeft = topLeft.prev;
      }
      while (topRight.next.point.y > topRight.point.y) {
         topRight = topRight.next;
      }

      // find the top CH bridge (similar to finding the bottom one) it may not always
      // be the topmost node of both CHs
      if (topLeft.point.y >= topRight.point.y) {
         topRight = getCHBridge(topRight, topLeft.point, PREV);
      } else {
         topLeft = getCHBridge(topLeft, topRight.point, NEXT);
      }

      // We now now where the top and bottom bridges of the two convex hulls are. We
      // can use this information to connect the two convex hulls and remove the inner
      // points by iterating from bottom to top bridge

      Node leftStitch = mergedLeft;
      Node rightStitch = mergedRight;

      while (leftStitch != topLeft) {
         leftStitching.add(leftStitch.point);
         leftStitch = leftStitch.next;
      }
      leftStitching.add(leftStitch.point); // top node

      while (rightStitch != topRight) {
         rightStitching.add(rightStitch.point);
         rightStitch = rightStitch.prev;
      }
      rightStitching.add(rightStitch.point); // top node

      // we now know all the nodes to discard and return as stitching. Now just merged
      // the two outer convex hull portions and the bridges

      mergedRight.prev = mergedLeft;
      mergedLeft.next = mergedRight;
      topRight.next = topLeft;
      topLeft.prev = topRight;

      ArrayList<LinkedList<Point>> res = new ArrayList<LinkedList<Point>>(2);
      res.add(leftStitching);
      res.add(rightStitching);

      // left convex hull is now whole convex whole
      bottomNode = merged;

      return res;

   }

   // finds the bridge of two convex hulls given an extreme point and an iterator
   // to find the other convex hull's extreme point.

   // Example: left extreme lower point and right variable point. Direction of
   // bridge is NorthEast and positive. Therefore, we are looking for any points to
   // the right of the variable point that minimize the slope of the bridge. We
   // stop the moment
   // we increase slope oft he bridge.

   // we are always trying to minimize the absolute value of the slope.
   private Node getCHBridge(Node variable, Point extreme, int direction) {

      double slope = Math.abs(getSlope(variable.point, extreme));

      while (true) {
         double nextSlope = Math.abs(getSlope(variable.prev.point, extreme));
         if (nextSlope >= slope) {
            break;
         }
         slope = nextSlope;

         if (direction == PREV) {
            variable = variable.prev;
         } else {
            variable = variable.next;
         }

      }

      return variable;
   }

   private double getSlope(Point a, Point b) {
      return (double) (b.y - a.y) / (double) (b.x - a.x);
   }

   // WARNING this function alters the points by giving them lines not part of the
   // Voronoi diagram. This is only used for testing.
   public void draw() {
      Node iterator = bottomNode;
      do {

         Point p = iterator.point;
         p.insertLine(new Line(p, iterator.next.point));
         iterator = iterator.next;
      } while (iterator != bottomNode);

   }

}
