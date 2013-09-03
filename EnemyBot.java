package g21;

public class EnemyBot implements Comparable<EnemyBot> {

    private String  name;       // name is used as identifier
    private long    time;       // current game time (updated by target method)
    private double  posX;       // current tank x position
    private double  posY;       // current tank y position
    private double  gunDir;     // current tank aiming direction
    private long    lastSeen;   // last spotted round
    private double  energy;     // current energy state
    private Position pos;       // position log

    public EnemyBot(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Position getPos() {
        return pos;
    }

    public int getAge() {

        return (int) (time - lastSeen);
    }

    public void updateTank(double posX, double posY, long time, double dir) {
        this.time = time;
        this.posX = posX;
        this.posY = posY;
        this.gunDir = dir;
    }

    public void updateTarget(
            double posX, double posY, double e, double h, long time) {

        this.time = time;
        long dTime = time - lastSeen;
        lastSeen = time;
        energy = e;

        System.out.println(" time since last: " + dTime);

        if (pos == null) {
            pos = new Position(posX, posY, h, time);
        }
        else {
            pos.update(posX, posY, h, time);
        }
    }

    public int guessTurnTime() {

        double TH = Helper.calcHeadingFromPosition(
                posX, posY, pos.guessX(time), pos.guessY(time));
        double TDH = Helper.calcHeadingFromPosition(
                posX, posY, pos.guessX(time+1), pos.guessY(time+1)) - TH;

        double guess = Math.ceil((TH - gunDir) / (20 - TDH));
        return (guess < 0) ? (int) guess * -1 : (int) guess;
    }

    public double guessHeading(long time) {

        return Helper.calcHeadingFromPosition(
            posX, posY, pos.guessX(time), pos.guessY(time));
    }

    public double guessDistance(long time) {

        return Helper.calcDistanceFromPosition(
            posX, posY, pos.guessX(time), pos.guessY(time));
    }

    public double guessShotTime() {
        return pos.guessShotTime(posX, posY);
    }

    public int priority() {
        
        return 1000 
            - (int) shotDifficulty()*10
            - 10*getAge()
            - 100*guessTurnTime();
    }

    public int shotDifficulty() {

        return (int) (guessShotTime() + 2*getAge());
    }

    public int compareTo(EnemyBot that) {
        
        return this.priority() - that.priority();
    }

    public String toString() {
        String out = "** TARGET ROBOT **\n";
        out += String.format(" name:   %-20s  age: %2d turns\n",
                getName(), time - lastSeen);
        out += String.format(" bearing: %4.2f deg  distance: %3.2f ticks\n",
                guessHeading(time), guessDistance(time));
        out += String.format(" priority:  %4d            shot diff:  %3d\n",
                priority(), shotDifficulty());
        return out;
    }
}
