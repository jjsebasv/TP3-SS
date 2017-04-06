package models;

/**
 * Created by sebastian on 4/6/17.
 */
public class MassParticle extends DynamicParticle {

    private double mass;
    private double vx;
    private double vy;

    public MassParticle(int id, double radius, double rc, double x, double y, double vx, double vy, double mass) {
        this.mass = mass;
        this.vx = vx;
        this.vy = vy;
        super(id, radius, rc, x, y, angle, Math.sqrt( Math.pow(vx, 2) + Math.pow(vy, 2) ));
    }
}
