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

    static {
        System.out.println("!!!TCUTILITY!!! TCUtilityMain class loaded (static init)");
    }

    public TCUtilityMain() {
        System.out.println("!!!TCUTILITY!!! Mod constructor entered");
        try {
            LOG.info("[TCUtility] Mod constructor starting");

            fileHandler = new ContentPackHandler(MODID, "assets/" + MODID, LOG,
                    name -> getRessourceLocation(name).map(Path::toAbsolutePath).orElse(null));
            LOG.info("[TCUtility] ContentPackHandler created");

            // Force-load TCTabs vor jedem Item.Properties.tab()-Call.
            TCTabs.touch();

            TCFluidsInit.initJsonFiles();
            TCItems.init();
            TCBlocks.init();
            TCBlocks.initJsonFiles();
            TCItems.initJsonFiles();
            LOG.info("[TCUtility] Pipeline produced {} block entries, {} item entries, "
                    + "{} fluid block entries", TCBlocks.blockEntries.size(),
                    TCItems.itemEntries.size(), TCFluidsInit.blockEntries.size());

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
