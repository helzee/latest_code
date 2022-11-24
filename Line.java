import java.io.Serializable;
import java.util.ConcurrentModificationException;

public class Line {
    public static final double INFINITY = Double.MAX_VALUE;
    public double x1, y1; // x1 < x2
    public double x2, y2; //
    public double a; // slope
    public double b; // y-intersect

    public Point p1;
    public Point p2;

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

    public void cutOffLines(int direction, Line cut, double exactCut) {
        p1.cutOffLines(direction, cut, exactCut);
        p2.cutOffLines(direction, cut, exactCut);
    }

}
