import java.util.Random;
import java.util.Vector;

public class TestConvexHull {

   public static void main(String[] args) {
      int N = 0;

      // validate args
      if (args.length > 0) {
         try {
            N = Integer.parseInt(args[0]);
         } catch (Exception e) {
         }
      }
      if (N <= 0 || N > 1000000) {
         System.err.println("usage: java Driver #points");
         System.err.println("       where 0< #points <= 1000000"); // up to 1M
         System.exit(-1);
      }

      // generate N points
      Random rand = new Random(10);
      Vector<Point> points = new Vector<Point>();

      // space should be always 1250 x 1250
      int size = 1250;
      int[][] map = new int[size][size];

      for (int i = 0; i < N; i++) {
         do {
            int x = rand.nextInt(size);
            int y = rand.nextInt(size);
            if (map[x][y] != 1) {
               map[x][y] = 1;
               break;
            }
         } while (true);
      }

      // generate points on map[x][y] == 1
      for (int x = 0; x < size; x++)
         for (int y = 0; y < size; y++)
            if (map[x][y] == 1)
               points.add(new Point(x, y));
      // they are sorted in x and then in y
      // visualize the diagram: max size = 1250 x 1250
      VoronoiGraphics vg = new VoronoiGraphics(size, size, points);
      Thread graphics = new Thread(vg);
      graphics.start();

      // generate a voronoi diagram
      VoronoiDiagram voronoi = new VoronoiDiagram(size, size, points);
   }

}
