package com.jasperstudio.app;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public class ArchitectureTest {

    @Test
    public void validateLayeredArchitecture() {
        JavaClasses importedClasses = new ClassFileImporter().importPackages("com.jasperstudio");

        ArchRule layerRule = layeredArchitecture()
                .consideringOnlyDependenciesInAnyPackage("com.jasperstudio..")
                .layer("UI").definedBy("com.jasperstudio.ui..")
                .layer("Engine").definedBy("com.jasperstudio.designer..")
                .layer("Model").definedBy("com.jasperstudio.model..")
                .layer("AI").definedBy("com.jasperstudio.ai..")
                .layer("App").definedBy("com.jasperstudio.app..")

                // Strict dependency rules
                // App (Deepest) -> UI -> Engine -> Model
                // AI observes Model and Engine

                .whereLayer("App").mayNotBeAccessedByAnyLayer()
                .whereLayer("UI").mayOnlyBeAccessedByLayers("App")
                .whereLayer("Engine").mayOnlyBeAccessedByLayers("UI", "AI")
                .whereLayer("Model").mayOnlyBeAccessedByLayers("Engine", "AI", "UI"); // UI needs read-only access
                                                                                      // usually, or via Engine.
        // Note: For now allowing UI -> Model for simple binding, but ideally mapped via
        // Engine.
        // Let's keep it strict: UI can access Engine, Engine accesses Model.
        // But wait, "model-adapter" is a dependency of UI?
        // In my pom, "studio-ui" depends on "designer-engine" and "model-adapter".
        // So UI *can* access Model.

        // Let's refine based on the POM structure I created:
        // app -> ui, ai
        // ui -> engine, model (for Observable wrappers)
        // engine -> model
        // ai -> model, engine

        ArchRule refinedRule = layeredArchitecture()
                .consideringOnlyDependenciesInAnyPackage("com.jasperstudio..")
                .layer("UI").definedBy("com.jasperstudio.ui..")
                .layer("Engine").definedBy("com.jasperstudio.designer..", "com.jasperstudio.descriptor..")
                .layer("Model").definedBy("com.jasperstudio.model..")
                .layer("AI").definedBy("com.jasperstudio.ai..")
                .layer("App").definedBy("com.jasperstudio.app..")

                .whereLayer("App").mayNotBeAccessedByAnyLayer()
                .whereLayer("UI").mayOnlyBeAccessedByLayers("App")
                .whereLayer("Model").mayOnlyBeAccessedByLayers("Engine", "UI", "AI")
                .whereLayer("Engine").mayOnlyBeAccessedByLayers("UI", "AI");

        refinedRule.check(importedClasses);
    }
}
