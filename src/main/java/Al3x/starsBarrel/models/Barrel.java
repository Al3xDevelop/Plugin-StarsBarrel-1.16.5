package Al3x.starsBarrel.models;

import org.bukkit.Location;

import java.util.UUID;

public class Barrel {
    private final UUID id;
    private final Location location;
    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public Barrel(Location location) {
        this.id = UUID.randomUUID();
        this.location = location;
        this.world = location.getWorld().getName();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    public Barrel(UUID id, String world, int x, int y, int z) {
        this.id = id;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.location = null;
    }

    public UUID getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}