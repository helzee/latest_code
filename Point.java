
import java.lang.Math;
import java.util.Stack;
import java.util.Vector;

public class Point implements Comparable<Point> {
    public Stack<Line> lines;

    public Stack<Line> stitching;

    public int x;
    public int y;

    public Point(int i, int j) {
        x = i;
        y = j;
        lines = new Stack<Line>();
        stitching = new Stack<>();
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

    /**
     * delete all lines to the LEFT or RIGHT of the given line
     * 
     * @param direction the side of the line that we cut off (1 = left, 2 = right)
     * @param cut       the line to determein where we make the cut
     */
    public void cutOffLines(int direction, Line cut, double exactCut) {

        Vector<Line> removedLines = new Vector<>();
        for (Line l : lines) {
            if (direction == 2) { // cutoff right side

                if (l.x1 > exactCut && l.x2 > exactCut) {
                    removedLines.add(l);
                }
            } else {

                if (l.x1 < exactCut && l.x2 < exactCut) {
                    removedLines.add(l);
                }

            }

        }
        for (Line l : removedLines) {
            l.removeSelf();
        }

    }

    public void applyStitching() {
        lines.addAll(stitching);
        stitching = new Stack<>();
    }

    public void addStitch(Line l) {
        stitching.add(l);
    }

}
