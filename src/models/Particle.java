package models;

public class Particle {

    private int id;
    private double radius;
    private double rc;
    private Point position;

    public Particle(int id, double radius, double rc, double x, double y) {
        this.id = id;
        this.radius = radius;
        this.rc = rc;
        this.position = new Point(x, y);
    }

    public Point getPosition() {
        return this.position;
    }

    public double getRadius() {
        return this.radius;
    }

    public int getId() {
        return this.id;
    }

    public double getRc() {
        return rc;
    }

    public static double getDistance(Particle p, Particle q) {
        double dx = Math.abs(p.getPosition().x - q.getPosition().x);
        double dy = Math.abs(p.getPosition().y - q.getPosition().y);
        double hyp = Math.sqrt(dx * dx + dy * dy);
        return hyp - p.getRadius() - q.getRadius();
    }
//
//    public static String getXYZformat(Particle particle, int R, int G, int B) {
//        return particle.getRadius() + "\t" + particle.getId() + "\t" + particle.getPosition().x + "\t" + particle.getPosition().y + "\t" + R + "\t" + G + "\t" + B;
//    }
//
//    public void setPosition(double x, double y) {
//        this.position = new Point(x, y);
//    }

}
