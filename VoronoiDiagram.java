import java.util.Collections;
import java.util.Vector;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

public class VoronoiDiagram {

	public VoronoiDiagram(int size_x, int size_y, Vector<Point> points) {
		divide(size_x, size_y, points, 0, points.size() - 1);
	}

	ConvexHull divide(int size_x, int size_y, Vector<Point> points, int lower, int upper) {
		int size = upper - lower + 1; // + 1 because converting last index to size?

		if (size > 2) {
			int mid = lower + size / 2;
			ConvexHull leftConvexHull = divide(size_x, size_y, points, lower, mid - 1);
			ConvexHull rightConvexHull = divide(size_x, size_y, points, mid, upper);
			return stitch(size_x, size_y, points, leftConvexHull, rightConvexHull);
			// if we comput convex hull to reduce time complexity, could do it after we get
			// each ConvexHull. Take right convex hull of left ConvexHull and left CV of
			// right
			// ConvexHull. then send those to stitch
		} else {
			// base case
			if (size == 2) {
				// draw a line
				Point p1 = points.elementAt(lower);
				Point p2 = points.elementAt(upper);

				Line bisector = bisectorLine(size_x, size_y, p1, p2);

				// lower point always gets the new line
				if (p1.y < p2.y) {
					p1.insertLine(bisector);
				} else {
					p2.insertLine(bisector);
				}

				return new ConvexHull(p1, p2);

			}

		}

		return new ConvexHull(points.elementAt(lower));
	}

	// check the newest line for this point for an intersection
	// if point has no line or no intersection found then return null
	private double[] findItx(Point p1, Line bisector, double srcX, double srcY) {
		double[] itx = null;
		double dist = Double.MAX_VALUE;
		int i = 0;
		for (Line line : p1.lines) {
			double[] temp = intersect(bisector, line);
			if (temp != null) {
				double distTemp = distance(temp[0], temp[1], srcX, srcY);

				if (distTemp < dist) {
					itx = new double[3];
					itx[0] = temp[0];
					itx[1] = temp[1];
					itx[2] = i;
					dist = distTemp;

				}
			}

			i++;
		}

		return itx;
	}

	private ConvexHull stitch(int size_x, int size_y, Vector<Point> points,
			ConvexHull leftConvexHull, ConvexHull rightConvexHull) {

		ArrayList<LinkedList<Point>> stitching = leftConvexHull.merge(rightConvexHull);

		ListIterator<Point> leftStitching = stitching.get(0).listIterator();
		ListIterator<Point> rightStitching = stitching.get(1).listIterator();

		// choose the lowest point from left and right.
		Point p1 = leftStitching.next();
		Point p2 = rightStitching.next();

		double srcX = 0.0;
		double srcY = 0.0;
		double endX = 0.0;
		double endY = 0.0;

		do {
			// 1. get a bisector line between them.
			Line bisector = bisectorLine(size_x, size_y, p1, p2);

			// 2. adjust the src point
			srcX = (endX == 0.0 && endY == 0.0) ? ((bisector.y1 < bisector.y2) ? bisector.x1 : bisector.x2) : endX;
			srcY = (endX == 0.0 && endY == 0.0) ? ((bisector.y1 < bisector.y2) ? bisector.y1 : bisector.y2) : endY;

			// 3. compute the intersect with the bottom voronoi edges.

			double[] its1 = findItx(p1, bisector, srcX, srcY);
			double[] its2 = findItx(p2, bisector, srcX, srcY);

			// 4. find which of the two intersects with the bisector line is closer to the
			// source point
			double dist1 = (its1 != null) ? distance(its1[0], its1[1], srcX, srcY) : Double.MAX_VALUE;
			double dist2 = (its2 != null) ? distance(its2[0], its2[1], srcX, srcY) : Double.MAX_VALUE;

			System.out.println("dist1=" + dist1 + ", dist2=" + dist2);
			// if no intersections. we are at the top of both convex hulls. We should end
			// the loop after this
			if (its1 == null && its2 == null) {
				endY = size_y;
				endX = (endY - bisector.b) / bisector.a;
			} else {
				endX = (dist1 < dist2) ? its1[0] : its2[0];
				endY = (dist1 < dist2) ? its1[1] : its2[1];
			}

			// 5. cut off the bisector line to the last intersection
			bisector.x1 = srcX;
			bisector.y1 = srcY;
			bisector.x2 = endX;
			bisector.y2 = endY;

			System.out.println("bisector line: [" + srcX + "," + srcY + "]-->[" + endX + "," + endY + "]");

			// 6. cut off the voronoi edge at the intersection
			if (its1 != null || its2 != null) {
				if (dist1 < dist2) {
					p1.lines.elementAt((int) its1[2]).x2 = endX;
					p1.lines.elementAt((int) its1[2]).y2 = endY;
					// the line is now bounded at this point

				} else {
					p2.lines.elementAt((int) its2[2]).x1 = endX;
					p2.lines.elementAt((int) its2[2]).y1 = endY;

				}

			}

			// give the new line to the lowest point
			if (p1.y < p2.y) {
				p1.insertLine(bisector);
			} else {
				p2.insertLine(bisector);
			}

			// as mentioned earlier, we have traversed both convex hulls if this is true.
			// we finish up the new convex hull and give the bisector to the lower point
			if (its1 == null && its2 == null) {
				break;
			}

			// 7. choose the next point from the same side that keeps the last voronoi edge
			if (dist1 < dist2) {

				p1 = leftStitching.next();
			} else {

				p2 = rightStitching.next();
			}

		} while (true);

		return leftConvexHull;
	}

	private Line bisectorLine(int size_x, int size_y, Point p1, Point p2) {

		int mid_x = Math.abs(p1.x + p2.x) / 2;
		int mid_y = Math.abs(p1.y + p2.y) / 2;

		double slope = (double) (p2.y - p1.y) / (double) (p2.x - p1.x); // danger of div by 0? since 2 points could have
																								// same x axis
		double perpendicular_slope = -1 / slope;

		double intersect = mid_y - perpendicular_slope * mid_x;

		// extend bounds to infinite?
		// generate a bisector line
		// compute x1, y1
		double x1 = 0.0;
		double y1 = intersect;
		// compute x2, y2
		double x2 = size_x;
		double y2 = perpendicular_slope * x2 + intersect;

		return new Line(x1, y1, x2, y2);
	}

	private double[] intersect(Line l1, Line l2) {
		// a is slope
		double a1 = l1.a;
		double b1 = l1.b;

		double a2 = l2.a;
		double b2 = l2.b;

		double[] i = new double[2];
		i[0] = (b2 - b1) / (a1 - a2);
		i[1] = i[0] * a1 + b1;

		// is the point within bounds of the line segments
		if (!(Math.min(l1.x2, l2.x1) < i[0] && i[0] < Math.max(l1.x2, l1.x1))) {
			return null;
		}
		return i;
	}

	private double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
	}
}
