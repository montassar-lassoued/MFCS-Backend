package com.IntraConnect.listViews.config;

import com.IntraConnect.intf.PilotViewFactory;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import com.IntraConnect.listViews.PilotView;

import java.lang.reflect.InvocationTargetException;

public class ViewConfig {

    public static PilotViewRegister createRegister() {

        PilotViewRegister register = new PilotViewRegister();

        try (ScanResult scan = new ClassGraph()
                .enableClassInfo()
                .acceptPackages("com.IntraConnect.views")   // <-- Package!
                .scan()) {

            for (ClassInfo info : scan.getSubclasses(PilotView.class.getName())) {

                Class<?> cls = info.loadClass();
                Object instance = cls.getDeclaredConstructor().newInstance();
                // factory() Methode finden
                var factoryMethod = cls.getMethod("getClassType");

                // Factory aufrufen (statische Methode)
                Object factoryObj = factoryMethod.invoke(instance);

                // Cast zur Factory
                PilotViewFactory factory = (PilotViewFactory) factoryObj;

                // View-Factory registrieren
                register.addFactory(factory);
            }
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }

        return register;
    }
}
