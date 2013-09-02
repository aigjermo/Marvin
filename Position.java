package g21;
import java.util.Arrays;
import java.awt.Color;
import java.awt.Graphics2D;

class Position {

    private double[] X, Y, H;  // last three known positions
    private long[] T;
    private static long[] map = {800,800};

    public Position(double x, double y, double h, long time) {

        X = new double[3];
        Y = new double[3];
        H = new double[3];
        T = new long[3];

        Arrays.fill(X, x);
        Arrays.fill(Y, y);
        Arrays.fill(H, h);
        T[0] = time;
        T[1] = -1;
        T[2] = -1;
    }

    public void update(double x, double y, double h, long time) {

        int i = 3;
        while (--i > 0) {
            X[i] = X[i-1];
            Y[i] = Y[i-1];
            H[i] = H[i-1];
            T[i] = T[i-1];
        }
        X[i] = x;
        Y[i] = y;
        H[i] = h;
        T[i] = time;
    }

    public double guessX(long time) {
        return guessPos(time)[0];
    }

    public double guessY(long time) {
        return guessPos(time)[1];
    }

    public double deltHeading() {
        return (H[0]-H[1]) / (T[0]-T[1]);
    }

    public double guessShotTime(double x, double y) {
        double dist = Helper.calcDistanceFromPosition(x, y, X[0], Y[0]);
        double[] np = guessPos(T[0]+1);
        double dDist = Helper.calcDistanceFromPosition(x, y, np[0], np[1])-dist;

        double dB = 11;

        return dist / (dB - dDist);
    }

    public double guessSpeed() {
        return Helper.calcDistanceFromPosition(X[1], Y[1], X[0], Y[0])
            / (T[0]-T[1]);
    }

    public double[] guessPos(long time) {
        double[] pos = new double[2];

        if (T[1] < 0) { // not enough data
            pos[0] = X[0];
            pos[1] = Y[0];
            return pos;
        }

        double dHeading = deltHeading() * (time - T[0]);
        double gHeading = H[0] + (dHeading/2);
        double errHeading = Math.abs(Helper.plusMinusPI(
                gHeading -
                Helper.calcHeadingFromPosition(X[1], Y[1], X[0], Y[0])));

        if (errHeading > (Math.PI/2)) {
            gHeading = Helper.plusMinusPI(gHeading + Math.PI);
        }

        double gDistance = guessSpeed() * (time - T[0]);
        double x = Helper.calcXFromHeadingDistance(
                gHeading, gDistance, X[0]);
        double y = Helper.calcYFromHeadingDistance(
                gHeading, gDistance, Y[0]);

        pos[0] = (x < 0) ? 0 : ((x > map[0]) ? map[0] : x);
        pos[1] = (y < 0) ? 0 : ((y > map[1]) ? map[1] : y);
        return pos;
    }

    public void paint(Graphics2D g, long time, long shotTime, boolean target) {
        g.setColor(new Color(0x40, 0xff, 0x60, 0xbb));
        g.drawArc((int) X[0]-15, (int) Y[0]-15, 30, 30, 0, 360);
        g.drawArc((int) X[1]-10, (int) Y[1]-10, 20, 20, 0, 360);
        g.drawArc((int) X[2]-5, (int) Y[2]-5, 10, 10, 0, 360);

        if (target) {
            g.setColor(new Color(0xcc, 0xcc, 0x10, 0xbb));
            g.fillArc((int) guessX(time)-20, (int) guessY(time)-20,
                    40, 40, 0, 360);

            g.setColor(new Color(0xff, 0x10, 0x10, 0xff));
            int shotX = (int) guessX(time+shotTime);
            int shotY = (int) guessY(time+shotTime);
            g.drawArc(shotX-15, shotY-15, 30, 30, 0, 360);
            g.drawLine(shotX-20, shotY, shotX+20, shotY);
            g.drawLine(shotX, shotY-20, shotX, shotY+20);
        }
    }
}
