package models;

/**
 * Created by sebastian on 4/6/17.
 */
public class MassParticle extends DynamicParticle {

    private double mass;
    private double vx;
    private double vy;

    public MassParticle(int id, double radius, double rc, double x, double y, double vx, double vy, double mass) {
        super(id, radius, rc, x, y, Math.atan2(vy, vx), Math.sqrt( Math.pow(vx, 2) + Math.pow(vy, 2) ));
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

    // Static function

    public static double getJx(MassParticle p1, MassParticle p2) {
        return (getJ(p1, p2) * (p1.getPosition().x - p2.getPosition().x)) / getSigma(p1, p2);
    }

    public static double getJy(MassParticle p1, MassParticle p2) {
        return (getJ(p1, p2) * (p1.getPosition().y - p2.getPosition().y)) / getSigma(p1, p2);
    }

    public static double getJ(MassParticle p1, MassParticle p2) {
        return (2 * p1.getMass() * p2.getMass() * getDvDr(p1, p2)) / (getSigma(p1, p2) * (p1.getMass() + p2.getMass()));
    }

    public static double getD(MassParticle p1, MassParticle p2) {
        return Math.pow(getDvDr(p1, p2), 2) - getDvDv(p1, p2) * (getDrDr(p1, p2) - Math.pow(getSigma(p1, p2), 2));
    }

    public static double getSigma(MassParticle p1, MassParticle p2) {
        return p1.getRadius() + p2.getRadius();
    }

    /**
     * The difference is particle 1 - particle 2
     */
    public static double getDvDr(MassParticle p1, MassParticle p2) {
        return (p1.getVx() - p2.getVx()) * (p1.getPosition().x - p2.getPosition().x) +
                (p1.getVy() - p2.getVy()) * (p1.getPosition().y - p2.getPosition().y);
    }

    public static double getDvDv(MassParticle p1, MassParticle p2) {
        return Math.pow(p1.getVx() - p2.getVx(), 2) + Math.pow(p1.getVy() - p2.getVy(), 2);
    }

    public static double getDrDr(MassParticle p1, MassParticle p2) {
        return Math.pow(p1.getPosition().x - p2.getPosition().x, 2) + Math.pow(p1.getPosition().y - p2.getPosition().y, 2);
    }

}
