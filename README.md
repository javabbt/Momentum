# 💎Momentum

Momentum is an innovative photo-sharing social app that revolves around the concept of "photo
chains" - a unique feature
where users create themed photo sequences that their friends must continue within 24 hours. The app
combines the engaging
mechanics of streaks with collaborative storytelling, encouraging daily active usage through
time-sensitive interactions.
Each chain becomes a visual story where friends share moments around specific themes like "Morning
Coffee" or "Daily Workout,"
with visible streaks and time limits creating a sense of urgency and community engagement. The app's
focus on quick, authentic photo
sharing and social connection is designed to drive viral growth through its inherent
multi-participant nature, where each chain requires
friends to join and continue the visual narrative. With a clean, modern interface and emphasis on
user experience, Momentum aims to reach 1M
daily active users by tapping into young adults' desire for meaningful digital connections and
creative expression.

## Demo
[![Watch the video]]((https://github.com/javabbt/Momentum/blob/main/demo.webm))

## Tech-Stack

This project incorporates industry best practices and utilizes a wide range of popular libraries and
tools in the Android ecosystem. The majority of the libraries used are in stable versions, unless
there is a specific reason to use a non-stable dependency.

* Tech-stack
    * [100% Kotlin](https://kotlinlang.org/)
        + [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) - perform
          background operations
        + [Kotlin Flow](https://kotlinlang.org/docs/flow.html) - data flow across all app layers,
          including views
        + [Kotlin Symbol Processing](https://kotlinlang.org/docs/ksp-overview.html) - enable
          compiler plugins
    * [Jetpack](https://developer.android.com/jetpack)
        * [Compose](https://developer.android.com/jetpack/compose) - modern, native UI kit
        * [Navigation](https://developer.android.com/topic/libraries/architecture/navigation/) -
          in-app navigation
    * [Koin](https://insert-koin.io/) - dependency injection (dependency retrieval)
        * No code generation: Unlike Dagger Hilt, Koin does not require any code generation during
          the build process, which can simplify the development workflow.
        * No reflection: Koin does not rely on reflection, which can improve performance and reduce
          the risk of runtime errors.
        * Testability: Koin provides good support for testing, allowing developers to easily mock
          dependencies and write unit tests for their Android applications.
        * Kotlin-friendly: Koin is designed with Kotlin in mind and provides seamless integration
          with Kotlin features such as coroutines and extension functions.
    * [Coil](https://github.com/coil-kt/coil) - image loading library
* Modern Architecture
    * [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
    * Single activity architecture
      using [Navigation component](https://developer.android.com/guide/navigation/navigation-getting-started)
    * MVVM
    * [Android KTX](https://developer.android.com/kotlin/ktx) - Jetpack Kotlin extensions
* UI
    * Reactive UI
    * [Jetpack Compose](https://developer.android.com/jetpack/compose) - modern, native UI kit (used
      for Fragments)
    * [Material Design 3](https://m3.material.io/) - application design system providing UI
      components

* CI
    * [GitHub Actions](https://github.com/features/actions)
    * Automatic PR verification including tests, linters, and 3rd online tools

* Testing
    * [Unit Tests](https://en.wikipedia.org/wiki/Unit_testing) ([JUnit 5](https://junit.org/junit5/)
    * [Mockk](https://mockk.io/) - mocking framework
* Static analysis tools (linters)
    * [Detekt](https://github.com/arturbosch/detekt#with-gradle) - verify code complexity and code
      smells
    * [Spotless](https://github.com/diffplug/spotless) - verify code complexity and code smells
    * [Android Lint](http://tools.android.com/tips/lint) - verify Android platform usage
* Gradle
    * [Gradle Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html) - define build
      scripts

### Feature Module Structure

`Clean Architecture` is implemented at module level - each module contains its own set of Clean
Architecture layers

#### Advantages of Feature Module Architecture

1. **Modularity**: Feature module architecture promotes modularity by dividing the application into
   smaller, self-contained modules. Each module focuses on a specific feature or functionality,
   making it easier to understand, maintain, and test.

2. **Code Reusability**: Feature modules can be reused across different projects or applications.
   This allows developers to save time and effort by leveraging existing modules instead of
   reinventing the wheel for every project.

3. **Parallel Development**: With feature module architecture, different teams or developers can
   work on different modules simultaneously. This promotes parallel development and reduces
   dependencies, leading to faster development cycles.

4. **Scalability**: Feature modules can be added or removed without affecting the entire
   application. This makes it easier to scale the application by adding new features or removing
   obsolete ones, without impacting the stability of the existing codebase.

5. **Encapsulation**: Each feature module encapsulates its own set of components, such as
   presentation, domain, and data layers. This promotes encapsulation and separation of concerns,
   making the codebase more maintainable and easier to understand.

6. **Testing**: Feature module architecture facilitates unit testing and integration testing. Each
   module can be tested independently, ensuring that the individual features work correctly and do
   not introduce regressions in other parts of the application.

7. **Team Collaboration**: Feature module architecture enables teams to work on different modules
   independently, without stepping on each other's toes. This promotes collaboration and allows
   teams to focus on their specific areas of expertise.

8. **Code Organization**: Feature module architecture provides a clear structure for organizing
   code. Each module has its own directory structure, making it easier to navigate and locate
   specific code files.

9. **Dependency Management**: Feature modules can have their own dependencies, allowing for granular
   control over the libraries and frameworks used in each module. This helps in managing
   dependencies and avoiding conflicts between different parts of the application.

10. **Code Reusability**: Feature modules can be shared across different projects or applications,
    promoting code reusability and reducing duplication of effort.

Overall, feature module architecture offers several advantages in terms of modularity, code
reusability, parallel development, scalability, encapsulation, testing, team collaboration, code
organization, and dependency management.

#### Presentation Layer

This layer is closest to what the user sees on the screen.

- `MVVM` - Jetpack `ViewModel` is used to encapsulate `common UI state`. It exposes the `state` via
  observable state
  holder (`Kotlin Flow`)

  This approach facilitates the creation of consistent states. State is collected via the
  `collectAsUiStateWithLifeCycle` method. Flows collection happens in a lifecycle-aware manner, so
  the UI is
  automatically updated when the state changes.

#### Domain Layer

This is the core layer of the application.

Components:

- **UseCase** - contains business logic
- **Repository interface** - required to keep the `domain` layer independent from
  the `data layer`

#### Data Layer

Manages application data. Connect to data sources and provide data through repository to the
`domain` layer eg. retrieve
data from the internet.

Components:

- **Repository** is exposing data to the `domain` layer. Depending on the application structure and
  quality of the
  external APIs repository can also merge, filter, and transform the data. These operations intend
  to create
  high-quality data source for the `domain` layer.
- **Mapper** - maps `data model` to `domain model` (to keep `domain` layer independent from the
  `data` layer).

## CI Pipeline

CI is utilizing [GitHub Actions](https://github.com/features/actions). Complete GitHub Actions
config is located in
the [.github/workflows](.github/workflows) folder.

