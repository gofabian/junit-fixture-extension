package de.gofabian.jfixture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixtureSession {

    public final Map<FixtureDefinition, FixtureLifecycle> definitionLifecycleMap = new HashMap<>();
    public final List<FixtureLifecycle> orderedLifecycles = new ArrayList<>();

}
