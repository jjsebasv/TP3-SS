package brownianmotion;

import cellindexmethod.CellIndexMethod;
import com.sun.tools.javac.util.Pair;
import models.Collision;
import models.CollisionedWith;
import models.MassParticle;
import models.Particle;
import sun.applet.resources.MsgAppletViewer_sv;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by sebastian on 4/6/17.
 */
public class BrownianMotion extends CellIndexMethod {

    private List<List<Particle>> simulation = new ArrayList<>();

    private final double TIME_GRANULARITY = 0.0005;

    private final double EPSILON = 0.000001;

    public BrownianMotion(double l, double rc, List<Particle> particles) {
        super(l, rc, particles, false);
        simulation.add(particles);
    }

    public List<List<Particle>> simulate(double time) {
        double t = 0.0;
        int cant = 0;
        while (t < time) {
            if(cant % 1200 == 0)
                System.out.println(cant/1200 + " %");
            cant++;

            List<Particle> currentState = simulation.get(simulation.size() - 1);
            reloadMatrix(currentState);

            Collision collision = getNextCollision(currentState);
            double tc = collision.getTc();
            while (tc > TIME_GRANULARITY) {
                List<Particle> intermediateState = evolveSystem(currentState, collision);
                t += TIME_GRANULARITY;
                simulation.add(intermediateState);
                reloadMatrix(intermediateState);
                currentState = intermediateState;
                tc -= TIME_GRANULARITY;
                collision.setTc(tc);
                cant++;
                if(cant % 1200 == 0)
                    System.out.println(cant/1200 + " %");
            }

            currentState = simulation.get(simulation.size() - 1);
            List<Particle> nextState = evolveSystem(currentState, collision);
            t += TIME_GRANULARITY;
            simulation.add(nextState);
        }

        return simulation;
    }

    // Private functions

    private Collision getNextCollision(List<Particle> particles) {

        Map<Particle, Set<Particle>> neighbours = findNeighbors(particles);
        Collision firstCollision = null;

        for (Particle p : particles) {
            MassParticle mp = (MassParticle) p;

            Collision collision = getMinTcOfPaticle(mp, neighbours.get(mp));

            if (particles.get(0) == p) {
                firstCollision = collision;
            } else if (firstCollision.getTc() > collision.getTc()) {
                firstCollision = collision;
            }
        }

        return firstCollision;
    }

    private List<Particle> evolveSystem(List<Particle> particles, Collision collision) {

        ArrayList<Particle> nextParticles = new ArrayList<>();

        for (Particle p : particles) {
            MassParticle mp = (MassParticle) p;
            double newX = mp.getPosition().x + mp.getVx() * TIME_GRANULARITY;
            double newY = mp.getPosition().y + mp.getVy() * TIME_GRANULARITY;

            if (newX >= l) {
                newX -= (newX - l) + EPSILON + mp.getRadius();
            }
            if (newY >= l) {
                newY -= (newY - l) + EPSILON + mp.getRadius();
            }
            if (newX < 0) {
                newX += (newX * -1) + EPSILON + mp.getRadius();
            }
            if (newY < 0) {
                newY += (newY * -1) + EPSILON + mp.getRadius();
            }

            Pair<Double, Double> newVel = handleCollision(mp, collision);

            MassParticle newMp = new MassParticle(mp.getId(), mp.getRadius(), mp.getRc(), newX, newY, newVel.fst, newVel.snd, mp.getMass());

            checkAceleration(newMp);

            nextParticles.add(newMp);
        }

        return nextParticles;
    }

