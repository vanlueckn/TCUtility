package com.troblecodings.tcutility;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.troblecodings.contentpacklib.ContentPackHandler;
import com.troblecodings.tcutility.init.TCBlocks;
import com.troblecodings.tcutility.init.TCTabs;
import com.troblecodings.tcutility.init.TCFluidsInit;
import com.troblecodings.tcutility.init.TCItems;

import net.minecraftforge.fml.common.Mod;

@Mod(TCUtilityMain.MODID)
public class TCUtilityMain {

    public static final String MODID = "tcutility";
    public static final Logger LOG = LogManager.getLogger();
    public static ContentPackHandler fileHandler;

    private static FileSystem fileSystemCache = null;

    public TCUtilityMain() {
        // ContentPackHandler in 1.16.5 registriert NetworkContentPackHandler
        // intern und haengt sich auf Client-Setup an die ResourcePackList,
        // sodass kein separater "new NetworkContentPackHandler" mehr
        // notwendig ist.
        fileHandler = new ContentPackHandler(MODID, "assets/" + MODID, LOG,
                name -> getRessourceLocation(name).map(Path::toAbsolutePath).orElse(null));

        // Mod-Bus-Registration der Registry-Subscriber. Die @Mod.EventBusSubscriber
        // Annotationen koennten das auch tun, aber explizite Registrierung passt
        // besser zur JSON-getriebenen Pipeline und macht die Reihenfolge sichtbar.
        // Force-load TCTabs, sodass die Custom-CreativeModeTabs in TABS[]
        // landen, bevor irgendein Item via .tab() darauf zeigt. Wuerde im
        // Normalfall auch implizit beim ersten groupFor()-Call passieren --
        // hier tun wir's eagerly, damit es deterministisch und vor jeder
        // moeglichen Inventory-Cache-Initialisierung passiert.
        TCTabs.touch();

        LOG.info("[TCUtility] Mod constructor starting -- pipeline init");
        TCFluidsInit.initJsonFiles();
        TCItems.init();
        TCBlocks.init();
        TCBlocks.initJsonFiles();
        TCItems.initJsonFiles();
        LOG.info("[TCUtility] Pipeline produced {} block entries, {} item entries, "
                + "{} fluid block entries", TCBlocks.blockEntries.size(),
                TCItems.itemEntries.size(), TCFluidsInit.blockEntries.size());

        // 1.19.2-Modul-System scannt @Mod.EventBusSubscriber bei Init-Klassen
        // in Subpackages nicht zuverlaessig auto-discovern -- manuelle
        // Mod-Bus-Registrierung holt das nach.
        final var modBus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get()
                .getModEventBus();
        modBus.register(TCBlocks.class);
        modBus.register(TCItems.class);
        modBus.register(TCFluidsInit.class);
        LOG.info("[TCUtility] Subscribed TCBlocks/TCItems/TCFluidsInit on mod bus");
    }

    private static Optional<Path> getRessourceLocation(final String location) {
        String filelocation = location;
        final URL url = TCBlocks.class.getResource("/assets/" + MODID);
        try {
            if (url != null) {
                final URI uri = url.toURI();
                if ("file".equals(uri.getScheme())) {
                    if (!location.startsWith("/")) {
                        filelocation = "/" + filelocation;
                    }
                    final URL resource = TCBlocks.class.getResource(filelocation);
                    if (resource == null) {
                        return Optional.empty();
                    }
                    return Optional.of(Paths.get(resource.toURI()));
                } else {
                    if (!"jar".equals(uri.getScheme())) {
                        return Optional.empty();
                    }
                    if (fileSystemCache == null) {
                        fileSystemCache = FileSystems.newFileSystem(uri, Collections.emptyMap());
                    }
                    return Optional.of(fileSystemCache.getPath(filelocation));
                }
            }
        } catch (final IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
