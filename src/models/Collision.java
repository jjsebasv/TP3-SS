package models;

import javafx.util.Pair;

/**
 * Created by amounier on 4/8/17.
 */
public class Collision {

    private double tc;
    private CollisionedWith collisionedWith;
    private Pair<MassParticle, MassParticle> particles;
    private double j = 0.0;

    public Collision(double tc, CollisionedWith collisionedWith, Pair<MassParticle, MassParticle> particles) {
        this.tc = tc;
        this.collisionedWith = collisionedWith;
        this.particles = particles;
    }

    public double getTc() {
        return tc;
    }

    public CollisionedWith getCollisionedWith() {
        return collisionedWith;
    }

    public Pair<MassParticle, MassParticle> getParticles() {
        return particles;
    }

    public void setTc(double tc) {
        this.tc = tc;
    }
}