    private Pair<Double, Double> handleCollision(MassParticle mp, Collision collision) {
        double newVx = mp.getVx();
        double newVy = mp.getVy();

        if (TIME_GRANULARITY >= collision.getTc() &&
                (mp.equals(collision.getParticles().fst) || mp.equals(collision.getParticles().snd))) {
            switch (collision.getCollisionedWith()) {
                case HORIZONTAL_WALL:
                    newVy = mp.getVy() * -1;
                    newVx = mp.getVx();
                    break;
                case VERTICAL_WALL:
                    newVy = mp.getVy();
                    newVx = mp.getVx() * -1;
                    break;
                case PARTICLE:
                    if (mp.equals(collision.getParticles().fst)) {
                        newVx += collision.getElasticCrashX() / collision.getParticles().fst.getMass();
                        newVy += collision.getElasticCrashY() / collision.getParticles().fst.getMass();
                    } else if (mp.equals(collision.getParticles().snd)){
                        newVx += -1 * collision.getElasticCrashX() / collision.getParticles().snd.getMass();
                        newVy += -1 * collision.getElasticCrashY() / collision.getParticles().snd.getMass();
                    }
                    break;
            }
        }

        return new Pair<>(newVx, newVy);
    }

    private Pair<Double, MassParticle> getParticleCollisionTime(MassParticle particle, Set<Particle> neighbours) {
        MassParticle colissionedWith = particle;
        double minTc = -1;

        for (Particle n : neighbours) {
            MassParticle neighbour = (MassParticle) n;
            if(particle.equals(neighbour))
                continue;

            double timeToCol = MassParticle.timeToCollision(particle, neighbour);
            if(minTc == -1) {
                minTc = timeToCol;
                colissionedWith = neighbour;
            } else if (minTc > timeToCol) {
                minTc = timeToCol;
                colissionedWith = neighbour;
            }
        }

        return new Pair<>(minTc, colissionedWith);

    }

    private double getVerticlaWallCollisionTime(MassParticle particle) {
        if (particle.getVx() > 0) {
            return (this.l - particle.getRadius() - particle.getPosition().x) / particle.getVx();
        } else if (particle.getVx() < 0) {
           return (particle.getRadius() - particle.getPosition().x) / particle.getVx();
        }
        return Double.MAX_VALUE;
    }

    private double getHorizontalWallCollisionTime(MassParticle particle) {
        if (particle.getVy() > 0) {
            return (this.l - particle.getRadius() - particle.getPosition().y) / particle.getVy();
        } else if (particle.getVy() < 0) {
            return (particle.getRadius() - particle.getPosition().y) / particle.getVy();
        }
        return Double.MAX_VALUE;
    }

    private Collision getMinTcOfPaticle(MassParticle mp, Set<Particle> neighbours) {
        ArrayList<Double> tcs = new ArrayList<>();

        tcs.add(getVerticlaWallCollisionTime(mp));
        tcs.add(getHorizontalWallCollisionTime(mp));

        Pair<Double, MassParticle> particleTc = getParticleCollisionTime(mp, neighbours);
        tcs.add(particleTc.fst);

        double minTc = Collections.min(tcs);

        if (tcs.indexOf(minTc) == CollisionedWith.VERTICAL_WALL.ordinal()) {
            return new Collision(minTc, CollisionedWith.VERTICAL_WALL, new Pair<>(mp, null));
        } else if (tcs.indexOf(minTc) == CollisionedWith.HORIZONTAL_WALL.ordinal()) {
            return new Collision(minTc, CollisionedWith.HORIZONTAL_WALL, new Pair<>(mp, null));
        } else {
            return new Collision(minTc, CollisionedWith.PARTICLE, new Pair<>(mp, particleTc.snd));
        }
    }

    private void checkAceleration(MassParticle mp) {
        double prevVx = mp.getVx();
        double prevVy = mp.getVy();

        double velModule = mp.getVelocityModule();
        if(velModule > 0.11 || velModule < 0.09) {
            double newVx = 0.1 * Math.cos(mp.getVelocityAngle());
            double newVy = 0.1 * Math.sin(mp.getVelocityAngle());

            if ((prevVx < 0 && newVx > 0) || (prevVx > 0 && newVx < 0))
                newVx *= -1;
            if ((prevVy < 0 && newVy > 0) || (prevVy > 0 && newVy < 0))
                newVy *= -1;

            mp.setVx(newVx);
            mp.setVy(newVy);
        }

    }
}
