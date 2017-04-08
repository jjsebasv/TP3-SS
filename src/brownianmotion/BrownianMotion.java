package brownianmotion;

import cellindexmethod.CellIndexMethod;
import models.MassParticle;
import models.Particle;
import sun.applet.resources.MsgAppletViewer_sv;

import java.util.*;

/**
 * Created by sebastian on 4/6/17.
 */
public class BrownianMotion extends CellIndexMethod {

    private Map<Double, List<? extends Particle>> simulation = new HashMap<>();

    public BrownianMotion(double l, double rc, List<? extends Particle> particles) {
        super(l, rc, particles, false);
        simulation.put(0.0, particles);
    }

    public void simulate (double T) {
        double t = 0.0;
        while (t < T) {
            reloadMatrix(simulation.get(t));
            t = calculateNextTc(t);
        }
    }

    // Private functions

    private double calculateNextTc(double to) {
        double tc = -1;
        Map<Particle, Set<Particle>> neighbours = findNeighbors(simulation.get(to));
        List<Particle> nextStep = new ArrayList<>();

        /**
         * Integer represents the way to be modified
         * If 0, should be modified as in collision with horizontal wall
         * If 1, should be modified as in collision with vertical wall
         * If 2, should be modified as in collision with other particle
         *
         */
        Map<MassParticle, Integer> modifiedParticles = new HashMap<>();
        Map<MassParticle, MassParticle> collisionedWith = new HashMap<>();

        for (Particle p : simulation.get(to)) {
            MassParticle mp = (MassParticle) p;
            double auxTc;
            int auxInt = 0;

            double horizontalTc = getHorizontalWallCollisionTime(mp);
            double verticalTc = getVerticlaWallCollisionTime(mp);
            double particleTc = -1;

            // I know it only has 1 key
            Map<Double, MassParticle> auxCollisonParticles = getParticleCollisionTime(mp, neighbours.get(mp));
            for ( double t : auxCollisonParticles.keySet()) {
                particleTc = t;
            }

            auxTc = horizontalTc;
            if (verticalTc < auxTc) {
                auxTc = verticalTc;
                auxInt = 1;
            }
            if (particleTc != -1 && particleTc < auxTc) {
                auxTc = particleTc;
                auxInt = 2;
                collisionedWith.put(mp, auxCollisonParticles.get(auxTc));
            }

            if (tc == -1) {
                // First case
                tc = auxTc;
                modifiedParticles.put(mp, auxInt);
            } else if (auxTc == tc) {
                // We found an exact same tc from other particle - Chances are minuscules.
                modifiedParticles.put(mp, auxInt);
            } else if (auxTc < tc) {
                // All the to-be-modified particles would evolve normally
                tc = auxTc;
                modifiedParticles.clear();
                collisionedWith.clear();
                modifiedParticles.put(mp, auxInt);
                if (auxInt == 2) collisionedWith.put(mp, auxCollisonParticles.get(tc));
            }
            // Other case would be if the particles' tc is higher than the calculated, would directly evolve normally
        }

        double dt = tc - to;

        // Modify the ones whose v changes
        for (MassParticle particle : modifiedParticles.keySet()) {
            double newX = particle.getPosition().x + particle.getVx() * dt;
            double newY = particle.getPosition().y + particle.getVy() * dt;
            double newVx = particle.getVx();
            double newVy = particle.getVy();

            int auxInt = modifiedParticles.get(particle);
            if (auxInt == 0) {
                newVy = -newVy;
            } else if (auxInt == 1) {
                newVx = -newVx;
            } else {
                newVx = newVx + MassParticle.getJx(particle, collisionedWith.get(particle))/particle.getMass();
                newVy = newVy + MassParticle.getJy(particle, collisionedWith.get(particle))/particle.getMass();
            }

            nextStep.add(new MassParticle(particle.getId(), particle.getRadius(), particle.getRc(), newX, newY, newVx, newVy, particle.getMass()));
        }

        // Modify the rest
        for (Particle particle : simulation.get(to)) {
            MassParticle mp = (MassParticle) particle;
            if (modifiedParticles.containsKey(mp)) continue;
            double newX = mp.getPosition().x + mp.getVx() * dt;
            double newY = mp.getPosition().y + mp.getVy() * dt;
            nextStep.add(new MassParticle(mp.getId(), mp.getRadius(), mp.getRc(), newX, newY, mp.getVx(), mp.getVy(), mp.getMass()));
        }

        simulation.put(tc, nextStep);
        return tc;
    }


    private Map<Double, MassParticle> getParticleCollisionTime(MassParticle particle, Set<Particle> neighbours) {
        double tc = -1;
        MassParticle colissionedWith = particle;

        for (Particle n : neighbours) {
            MassParticle neighbour = (MassParticle) n;
            double d = MassParticle.getD(particle, neighbour);
            double dVdR = MassParticle.getDvDr(particle, neighbour);
            double tcAux;

            if (dVdR < 0 && d >= 0) {
                tcAux = - (dVdR + Math.sqrt(d)) / MassParticle.getDvDv(particle, neighbour);
                if (tc == -1 || tcAux < tc) {
                    tc = tcAux;
                    colissionedWith = neighbour;
                }
            }
        }

        Map<Double, MassParticle> aux = new HashMap<>();
        aux.put(tc, colissionedWith);

        return aux;

    }

    private double getVerticlaWallCollisionTime(MassParticle particle) {
        if (particle.getVx() > 0) {
            return (this.l - particle.getRadius() - particle.getPosition().x) / particle.getVx();
        } else {
            return (particle.getRadius() - particle.getPosition().x) / particle.getVx();
        }
    }

    private double getHorizontalWallCollisionTime(MassParticle particle) {
        if (particle.getVy() > 0) {
            return (this.l - particle.getRadius() - particle.getPosition().y) / particle.getVy();
        } else {
            return (particle.getRadius() - particle.getPosition().y) / particle.getVy();
        }
    }
}
