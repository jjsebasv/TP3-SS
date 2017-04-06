import models.DynamicParticle;
import models.Particle;
import offlatice.OffLaticeAutomaton;

import java.io.*;
import java.util.*;


/**
 * Created by amounier on 3/12/17.
 */
public class main {

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

        // N - L - RC - n - v - T
        int N = 300;
        double L = 7.0;
        double n = 0.1;
        double vel = 0.3;
        double Rc = 1.0;
        int Iteraciones = 1500;

        List<Particle> particles = generateRandomOffLaticeState(N, L, 0, Rc, vel);
        executeOffLaticeSimulation(N, L, Rc, n, vel, Iteraciones, particles);
    }


    public static void executeOffLaticeSimulation(int cantParticles, double L, double rc, double n, double vel, int TMax, List<Particle> particles) {
        generateRepetitions(1, particles, rc, n, vel, TMax, L);
    }

    private static List<Particle> generateRandomOffLaticeState(int cant_particles, double l, double radius, double rc, double vel) {
        List<Particle> particles = new ArrayList<>(cant_particles);
        Random r = new Random();
        for (int i = 0; i < cant_particles; i++) {
            double x = l * r.nextDouble();
            double y = l * r.nextDouble();
            double angle = (Math.random() * 2 * Math.PI) - Math.PI;
            DynamicParticle particle = new DynamicParticle(i, radius, rc, x, y, angle, vel);
            while (!isValid(particle, particles)) {
                x = l * r.nextDouble();
                y = l * r.nextDouble();
                particle = new DynamicParticle(i, radius, rc, x, y, angle, vel);
            }
            particles.add(particle);
        }
        return particles;
    }

    private static void generateRepetitions(int repetitions, List<Particle> particles, double rc, double n, double vel, int TMax, double L) {
        Map<Integer, Map<Integer, Double>> vaRepetitions = new HashMap<>();
        double density = particles.size() / Math.pow(L, 2);
        for (int i = 0; i < repetitions; i++) {
            OffLaticeAutomaton offLaticeAutomaton = new OffLaticeAutomaton(L, rc, particles, true, n, vel);
            offLaticeAutomaton.simulate(TMax, i == 0, n, density);
            vaRepetitions.put(i, offLaticeAutomaton.getvaEvolutions());
        }
    }


    public static boolean isValid(Particle p, List<? extends Particle> particles) {
        for (Particle p2 : particles) {
            if (Particle.getDistance(p, p2) < 0)
                return false;
        }
        return true;
    }
}



