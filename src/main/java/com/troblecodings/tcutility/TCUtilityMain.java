package com.troblecodings.tcutility;

import java.nio.file.Path;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.troblecodings.contentpacklib.ContentPackHandler;
import com.troblecodings.tcutility.init.TCBlocks;
import com.troblecodings.tcutility.init.TCFluidsInit;
import com.troblecodings.tcutility.init.TCItems;
import com.troblecodings.tcutility.init.TCTabs;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod(TCUtilityMain.MODID)
public class TCUtilityMain {

    public static final String MODID = "tcutility";
    public static final Logger LOG = LogManager.getLogger();
    public static ContentPackHandler fileHandler;

    static {
        System.out.println("!!!TCUTILITY!!! TCUtilityMain class loaded (static init)");
    }

    public TCUtilityMain() {
        System.out.println("!!!TCUTILITY!!! Mod constructor entered");
        try {
            LOG.info("[TCUtility] Mod constructor starting");

            fileHandler = new ContentPackHandler(MODID, "assets/" + MODID, LOG,
                    name -> getRessourceLocation(name).orElse(null));
            LOG.info("[TCUtility] ContentPackHandler created");

            // Force-load TCTabs vor jedem Item.Properties.tab()-Call.
            TCTabs.touch();

            TCFluidsInit.initJsonFiles();
            TCItems.init();
            TCBlocks.init();
            TCBlocks.initJsonFiles();
            TCItems.initJsonFiles();
            LOG.info("[TCUtility] Pipeline parsed JSON specs; "
                    + "all Block/Item construction is deferred to RegisterEvent");

            final var modBus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get()
                    .getModEventBus();
            modBus.register(TCBlocks.class);
            modBus.register(TCItems.class);
            modBus.register(TCFluidsInit.class);
            LOG.info("[TCUtility] Subscribed TCBlocks/TCItems/TCFluidsInit on mod bus");
            System.out.println("!!!TCUTILITY!!! Mod constructor completed successfully");
        } catch (final Throwable t) {
            System.out.println("!!!TCUTILITY!!! Mod constructor FAILED: " + t);
            t.printStackTrace(System.out);
            LOG.error("[TCUtility] Mod constructor failed", t);
            throw t;
        }
    }

    /**
     * 1.19+ verwendet einen Modul-Classloader, der Mod-Resources unter dem
     * {@code union:}-URL-Schema rausgibt -- {@code Class.getResource} +
     * {@code Paths.get(URI)} schlaegt damit fehl. Stattdessen besorgen wir
     * uns die Pfade direkt aus dem Forge-{@code IModFile}, das fuer alle
     * Schema-Varianten einen NIO-{@code Path} liefert.
     */
    private static Optional<Path> getRessourceLocation(final String location) {
        try {
            final var modFileInfo = ModList.get().getModFileById(MODID);
            if (modFileInfo == null) {
                return Optional.empty();
            }
            final Path resolved = modFileInfo.getFile().findResource(location);
            if (resolved == null) {
                return Optional.empty();
            }
            return Optional.of(resolved);
        } catch (final Exception e) {
            LOG.error("[TCUtility] Failed to resolve mod resource '{}'", location, e);
            return Optional.empty();
        }
    }
}
