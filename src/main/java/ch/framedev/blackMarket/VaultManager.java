package ch.framedev.blackMarket;



/*
 * ch.framedev.blackMarket
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 25.08.2024 00:05
 */

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Level;

import static org.bukkit.Bukkit.getServer;

public class VaultManager {

    private Economy economy;

    public VaultManager() {
        if(!setupEconomy())
            BlackMarket.getPlugin(BlackMarket.class).getLogger().log(Level.SEVERE, "Failed to initialize Economy!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public Economy getEconomy() {
        return economy;
    }
}
