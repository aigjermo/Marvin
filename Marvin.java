package aig;
import robocode.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import aig.*;

/**
 * Marvin - a robot by aig
 */
public class Marvin extends AdvancedRobot {

    // variables
    Target target = null;
    int direction = 1;
    double x, y;
    long t;
    double offset = 0;
    long shootTime = 0;

    /**
     * run: IntelliBot's default behavior
     */
    public void run() {

        setColors(Color.black,Color.orange,Color.black);

        // independent turret and radar
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        Grid.init(getBattleFieldWidth(), getBattleFieldHeight());

        while(true) {

            x = getX();
            y = getY();
            t = getTime();

            seek();
            target();
            shoot();
            drive();

            execute();
        }
    }

    /**
     * Seek: turn the radar to look for other robots
     */
    private void seek() {

        if (getRadarTurnRemaining() < 10)
            setTurnRadarLeft(360);
    }

    /**
     * Shoot: Shoot at good opportunities
     */
    private void shoot() {

        if (target != null) {
            long time = getTime();
            double r = getGunTurnRemainingRadians();
            double aimMargin = 0.05;
            if (getGunHeat() == 0 && r > aimMargin*-1 && r < aimMargin) {
                setFire(Rules.MAX_BULLET_POWER); 
            }
        }
    }

    /**
     * Target: Choose a target and track it.
     */
    private void target() {

        long time = getTime();
        double x = getX();
        double y = getY();
        double gunDir = getGunHeadingRadians();

        Target t = Grid.getPriorityTarget();

        // find enemy with highest pri

        // turn gun towards him
        if (target != null) {
            long turnTime = target.guessTurnTime();
            long shotTime = (long) (target.guessShotTime()+turnTime);
            double turn = Helper.calcRelativeBearing(
                    target.guessHeading(time + turnTime + shotTime), gunDir);
            setTurnGunRightRadians(turn);
            System.out.print(target);
            System.out.printf(" turning:  %4.2f deg\n", turn);
        }
    }

    /**
     * drive: get at them!
     */
    public void drive() {

        setMaxVelocity(Rules.MAX_VELOCITY);
        double bearing = 0;

        if (target != null) {
            bearing = Helper.calcRelativeBearing(
                    target.guessHeading(getTime()), getHeadingRadians());
            bearing += Math.PI * Math.max(0,
                    (1-(target.guessDistance(getTime()) / 500)));
        }
        else {
            bearing = Helper.calcRelativeBearing(
                    Helper.calcHeadingFromPosition(
                        getX(), getY(),
                        getBattleFieldWidth()/2,
                        getBattleFieldHeight()/2),
                    getHeadingRadians());
        }

        // turn
        if (getTime() % 6 == 0) {
            offset = (Math.random()-0.5)*(Math.PI);
        }
        setTurnRightRadians(bearing + offset);

        if (target != null && target.guessDistance(getTime()) > 200) {
            direction = Math.abs(direction);
        }
        setAhead(100*direction);
    }

    /**
     * onScannedRobot: Update the battlefield map
     */
    public void onScannedRobot(ScannedRobotEvent e) {

        String name = e.getName();

        EnemyBot enemy = enemies.get(name);
        if (enemy == null) {
            enemy = new EnemyBot(name);
            enemies.put(name, enemy);
        }

        System.out.println("** DETECTED " + e.getName() + " **");

        double heading = getHeadingRadians() + e.getBearingRadians();

        double x = Helper.calcXFromHeadingDistance(
                heading, e.getDistance(), getX());
        double y = Helper.calcYFromHeadingDistance(
                heading, e.getDistance(), getY());

        enemy.updateTarget(
                x, y, e.getEnergy(), e.getHeadingRadians(), getTime());
    }

    /**
     * onHitByBullet: What to do when you're hit by a bullet
     */
    public void onHitByBullet(HitByBulletEvent e) {
        //direction *= -1;
        target = enemies.get(e.getName());
    }

    /**
     * onHitWall: reverse for a few ticks
     */
    public void onHitWall(HitWallEvent e) {
        direction *= -1;
    }

    /**
     * onHitRobot: reverse for a few ticks
     */
    public void onHitRobot(HitRobotEvent e) {
        direction *= -1;
        //target = enemies.get(e.getName());
    }

    public void onPaint(Graphics2D g) {
        for (EnemyBot e : enemies.values()) {
            long shotTime = (long) e.guessShotTime();
            e.getPos().paint(g, getTime(), shotTime, (e == target));
        }
    }
}
