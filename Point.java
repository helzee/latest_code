
import java.lang.Math;
import java.util.Stack;

public class Point implements Comparable<Point> {
    public Stack<Line> lines;

    public int x;
    public int y;

    public Point(int i, int j) {
        x = i;
        y = j;
        lines = new Stack<Line>();
    }

    public void insertLine(Line line) {
        lines.push(line);
    }

    public void deleteLine(Line line) {
        lines.remove(line);
    }

    public Line popLine() {
        if (lines.size() > 0)
            return lines.remove(0);
        else
            return null;
    }

    public Line peekLine() {
        if (lines.size() > 0)
            return lines.peek();
        else
            return null;
    }

    public double distance(Point buddy) {
        return Math.sqrt(Math.pow(buddy.x - x, 2.0) +
                Math.pow(buddy.y - y, 2.0));
    }

    public Point midPoint(Point buddy) {
        return new Point(buddy.x + x / 2,
                buddy.y + y / 2);
    }

    public int compareTo(Point x) {
        return this.y - x.y;
    }

}
