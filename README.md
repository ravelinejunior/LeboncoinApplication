Album Viewer App
A native Android application that fetches and displays a list of albums from a remote API, with support for offline persistence and navigation to album details.

📱 Features
- Album listing with title and image
  
- Navigation to detail screen
  
- Remote loading with local cache (offline support)
  
- Data persists after application is closed
  
- Unit and UI tests included
  
- Compatible with screen rotation and configuration changes

🏛 Architecture

This project follows the MVVM (Model-View-ViewModel) architecture pattern to ensure a clean separation of responsibilities:

- Model: Data classes, local database (Room), and remote API (Retrofit).

- View: Activities and XML layouts to display UI.

- ViewModel: Acts as a bridge between the view and data layers, exposing state via StateFlow.

🧪 Testing

The project includes both unit tests and UI tests to ensure functionality and reliability:

- Repository-level unit tests using mock DAO and API.

- ViewModel tests using coroutine test rules.

- UI tests using Espresso.

- Edge cases covered (e.g., network errors, empty responses, database exceptions).

🛠 Tech Stack

- Kotlin: Main language
  
- Coroutines & Flow: Asynchronous programming and state management

- Retrofit: API requests

- Room: Local persistence

- Jetpack ViewModel: Lifecycle-aware state management

- StateFlow: Reactive UI updates

- JUnit, Mockito, Turbine: Testing tools

- Espresso: UI testing

⚠️ Requirements

- Android Studio Giraffe or newer

- Android SDK 33+

- Minimum SDK: 24 (Android 7.0)

📂 Folder Structure

data/

├── api/           # Retrofit services

├── db/            # Room DAOs and database

├── model/         # Data models

domain/

├── repository/    # Repository interfaces and implementations

presentation/

├── albums/     # Album list screen, state and viewModel

├── details/   # Album detail screen, state and viewModel





