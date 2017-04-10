package models;


import javafx.util.Pair;

/**
 * Created by sebastian on 4/6/17.
 */
public class MassParticle extends DynamicParticle {

    private double mass;
    private double vx;
    private double vy;

    public MassParticle(int id, double radius, double rc, double x, double y, double vx, double vy, double mass) {
        super(id, radius, rc, x, y, Math.atan2(vy, vx), Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2)));
        this.mass = mass;
        this.vx = vx;
        this.vy = vy;
    }

    public double getMass() {
        return mass;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public static double timeToCollision(MassParticle a, MassParticle b) {
        double sigma = a.getRadius() + b.getRadius();
        double deltaVX = b.getVx() - a.getVx();
        double deltaVY = b.getVy() - a.getVy();

        double deltaX = b.getPosition().x - a.getPosition().x;
        double deltaY = b.getPosition().y - a.getPosition().y;

        double vr = deltaVX * deltaX + deltaVY * deltaY;

        if (vr >= 0) {
            return Double.MAX_VALUE;
        }

        double vv = Math.pow(deltaVX, 2) + Math.pow(deltaVY, 2);
        double rr = Math.pow(deltaX, 2) + Math.pow(deltaY, 2);

        double d = Math.pow(vr, 2) - vv * (rr - Math.pow(sigma, 2));

        if (d < 0) {
            return Double.MAX_VALUE;
        }

        return -(vr + Math.sqrt(d)) / vv;
    }

    public double getVelocityModule() {
        return Math.sqrt(getVx() * getVx() + getVy() * getVy());
    }

    public double getVelocityAngle() {
        return Math.atan(getVy() / getVx());
    }


    @Override
    public String toString() {
        return getId() + "\t" + getPosition().x + "\t" + getPosition().y + "\t" + getRadius();
    }

    public boolean equals(MassParticle mp) {
        return mp != null && getId() == mp.getId();
    }

    public Pair<Double, Double> getVelAfterCollision(MassParticle particle) {

        double newVx = (((mass - particle.getMass()) * vx) + particle.getMass()*2*particle.getVx()) / (mass + particle.getMass());
        double newVy = (((mass - particle.getMass()) * vy) + particle.getMass()*2*particle.getVy()) / (mass + particle.getMass());

        return new Pair<>(newVx, newVy);

    }
}
