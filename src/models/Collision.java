package models;

import com.sun.tools.javac.util.Pair;

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


    public double getElasticCrash() {
        if(j != 0.0)
            return j;

        j = 2 * particles.fst.getMass() * particles.snd.getMass() * getVR(particles.fst, particles.snd);
        double sigma = getSigma();
        j /= sigma * (particles.fst.getMass() + particles.snd.getMass());
        return j;
    }

    public double getElasticCrashX() {
        return (getElasticCrash() * getDeltaX()) / getSigma();
    }

    public double getElasticCrashY() {
        return (getElasticCrash() * getDeltaY()) / getSigma();
    }

    public double getVR(MassParticle a, MassParticle b) {
        double deltaVX = b.getVx() - a.getVx();
        double deltaVY = b.getVy() - a.getVy();

        double deltaX = getDeltaX();
        double deltaY = getDeltaY();

        return deltaVX * deltaX + deltaVY * deltaY;
    }

    public double getDeltaX() {
        return particles.fst.getPosition().x - particles.snd.getPosition().x;
    }

    public double getDeltaY() {
        return particles.fst.getPosition().y - particles.snd.getPosition().y;
    }

    public double getSigma() {
        return particles.fst.getRadius() + particles.snd.getRadius();
    }
}