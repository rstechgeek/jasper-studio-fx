# Component Hierarchy: ReportCanvas (Design View)

This document outlines the runtime component hierarchy of the `ReportCanvas` class, which serves as the main visual editor for the report design.

**Root Component:** `ReportCanvas` (extends `BorderPane`)

## Structure

*   **Top (Region)**
    *   `topBox` (`HBox`)
        *   *(Spacer Pane)*
        *   `hRuler` (`RulerControl`) - Horizontal Ruler

*   **Left (Region)**
    *   `vRuler` (`RulerControl`) - Vertical Ruler

*   **Center (Region)**
    *   `centerContainer` (`StackPane`)
        *   `internalScrollPane` (`ScrollPane`) - Handles panning/scrolling
            *   `workspaceArea` (`StackPane`) - Centers the page within the scroll view
                *   **`pageGroup` (`Group`)** - **The Scalable Container** (Zoom pivots from here)
                    *   `pagePane` (`StackPane`) - The "Paper" background (White with drop shadow)
                    *   `gridLayer` (`Pane`) - Draws the grid lines (Sibling of pagePane)
                    *   `contentLayer` (`Pane`) - Contains the actual report elements (Sibling of pagePane)
                    *   `adornerLayer` (`Pane`) - Contains selection handles and resize controls (Sibling of pagePane)

> **Note:** In the initial FXML definition, the layers (`gridLayer`, `contentLayer`, `adornerLayer`) are defined as children of `pagePane`. However, during initialization (`initUI` method), they are programmatically moved (reparented) directly into `pageGroup`. This ensures that all layers share the same coordinate system and scale uniformly from the (0,0) origin, preventing alignment drift during zooming.