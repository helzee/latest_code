import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class Line {
    public static final double INFINITY = Double.MAX_VALUE;
    public double x1, y1; // x1 < x2
    public double x2, y2; //
    public double a; // slope
    public double b; // y-intersect

    public Point p1;
    public Point p2;
    static int RIGHT = 1, LEFT = -1, ZERO = 0;

    public Line(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        a = (y2 - y1) / (x2 - x1);
        b = y1 - a * x1;

    }

    public Line(double x1, double y1, double x2, double y2, Point p1, Point p2) {
        this(x1, y1, x2, y2);
        this.p1 = p1;
        this.p2 = p2;

    }

    public Line(Point a, Point b) {
        this(a.x, a.y, b.x, b.y);
        p1 = a;
        p2 = b;
    }

    public boolean inXBounds(double x) {
        return x < Math.max(x1, x2) && x > Math.min(x1, x2);
    }

    // delete all references to this line
    public void removeSelf() {

        p1.lines.remove(this);
        p2.lines.remove(this);

    }

    public void cutOffLines(int direction, Line cut, double exactCut, Vector<Line> res) {

        p1.cutOffLines(direction, cut, exactCut, res);
        p2.cutOffLines(direction, cut, exactCut, res);

    }

    public boolean isRightOf(Line line) {
        return proveDirection(line, RIGHT);

    }

    public boolean isLeftOf(Line line) {
        return proveDirection(line, LEFT);

    }

    public boolean isAbove(Line line) {
        return Math.min(line.y1, line.y2) > Math.max(this.y1, this.y2);
    }

    private boolean proveDirection(Line line, int direction) {
        Point a = new Point((int) line.x1, (int) line.y1);
        Point b = new Point((int) line.x2, (int) line.y2);
        Point p1 = new Point((int) this.x1, (int) this.y1);
        Point p2 = new Point((int) this.x2, (int) this.y2);

        return directionOfPoint(a, b, p1) == direction && directionOfPoint(a, b, p2) == direction;
    }

    private int directionOfPoint(Point A, Point B, Point P) {
        // subtracting co-ordinates of point A
        // from B and P, to make A as origin
        B.x -= A.x;
        B.y -= A.y;
        P.x -= A.x;
        P.y -= A.y;

        // Determining cross Product
        int cross_product = B.x * P.y - B.y * P.x;

        // return RIGHT if cross product is positive
        if (cross_product > 0)
            return RIGHT;

        // return LEFT if cross product is negative
        if (cross_product < 0)
            return LEFT;

        // return ZERO if cross product is zero.
        return ZERO;
    }

}
