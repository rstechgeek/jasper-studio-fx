# Jasper Studio FX

**Jasper Studio FX** is a modern, next-generation report designer for JasperReports, built with JavaFX. It aims to provide a premium, collaborative, and intuitive user experience for designing complex reports, replacing the need for legacy Eclipse-based designers.

## Overview

This project leverages the power of JavaFX to create a high-performance, responsive, and aesthetically pleasing interface. It features a layered architecture separating the UI, Designer Engine, and Data Model, ensuring scalability and maintainability.

## Key Features

### 🎨 Visual Designer
- **Drag-and-Drop**: Intuitive canvas for placing and arranging report elements.
- **Band-Based Layout**: Native support for Title, Page Header, Detail, Column Footer, and Summary bands.
- **Rich Palette**: Support for Text Fields, Static Text, Images, Rectangles, Ellipses, Lines, and Breaks.
- **Smart Tools**: Snap-to-grid, alignment guides, and 8-point resize handles.

### 📝 Source View
- **Bidirectional Editing**: Seamlessly switch between the Visual Designer and the raw JRXML Source view. Changes in one are instantly reflected in the other.
- **Error Handling**: Real-time error logging and reporting.

### 🌓 Premium Theming
- **Multiple Themes**:
    - **Light Theme**: A clean, modern look for day-to-day work.
    - **Dark Theme**: deeply integrated dark mode for low-light environments.
    - **Clear Glass UI**: A futuristic, translucent theme featuring glassmorphism effects.
- **Dynamic Switching**: Instantly toggle themes from the **Style** menu without restarting.

### 🛠 Comprehensive Tooling
- **Structure Outline**: Tree view of the report hierarchy.
- **Properties Panel**: Detailed property inspector for fine-tuning element attributes.
- **Data Source View**: Drag-and-drop fields from JSON or other data sources.
- **Logging Console**: Integrated Log Panel for debugging and status updates.

### 🤖 AI Assistant (Coming Soon)
- Placeholder for an integrated AI assistant to help generate and optimize report designs.

## Project Structure

The project follows a multi-module Maven architecture:

- **`app`**: The application bootstrapper and runtime entry point. Contains the main class and runtime dependencies.
- **`studio-ui`**: The core UI library containing views, controllers, FXML files, and CSS themes.
- **`designer-engine`**: The heart of the application. Manages the visual model, commands, history (Undo/Redo), and canvas logic.
- **`model-adapter`**: A bridge between the visual model and the underlying JasperReports library. Handles JRXML parsing and serialization.
- **`ai-assistant`**: (Experimental) Module for AI-powered features.

## Getting Started

### Prerequisites

- **Java JDK 21** or later.
- **Maven 3.8+**.

### Building the Project

Run the following command from the root directory to build all modules:

```bash
mvn clean install
```

### Running the Application

To launch the application, run the `app` module using the JavaFX Maven plugin:

```bash
mvn javafx:run -pl app
```

## Shortcuts

- **Undo**: `Ctrl/Cmd + Z`
- **Redo**: `Ctrl/Cmd + Y`
- **Delete**: `Delete` / `Backspace`
- **Zoom In**: `Ctrl/Cmd + +`
- **Zoom Out**: `Ctrl/Cmd + -`
- **Save**: `Ctrl/Cmd + S`

## License

[License Information Here]
