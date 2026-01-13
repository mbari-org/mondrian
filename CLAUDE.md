# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Mondrian is a VARS (Video Annotation and Reference System) image annotation application built with JavaFX. It provides a desktop UI for annotating underwater imagery with taxonomic concepts and regions of interest (ROI), integrating with the VARS microservice stack.

## Development Commands

### Prerequisites
- Java 25 (modular JPMS application)
- GitHub credentials for MBARI Maven repository access (libs hosted at github.com/mbari-org/maven)
- VARS microservice stack running (see [m3-quickstart](https://github.com/mbari-org/m3-quickstart))

Set credentials via environment variables or gradle properties:
```bash
export GITHUB_USERNAME=your_username
export GITHUB_TOKEN=your_github_token
# OR use gradle properties:
# -P"gpr.user"=username -P"gpr.key"=token
```

### Build and Run
```bash
# Navigate to mondrian subdirectory for gradle commands
cd mondrian

# Build the project
./gradlew build

# Run the application
./gradlew run

# Run with remote debugger attached (port 5005)
./gradlew runDebug

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests org.mbari.mondrian.SanityTest

# Create native installer package (DMG/DEB/MSI)
./gradlew jpackage

# Clean build
./gradlew clean
```

### Code Quality
```bash
# Check for dependency updates
./gradlew dependencyUpdates
```

## Architecture

### Design Pattern: Event-Driven MVC/MVVM Hybrid

The application uses a centralized EventBus for loose coupling between components:

1. **EventBus Communication**: All component interaction flows through `io.reactivex.rxjava3.subjects.PublishSubject`
2. **Three Message Types**:
   - **Messages** (`org.mbari.mondrian.msg.messages`) - Simple data transfer for state synchronization (immutable records)
   - **Commands** (`org.mbari.mondrian.msg.commands`) - Executable operations with `apply()` and `unapply()` for undo/redo support
   - **Events** - UI events from imgfx library

3. **Command Pattern**: All state mutations go through `CommandManager` which maintains undo/redo queues and executes commands asynchronously

### Core Components

**Dependency Injection**: `ToolBox` record (accessed via `Initializer.getToolBox()`) provides:
- `eventBus` - Central message bus (RxJava PublishSubject)
- `servicesProperty` - ObjectProperty wrapping the Services record (allows hot-reload)
- `data` - Observable application state (current images, selected image, annotations, user)
- `localizations` - Manages UI localization state and selection
- `annotationColors` - Color schemes for annotations
- `i18n` - Internationalization ResourceBundle
- `aes` - Encryption for credential storage

**Application Flow**:
```
User Action → UI Controller → EventBus.publish(Message/Command)
→ Subscribers (AppController, etc.) → CommandManager (for Commands)
→ Services (async) → EventBus.publish(Response Message)
→ Update Data (Observable) → UI Auto-Updates
```

### Key Packages

- **`org.mbari.mondrian`** - Core application (App, AppController, Initializer, Services, Data)
- **`org.mbari.mondrian.msg`** - Message system (messages/, commands/, events/)
- **`org.mbari.mondrian.services`** - Service layer interfaces and implementations
  - `services.vars.*` - Production VARS microservice clients
  - `services.noop.*` - No-op fallback implementations
- **`org.mbari.mondrian.domain`** - Domain models and data structures
  - **`VarsLocalization`** - Critical bridge between VARS data model (Annotation + Association) and UI model (Localization from imgfx)
- **`org.mbari.mondrian.javafx`** - UI layer (controllers, dialogs, controls)
  - `javafx.roi.*` - ROI (Region of Interest) translation system
- **`org.mbari.mondrian.util`** - Utilities

### VarsLocalization Bridge Pattern

`VarsLocalization` is the critical adapter between two worlds:
- **VARS Model**: `Annotation` (concept + metadata) + `Association` (ROI geometry as key-value pairs)
- **imgfx Model**: `Localization` (UI drawable ROI with concept)

It tracks dirty state for both concept changes and localization geometry changes, enabling proper synchronization with the database.

### ROI Translation System

**Translator Pattern** (`org.mbari.mondrian.javafx.roi`):
- `RoiTranslator<C>` interface - Bidirectional conversion between VARS Associations and imgfx Localizations
  - `fromAssociation(Association, ...) → Localization<C>` - Database → UI
  - `fromLocalization(Localization<C>, Annotation) → List<Association>` - UI → Database
- `RoiTranslators` - Factory/registry mapping association link names to translators
- Supported ROI types: BoundingBox, Line, Marker, Polygon

When adding new ROI types:
1. Create new `RoiTranslator<C>` implementation
2. Register in `RoiTranslators` factory
3. Define VARS association schema (link name + key-value pairs)

### Service Layer

All service methods return `CompletableFuture<T>` for async operations.

**Service Interfaces** (aggregated in `Services` record):
- `AnnotationService` - CRUD for annotations
- `AssociationService` - CRUD for associations (ROI data)
- `ImageService` - Query images by video sequence, concept, etc.
- `MediaService` - Media/video metadata
- `NamesService` - Taxonomy/concept hierarchy
- `UsersService` - User authentication/info

**Separate Services**:
- `MLPredictionService` - Machine learning predictions (optional, not in Services record)

**VARS Microservices Integration**:
- **Raziel** - OAuth2 authentication and endpoint discovery (credentials stored encrypted in `~/.vars/raziel.txt`)
- **Annosaurus** - Annotation storage (annotations, associations, images)
- **Vampire Squid** - Media/video metadata
- **Oni** - User management
- **Megalodon/Pythia** - ML predictions (optional)

## Module System

This is a Java Platform Module System (JPMS) application. See `module-info.java` for module dependencies.

**Critical JVM Args** (in `build.gradle.kts`):
```
--add-opens java.base/java.lang.invoke=mondrian.merged.module
--add-reads mondrian.merged.module=org.slf4j
--add-reads mondrian.merged.module=com.google.gson
```

These are required for jlink merged module compatibility. Do not remove.

## Testing

- Uses JUnit 5 (Jupiter)
- Test files in `mondrian/src/test/java/`
- Most tests require VARS microservices running
- Run specific tests: `./gradlew test --tests ClassName`

## Common Patterns

### Adding a New Command
1. Create record implementing `Command` in `org.mbari.mondrian.msg.commands`
2. Implement `apply()` (forward operation) and `unapply()` (undo operation)
3. Publish to EventBus: `eventBus.publish(new YourCommand(...))`
4. Subscribe in `AppController` or relevant controller
5. CommandManager automatically handles undo/redo

### Adding New Message Handling
1. Define message record in `org.mbari.mondrian.msg.messages`
2. Subscribe in appropriate controller:
   ```java
   eventBus.toObserverable()
       .ofType(YourMessage.class)
       .observeOn(JavaFxScheduler.platform())
       .subscribe(msg -> handleMessage(msg));
   ```
   Note: The method name is `toObserverable()` (with typo) in the codebase.

### Accessing Application State
```java
var toolBox = Initializer.getToolBox();
var data = toolBox.data();
var services = toolBox.servicesProperty().get();
var eventBus = toolBox.eventBus();
```

## Dependencies

- **JavaFX 25** - UI framework
- **RxJava 3** - Reactive event bus
- **imgfx** (MBARI) - Image annotation UI components with ROI drawing
- **vars-*-java-sdk** (MBARI) - Kiota-generated microservice clients (annosaurus, oni, raziel, vampire-squid)
- **Gson** - JSON serialization
- **OkHttp3** - HTTP client
- **Logback** - Logging
- **ControlsFX** - Extended JavaFX controls

## Important Notes

- All state mutations must go through Commands for undo/redo support
- Use EventBus for all cross-component communication (avoid direct coupling)
- Service calls are async - always use `CompletableFuture` patterns
- VarsLocalization dirty tracking is critical for save operations
- UI updates must run on JavaFX thread - use `JavaFxScheduler.platform()`
- Messages are immutable records - never mutate shared state directly
