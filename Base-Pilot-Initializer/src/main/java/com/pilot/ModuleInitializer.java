package com.pilot;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import intf.PilotServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import xml.ModuleConfig;
import xml.ModulesConfig;
import xml.SystemConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class ModuleInitializer implements ApplicationRunner {

    @Autowired
    private ApplicationContext context;

    private SystemConfig systemConfig;
    @Autowired
    private List<PilotServices<?>> pilotServices;


    @Override
    public void run(ApplicationArguments args) throws Exception {


        System.out.println("==== Registered PilotServices Beans ====");
        context.getBeansOfType(PilotServices.class)
                .forEach((name, bean) -> System.out.println(name + " -> " + bean.getClass().getName()));

        String absoluteFilePath = args.getSourceArgs()[0];
            try {
                XmlMapper xmlMapper = new XmlMapper();
                systemConfig = xmlMapper.readValue(new File(absoluteFilePath), SystemConfig.class);
            }
            catch (MismatchedInputException e) {
                throw new RuntimeException("XML-Struktur passt nicht zum Datenmodell: " + e.getMessage(), e);
            }
            catch (JsonProcessingException e) {
                System.err.println("Parsing-Fehler in Zeile " + e.getLocation().getLineNr() +
                        ", Spalte " + e.getLocation().getColumnNr());
                throw e;
            }
            catch (IOException e) {
                throw new RuntimeException("Fehler beim Lesen der XML-Datei: " + e.getMessage(), e);
            }
        if (systemConfig == null) {
            throw new RuntimeException("Es sind Keine Module in die Konfigurationsdatei definiret");
        }

        // 1. Discovery
        System.out.println("========= MODULES DISCOVERY =========");
        ModulesConfig modulesConfig = systemConfig.getModules();
        for ( ModuleConfig module : modulesConfig.getModules()) {
            System.out.println("[INFO].... Modul: " + module.getName()+ " - enabled:"+module.isEnabled());
        }

        // 1. Loading
        System.out.println("========= MODULES LOADED =========");
        String classpath = System.getProperty("java.class.path");
        for (String path : classpath.split(File.pathSeparator)) {
            if (path.endsWith(".jar")) {
                File jarFile = new File(path);
                if(jarFile.getName().startsWith("Base-Pilot-")) {
                    System.out.println("[INFO].... Modul loaded: " + jarFile.getName());
                }
            }
        }

        // 1. Configuration
        System.out.println("========= MODULES CONFIG =========");
        for (ModuleConfig module : modulesConfig.getModules()) {
            if (module.isEnabled()) {
                System.out.println("Config Modul: " + module.getName());
                pilotServices.stream()
                        .filter(m -> m.getName().equalsIgnoreCase(module.getName()))
                        .findFirst()
                        .ifPresent(m -> startModule(m, module));
            }
        }

        // 1. Validate
        System.out.println("========= MODULES VALIDATE =========");
        for (ModuleConfig module : modulesConfig.getModules()) {
            if (module.isEnabled()) {
                System.out.println("Validate Modul: " + module.getName());
                pilotServices.stream()
                        .filter(m -> m.getName().equalsIgnoreCase(module.getName()))
                        .findFirst()
                        .ifPresent(PilotServices::validate);
            }
        }

        // 1. Run
        System.out.println("========= MODULES Run =========");
        for (ModuleConfig module : modulesConfig.getModules()) {
            if (module.isEnabled()) {
                System.out.println("Run Modul: " + module.getName());
                pilotServices.stream()
                        .filter(m -> m.getName().equalsIgnoreCase(module.getName()))
                        .findFirst()
                        .ifPresent(Runnable::run);
            }
        }
    }
    /**
     * Module werden hier initialisiert*/
    @SuppressWarnings("unchecked")
    private <T> void startModule(PilotServices<T> module, Object config) {
        module.configuration((T) config);
    }

    public ModulesConfig getSystemConfig() {
        return systemConfig.getModules();
    }
}
