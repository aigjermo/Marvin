package aig;
import java.lang.Math;

public class Helper {

    public static double calcXFromHeadingDistance(
            double heading, double distance, double baseX)
    {
        return baseX + Math.sin(heading) * distance;
    }

    public static double calcYFromHeadingDistance(
            double heading, double distance, double baseY)
    {
        return baseY + Math.cos(heading) * distance;
    }

    public static double calcHeadingFromPosition(
            double startX, double startY, double targetX, double targetY)
    {
        return Math.atan2(targetX-startX,targetY-startY);
    }

    public static double calcDistanceFromPosition(
            double startX, double startY, double targetX, double targetY)
    {
        double x = targetX-startX;
        double y = targetY-startY;
        return Math.sqrt(x*x+y*y);
    }

    public static double calcRelativeBearing(
            double absoluteBearing, double heading)
    {
        double out = absoluteBearing - heading;
        return plusMinusPI(out);
    }

    public static double plusMinusPI(double angle) {
        if (angle > Math.PI) angle -= 2*Math.PI;
        if (angle < -Math.PI) angle += 2*Math.PI;
        return angle;
    }
}
