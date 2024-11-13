package cz.coffeerequired.api;

import cz.coffeerequired.SkJson;
import cz.coffeerequired.api.exceptions.ModulableException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class Modulable {

    @Getter public final Map<String, ArrayList<Class<?>>> loadedElements = new HashMap<>(Map.of(
            "Expressions", new ArrayList<>(),
            "Events", new ArrayList<>(),
            "Effects", new ArrayList<>(),
            "Sections", new ArrayList<>(),
            "Structures", new ArrayList<>(),
            "Functions", new ArrayList<>(),
            "Conditions", new ArrayList<>())
    );

    protected String sign;
    protected String skriptElementPath;

    protected Modulable() {
        this.sign = "";
        this.skriptElementPath = "";
    }

    public void load() throws ModulableException {
        if (this.sign.isEmpty() || this.skriptElementPath.isEmpty()) {
            throw new ModulableException("Cannot invoke Skript registration for empty sign or elements packages");
        }
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
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

}
