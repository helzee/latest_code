import java.io.Serializable;

public class Line {
    public static final double INFINITY = Double.MAX_VALUE;
    public double x1, y1; // x1 < x2
    public double x2, y2; //
    public double a; // slope
    public double b; // y-intersect

    public Line(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        a = (y2 - y1) / (x2 - x1);
        b = y1 - a * x1;

    }

    public Line(Point a, Point b) {
        this(a.x, a.y, b.x, b.y);
    }

}
