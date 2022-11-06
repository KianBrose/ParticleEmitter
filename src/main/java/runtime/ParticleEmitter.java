package runtime;

import emitters.EmitterLifetime;
import emitters.EmitterRate;
import emitters.EmitterShape;
import emitters.init.EmitterInitialization;
import emitters.init.EmitterLocalSpace;
import net.hollowcube.mql.foreign.Query;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import org.jetbrains.annotations.NotNull;
import particle.ParticleAppearanceTinting;
import particle.ParticleInitialSpeed;
import particle.ParticleLifetime;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParticleEmitter extends ParticleInterface {
    private Set<Particle> particles = new HashSet<>();
    private final int updatesPerSecond;

    private double emitter_age;
    private final double emitter_random_1;
    private final double emitter_random_2;
    private final double emitter_random_3;
    private final double emitter_random_4;

    private final EmitterLocalSpace local_space;
    private final EmitterInitialization initialization;

    private final EmitterLifetime lifetime;
    private final EmitterRate rate;
    private final EmitterShape shape;

    private final ParticleAppearanceTinting particleColour;
    private final ParticleInitialSpeed particleSpeed;
    private final ParticleLifetime particleLifetime;

    private EmitterLifetime.LifetimeState state = EmitterLifetime.LifetimeState.ALIVE;

    private Point offset = Vec.ZERO;
    private float yaw;

    @Query
    public int particle_count() {
        return particles.size();
    }
    @Query
    public double emitter_age() {
        return emitter_age;
    }
    @Query
    public double emitter_random_1() {
        return emitter_random_1;
    }
    @Query
    public double emitter_random_2() {
        return emitter_random_2;
    }
    @Query
    public double emitter_random_3() {
        return emitter_random_3;
    }
    @Query
    public double emitter_random_4() {
        return emitter_random_4;
    }
    @Query
    public double particle_age() {
        return 0;
    }
    @Query
    public double particle_random_1() {
        return 0;
    }
    @Query
    public double particle_random_2() {
        return 0;
    }
    @Query
    public double particle_random_3() {
        return 0;
    }
    @Query
    public double particle_random_4() {
        return 0;
    }

    public void setPosition(Point offset) {
        this.offset = offset;
    }

    public void setRotation(float yaw) {
        this.yaw = yaw;
    }

    public ParticleEmitter(int updatesPerSecond, EmitterInitialization initialization, EmitterLocalSpace local_space,
                           EmitterLifetime lifetime, EmitterRate rate, EmitterShape shape,
                           ParticleInitialSpeed particleSpeed, ParticleAppearanceTinting particleColour, ParticleLifetime particleLifetime) {
        this.emitter_age = 0;

        this.emitter_random_1 = Math.random();
        this.emitter_random_2 = Math.random();
        this.emitter_random_3 = Math.random();
        this.emitter_random_4 = Math.random();

        this.initialization = initialization;
        this.local_space = local_space;

        this.lifetime = lifetime;
        this.rate = rate;
        this.shape = shape;
        this.particleSpeed = particleSpeed;
        this.particleColour = particleColour;
        this.particleLifetime = particleLifetime;

        this.updatesPerSecond = updatesPerSecond;

        initialization.initialize(this);
    }

    public void reset() {
        emitter_age = 0;
        particles.clear();
        initialization.initialize(this);
    }

    @Override
    public int updatesPerSecond() {
        return updatesPerSecond;
    }

    public @NotNull Collection<ParticlePacket> tick() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        emitter_age += 1.0/updatesPerSecond;

        initialization.update(this);
        // particles.forEach(Particle::tick);
        // particles = particles.stream().filter(particle -> particle.isAlive()).collect(Collectors.toSet());

        this.state = lifetime.getState(this);
        if (state == EmitterLifetime.LifetimeState.DEAD || state == EmitterLifetime.LifetimeState.INACTIVE) {
            return List.of();
        }

        boolean canCreateParticle = rate.canEmit(this);

        if (canCreateParticle) {
            Vec position = rotateAroundOrigin(yaw, shape.emitPosition(this)).add(this.offset);
            // Vec direction = shape.emitDirection(this);
            // if (direction == null) direction = Vec.ZERO;
            // direction = direction.rotateFromView(yaw, 0);

            Particle particle = new Particle(position, Vec.ZERO, this, particleColour, particleLifetime);
            // particles.add(particle);
            return List.of(particle.getPacket());
        }

        return List.of();
    }

    private Vec rotateAroundOrigin(float yaw, Vec emitPosition) {
        return emitPosition.rotateAroundY(Math.toRadians(yaw));
    }

    @Override
    public String toString() {
        return "ParticleEmitter{" +
                "emitter_age=" + emitter_age +
                ", emitter_random1=" + emitter_random_1 +
                ", emitter_random2=" + emitter_random_2 +
                ", emitter_random3=" + emitter_random_3 +
                ", emitter_random4=" + emitter_random_4 +
                ", local_space=" + local_space +
                ", initialization=" + initialization +
                ", lifetime=" + lifetime +
                ", rate=" + rate +
                ", shape=" + shape +
                ", particleColour=" + particleColour +
                ", particleSpeed=" + particleSpeed +
                '}';
    }

    public EmitterLifetime.LifetimeState status() {
        return state;
    }
}
