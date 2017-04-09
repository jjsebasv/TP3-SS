import brownianmotion.BrownianMotion;
import models.MassParticle;
import models.Particle;

import java.io.*;
import java.util.*;


/**
 * Created by amounier on 3/12/17.
 */
public class main {

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

        // N - L - RC - n - v - T
        int N = 300;
        double L = 0.5;
        double vel = 0.1;
        double Rc = 0.1;
        double radius = 0.005;
        double mass = 0.1;
        int time = 60;

        List<Particle> particles = generateRandomBrownianMotionState(N, L, radius, Rc, vel, mass);

        BrownianMotion brownianMotion = new BrownianMotion(L, Rc, particles, time);

        List<List<Particle>> simulation = brownianMotion.simulate(time);

        brownianMotion.createSimulationFile(simulation, vel);

    }

    private static List<Particle> generateRandomBrownianMotionState(int cantParticles, double l, double radius, double rc, double vel, double mass) {
        List<Particle> particles = new ArrayList<>(cantParticles);
        Random r = new Random();
        MassParticle bigParticle = new MassParticle(cantParticles + 1, 0.05, rc, 0.25, 0.25, 0, 0, 100);
        particles.add(bigParticle);
        for (int i = 0; i < cantParticles; i++) {
            double x = l * r.nextDouble();
            double y = l * r.nextDouble();
            double vx = (Math.random() * 2 * vel) - vel;
            double vy = (Math.random() * 2 * vel) - vel;
            MassParticle particle = new MassParticle(i, radius, rc, x, y, vx, vy, mass);
            while (!isValid(particle, particles)) {
                x = l * r.nextDouble();
                y = l * r.nextDouble();
                particle = new MassParticle(i, radius, rc, x, y, vx, vy, mass);
            }
            particles.add(particle);
        }
        return particles;
    }


    public static boolean isValid(Particle p, List<? extends Particle> particles) {
        for (Particle p2 : particles) {
            if (Particle.getDistance(p, p2) < 0)
                return false;
        }
        return true;
    }
}



