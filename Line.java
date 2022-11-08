import java.io.Serializable;

public class Line {
    public double x1, y1; // x1 < x2
    public double x2, y2; //
    public double a; // slope
    public double b; // y-intersect
    public boolean x1Bound = false;
    public boolean x2Bound = false;

    public Line(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        a = (y2 - y1) / (x2 - x1);
        b = y1 - a * x1;
    }

    public boolean isPointInXBounds(double x) {
        // is the point within bounds of the line segments

        if (x1 < x2) {
            if (x1Bound) {
                if (x < x1) {
                    return false;
                }
            }
            if (x2Bound) {
                if (x > x2) {
                    return false;
                }
            }
        } else {
            if (x1Bound) {
                if (x > x1) {
                    return false;
                }
            }
            if (x2Bound) {
                if (x < x2) {
                    return false;
                }
            }
        }

        return true;
    }

    public void bind(int x) {
        if (x == 2) {
            x2Bound = true;
        } else if (x == 1) {
            x1Bound = true;
        } else {
            System.err.println("ERROR in bind(). Tried to bind nonexistent x value" + x);
        }
    }

}
