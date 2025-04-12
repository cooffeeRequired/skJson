package cz.coffeerequired.api;

import cz.coffeerequired.api.exceptions.ExtensibleThrowable;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class Extensible {

    @Getter
    public final Map<String, ArrayList<Class<?>>> loadedElements = new HashMap<>(Map.of(
            "Expressions", new ArrayList<>(),
            "Types", new ArrayList<>(),
            "Events", new ArrayList<>(),
            "Effects", new ArrayList<>(),
            "Sections", new ArrayList<>(),
            "Structures", new ArrayList<>(),
            "Functions", new ArrayList<>(),
            "Conditions", new ArrayList<>(),
            "Event Expressions", new ArrayList<>()
    ));

    protected String sign;
    protected String skriptElementPath;

    protected Extensible() {
        this.sign = "";
        this.skriptElementPath = "";
    }

    public void load() throws ExtensibleThrowable, IOException {
        if (this.sign.isEmpty() || this.skriptElementPath.isEmpty()) {
            throw new ExtensibleThrowable("Cannot invoke Skript registration for empty sign or elements packages");
        }
        Register.getAddon().loadClasses(this.skriptElementPath);
    }

    public abstract void registerElements(Register.SkriptRegister register);

    public void addNewElement(String type, Class<?> element) {
        switch (type.toLowerCase()) {
            case "effects":
                loadedElements.get("Effects").add(element);
                break;
            case "expressions":
                loadedElements.get("Expressions").add(element);
                break;
            case "conditions":
                loadedElements.get("Conditions").add(element);
                break;
            case "types":
                loadedElements.get("Types").add(element);
                break;
            case "events":
                loadedElements.get("Events").add(element);
                break;
            case "functions":
                loadedElements.get("Functions").add(element);
                break;
            case "event expressions":
                loadedElements.get("Event Expressions").add(element);
                break;
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

}
