
import java.util.Vector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Stack;

public class VoronoiDiagram {
	private int size_x;
	private int size_y;

	public VoronoiDiagram(int size_x, int size_y, Vector<Point> points) {
		this.size_x = size_x;
		this.size_y = size_y;
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

				p1.insertLine(bisector);

				p2.insertLine(bisector);

				return new ConvexHull(p1, p2);

			}

		}

		return new ConvexHull(points.elementAt(lower));
	}

	// check the newest line for this point for an intersection
	// if point has no line or no intersection found then return null
	private double[] findItx(Point p1, Line bisector, double srcX, double srcY, HashSet<Line> seenLines) {
		double[] itx = null;
		double dist = Double.MAX_VALUE;
		int i = 0;
		for (Line line : p1.lines) {

			double[] temp = intersect(bisector, line);
			if (temp != null) {
				double distTemp = distance(temp[0], temp[1], srcX, srcY);

				if (distTemp > 0.0 && distTemp < dist && !seenLines.contains(line)) {
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

		// we need to run a convex hull merge algorithm to find the starting and ending
		// bridge
		Vector<Stack<Point>> bridges = leftConvexHull.merge(rightConvexHull);

		Stack<Point> leftBridge = bridges.get(0);
		Stack<Point> rightBridge = bridges.get(1);

		// choose the lowest point from left and right.
		Point p1 = leftBridge.remove(0);
		Point p2 = rightBridge.remove(0);

		Point bottomLeftBridge = p1;
		Point bottomRightBridge = p2;

		double srcX = 0.0;
		double srcY = 0.0;
		double endX = 0.0;
		double endY = 0.0;

		HashSet<Line> seenLines = new HashSet<>();
		HashSet<Point> seenPoints = new HashSet<>();
		seenPoints.add(p1);
		seenPoints.add(p2);
		Point upperLeftBridge = p1;
		Point upperRightBridge = p2;

		if (!leftBridge.isEmpty()) {

			upperLeftBridge = leftBridge.pop();
		}
		if (!rightBridge.isEmpty()) {

			upperRightBridge = rightBridge.pop();
		}

		double maximumY = Math.max(upperLeftBridge.y, upperRightBridge.y);
		double minimumY = Math.min(p1.y, p2.y);

		Vector<Line> stitch = new Vector<>();
		Vector<Line> leftRemovedLines = new Vector<>();
		Vector<Line> rightRemovedLines = new Vector<>();

		do {
			// 1. get a bisector line between them.
			Line bisector = bisectorLine(size_x, size_y, p1, p2);
			// no longer necessary cuz of stitching
			seenLines.add(bisector);

			// 2. adjust the src point
			srcX = (endX == 0.0 && endY == 0.0) ? ((bisector.y1 < bisector.y2) ? bisector.x1 : bisector.x2) : endX;
			srcY = (endX == 0.0 && endY == 0.0) ? ((bisector.y1 < bisector.y2) ? bisector.y1 : bisector.y2) : endY;

			if (p1 == upperLeftBridge && p2 == upperRightBridge) { // this means we have finished. extend the line to
																						// infinity
				if (bisector.y1 < bisector.y2) {
					bisector.y1 = srcY;
					bisector.x1 = srcX;
				} else {
					bisector.x2 = srcX;
					bisector.y2 = srcY;
				}
				p1.insertLine(bisector);
				p2.insertLine(bisector);
				break;
			}
			if (p1 != bottomRightBridge && p2 != bottomRightBridge) {
				if (bisector.y1 < bisector.y2) {
					bisector.x1 = srcX;
					bisector.y1 = srcY;
				} else {
					bisector.x2 = srcX;
					bisector.y2 = srcX;
				}
			}

			// 3. compute the intersect with the bottom voronoi edges.

			double[] its1 = findItx(p1, bisector, srcX, srcY, seenLines);
			double[] its2 = findItx(p2, bisector, srcX, srcY, seenLines);

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

			Line l = null;

			// 6. add any edges that may get trimmed or deleted by this stitch line.
			// when the stitch line is complete, then we will look at all of these
			// candidates and determine if they should be deleted.
			if (dist1 < dist2 && its1 != null) {
				l = p1.lines.elementAt((int) its1[2]);
				trim(l, bisector, its1, endX, endY, maximumY, minimumY, 2, leftRemovedLines);
			} else if (its2 != null) {
				l = p2.lines.elementAt((int) its2[2]);
				trim(l, bisector, its2, endX, endY, maximumY, minimumY, 1, rightRemovedLines);
			}

			// give the new line to both points that share it

			p1.addStitch(bisector);

			p2.addStitch(bisector);

			// track the stitchings
			stitch.add(bisector);

			// as mentioned earlier, we have traversed both convex hulls if this is true.
			// we finish up the new convex hull and give the bisector to the lower point
			if (its1 == null && its2 == null || l == null) {
				break;
			}

			// 7. choose the next point from the same side that keeps the last voronoi edge
			// this point should be bisected by the line last intersected
			if (dist1 < dist2) {

				seenLines.add(l);
				if (l.p1 != p1 && !seenPoints.contains(l.p1)) {
					p1 = l.p1;

				} else {
					p1 = l.p2;

				}
				seenPoints.add(p1);

			} else {

				seenLines.add(l);
				if (l.p2 != p2 && !seenPoints.contains(l.p2)) {
					p2 = l.p2;

				} else {
					p2 = l.p1;

				}
				seenPoints.add(p2);

			}

		} while (true);

		// delete any lines from right side to the left of the stitch
		checkForRemoval(stitch, leftRemovedLines, 2, seenLines);
		checkForRemoval(stitch, rightRemovedLines, 1, seenLines);

		for (Point p : seenPoints) {
			p.applyStitching(stitch);
		}

		return leftConvexHull;
	}

	// remove all lines in removedLines to the <right|left> of the stitch
	// right = 2. left = 1
	private void checkForRemoval(Vector<Line> stitch, Vector<Line> removedLines, int direction,
			HashSet<Line> seenLines) {

		for (Line candidate : removedLines) {
			if (!seenLines.contains(candidate)) { // any lines the stitch crossed we dont wanna worry about
				candidate.removeSelf();
			}

			// boolean isRemoved = true;
			// for (Line stitching : stitch) {

			// if (direction == 2) { // right
			// if (!candidate.isRightOf(stitching)) {
			// isRemoved = false;
			// break;
			// }

			// } else {
			// if (!candidate.isLeftOf(stitching)) {
			// isRemoved = false;
			// break;
			// }
			// }
			// }
			// if (isRemoved) {
			// candidate.removeSelf();
			// }

		}
	}

	private void trim(Line l, Line bisector, double[] its,
			double endX, double endY, double maximumY, double minimumY, int direction, Vector<Line> removedLines) {

		if (its[1] > maximumY) { // check if we are exiting upper bridge (special case)
			// we need to cutoff the part of the line that goes to infinity
			if (l.y1 > l.y2) {
				l.x1 = endX;
				l.y1 = endY;
			} else {
				l.x2 = endX;
				l.y2 = endY;
			}

		} else if (its[1] < minimumY) { // check if we are entering lower bridge. cut off negative infinity
													// section
			if (l.y1 < l.y2) {
				l.x1 = endX;
				l.y1 = endY;
			} else {
				l.x2 = endX;
				l.y2 = endY;
			}

		} else {
			l.cutOffLines(direction, bisector, endX, removedLines);
			if (direction == 2) {
				if (l.x1 > l.x2) { // x1 is RIGHT side

					l.x1 = endX;
					l.y1 = endY;
				} else { // x2 is right side

					l.x2 = endX;
					l.y2 = endY;
				}
			} else {

				if (l.x1 < l.x2) { // x1 is left side

					l.x1 = endX;
					l.y1 = endY;
				} else { // x2 is left side

					l.x2 = endX;
					l.y2 = endY;
				}
			}

		}

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
		double x1 = -1 * size_x;
		double y1 = perpendicular_slope * x1 + intersect;
		// compute x2, y2
		double x2 = size_x;
		double y2 = perpendicular_slope * x2 + intersect;

		return new Line(x1, y1, x2, y2, p1, p2);
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
		if (!l1.inXBounds(i[0]) || !l2.inXBounds(i[0])) {
			return null;
		}

		return i;
	}

	private double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0));
	}
}
