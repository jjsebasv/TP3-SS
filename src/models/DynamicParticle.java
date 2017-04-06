package models;

/**
 * Created by sebastian on 3/18/17.
 */
public class DynamicParticle extends Particle {

    private double angle;
    private double velocity;

    public DynamicParticle(int id, double radius, double rc, double x, double y, double angle, double velocity) {
        super(id, radius, rc, x, y);
        this.angle = angle;
        this.velocity = velocity;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getAngle() {
        return angle;
    }

    @Override
    public String toString() {
        double aux = (angle + 2*Math.PI )*255/360;

        return getId() + "\t" + getPosition().x + "\t" + getPosition().y + "\t0.15\t" + angle + "\t" + velocity + "\t" +
                (Math.cos(angle) * velocity) + "\t" + (Math.sin(angle) * velocity) + "\t" +
                aux + "\t" + angle + "\t" + aux;
    }
}
