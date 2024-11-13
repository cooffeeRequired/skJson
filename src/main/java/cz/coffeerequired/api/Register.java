package cz.coffeerequired.api;


import cz.coffeerequired.api.annotators.Module;
import cz.coffeerequired.support.AnsiColorConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

import static cz.coffeerequired.SkJson.logger;

public class Register {

    public void tryRegisterSkript() {

        if (isSkriptAvailable()) {
            logger().info("Hooking into Skript plugin... Hooks initialized.");
            logger().info("Trying register Skript addon...");
            logger().info("Trying register Skript elements...");
            tryRegisterSkriptElements();

        } else {
            System.out.println("Skript plugin not detected.");
        }
    }

    private void tryRegisterSkriptElements() {

    }

    public <T> void registerNewHook(Class<T> tClass) {
        if (isClassAvailable(tClass) && tClass.getName().equals("ch.njol.skript.Skript")) {

            logger().info("Attempting to hook into Skript plugin...");
            tryRegisterSkript();
        } else {
            logger().error("Unsupported hook class: " + tClass.getName());
        }
    }

    private boolean isSkriptAvailable() {
        try {
            Class.forName("ch.njol.skript.Skript");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean isClassAvailable(Class<?> className) {
        try {
            Class.forName(className.getName());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public <T extends Modulable> void registerModule(Class<T> module) {
        try {
            if (module.isAnnotationPresent(Module.class) && Modifier.isPublic(module.getModifiers())) {
                Module annotation = module.getAnnotation(Module.class);
                String moduleName = annotation.module();
                String moduleVersion = annotation.version();
                logger().info("Registering module: " + AnsiColorConverter.hexToAnsi("#47a5ff")+ moduleName + AnsiColorConverter.RESET + " version: " + AnsiColorConverter.hexToAnsi("#8dff3f") + moduleVersion);
            } else {
                throw new IllegalCallerException("Class what extends Modulable always need to be annotated by @Module");
            }
        } catch (Exception e) {
            logger().exception(e.getMessage(), e);
        }
    }
}
