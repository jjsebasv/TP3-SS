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
}
