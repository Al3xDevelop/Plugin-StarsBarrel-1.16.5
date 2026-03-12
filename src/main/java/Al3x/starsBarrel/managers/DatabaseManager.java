package Al3x.starsBarrel.managers;

import Al3x.starsBarrel.StarsBarrel;
import Al3x.starsBarrel.models.Barrel;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class DatabaseManager {

    private final StarsBarrel plugin;
    private Connection connection;
    private final List<DropItem> dropItems = new ArrayList<>();

    public DatabaseManager(StarsBarrel plugin) {
        this.plugin = plugin;
        initDatabase();
        loadDropItems();
    }

    private void initDatabase() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, "database.db").getAbsolutePath());

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS barrels (" +
                                "id VARCHAR(36) PRIMARY KEY, " +
                                "world VARCHAR(255) NOT NULL, " +
                                "x INTEGER NOT NULL, " +
                                "y INTEGER NOT NULL, " +
                                "z INTEGER NOT NULL)"
                );

                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS drop_items (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "item_data TEXT NOT NULL, " +
                                "chance DOUBLE NOT NULL, " +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void saveBarrel(Barrel barrel) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR REPLACE INTO barrels (id, world, x, y, z) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, barrel.getId().toString());
            statement.setString(2, barrel.getWorld());
            statement.setInt(3, barrel.getX());
            statement.setInt(4, barrel.getY());
            statement.setInt(5, barrel.getZ());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeBarrel(Location location) {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM barrels WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
            statement.setString(1, location.getWorld().getName());
            statement.setInt(2, location.getBlockX());
            statement.setInt(3, location.getBlockY());
            statement.setInt(4, location.getBlockZ());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isBarrel(Location location) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM barrels WHERE world = ? AND x = ? AND y = ? AND z = ?")) {
            statement.setString(1, location.getWorld().getName());
            statement.setInt(2, location.getBlockX());
            statement.setInt(3, location.getBlockY());
            statement.setInt(4, location.getBlockZ());
            ResultSet result = statement.executeQuery();
            return result.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Barrel> loadAllBarrels() {
        List<Barrel> barrels = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT * FROM barrels")) {

            while (result.next()) {
                UUID id = UUID.fromString(result.getString("id"));
                String world = result.getString("world");
                int x = result.getInt("x");
                int y = result.getInt("y");
                int z = result.getInt("z");

                barrels.add(new Barrel(id, world, x, y, z));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return barrels;
    }


    public static class DropItem {
        private final int id;
        private final ItemStack item;
        private double chance;
        private final Date createdAt;

        public DropItem(int id, ItemStack item, double chance, Date createdAt) {
            this.id = id;
            this.item = item;
            this.chance = chance;
            this.createdAt = createdAt;
        }

        public int getId() { return id; }
        public ItemStack getItem() { return item; }
        public double getChance() { return chance; }
        public void setChance(double chance) { this.chance = chance; }
        public Date getCreatedAt() { return createdAt; }
    }

    private String itemStackToBase64(ItemStack item) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        dataOutput.writeObject(item);
        dataOutput.close();
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    private ItemStack itemStackFromBase64(String data) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        ItemStack item = (ItemStack) dataInput.readObject();
        dataInput.close();
        return item;
    }

    public void loadDropItems() {
        dropItems.clear();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT * FROM drop_items")) {

            while (result.next()) {
                int id = result.getInt("id");
                String itemData = result.getString("item_data");
                double chance = result.getDouble("chance");

                ItemStack item = itemStackFromBase64(itemData);
                if (item != null) {
                    DropItem dropItem = new DropItem(id, item, chance, new Date());
                    dropItems.add(dropItem);
                }
            }
            plugin.getLogger().info("Загружено " + dropItems.size() + " предметов дропа");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveDropItem(ItemStack item, double chance) {
        try {
            String itemData = itemStackToBase64(item);

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO drop_items (item_data, chance) VALUES (?, ?)")) {
                statement.setString(1, itemData);
                statement.setDouble(2, chance);
                statement.executeUpdate();
            }

            loadDropItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDropItem(int id, double chance) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE drop_items SET chance = ? WHERE id = ?")) {
            statement.setDouble(1, chance);
            statement.setInt(2, id);
            statement.executeUpdate();

            loadDropItems();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteDropItem(int id) {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM drop_items WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();

            loadDropItems();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAllDropItems() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM drop_items");
            dropItems.clear();
            plugin.getLogger().info("Все предметы дропа удалены");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<DropItem> getAllDropItems() {
        return new ArrayList<>(dropItems);
    }
}