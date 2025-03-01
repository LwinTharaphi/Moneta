# Moneta

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://your-build-url)  
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

**An intuitive expense, budget, and reminder tracker that keeps you on top of your finances while delivering the latest financial news.**

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Screenshots](#screenshots)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
- [Built With](#built-with)
- [Contributing](#contributing)
- [Future Updates](#future-updates)
- [License](#license)
- [Contact](#contact)

## Introduction

Moneta is a comprehensive Android app designed to help users track their daily expenses and manage budgets efficiently. With a clean and modern interface supporting both light and dark themes, Moneta not only tracks your spending but also reminds you to log your transactions and keeps you updated with financial news. The app leverages Firebase Firestore for cloud data storage and Room for local persistence, ensuring your data is secure and accessible across devices.

## Features

- **Expense Tracking:** Log and view your daily expenses with an interactive pie chart.
- **Budget Management:** Set monthly budgets and monitor your spending against the total budget.
- **Reminders & Notifications:** Schedule reminders to record your expenses. (Customizable repeat options and time pickers are available.)
- **Financial News:** Get the latest news articles from the world of finance directly within the app.
- **CSV Import:** A button for importing expenses from CSV files is provided (note: CSV import functionality is not implemented yet).
- **Theme Support:** Toggle between light and dark modes for an optimal viewing experience.
- **Firebase Integration:** Secure cloud data storage with Firestore.
- **Room Database:** Local data persistence for offline capabilities.

## Screenshots

Include screenshots or GIFs of your app in action here. Replace the placeholders below with your actual image paths.

![Home Screen](path/to/home_screenshot.png)  
![Expense Screen](path/to/expense_screenshot.png)  
![Budget Screen](path/to/budget_screenshot.png)  
![Reminder Screen](path/to/reminder_screenshot.png)  

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio)
- JDK 11 or higher
- Android SDK
  - compileSdk = 35
  - minSdk = 24
  - targetSdk = 34
- Firebase project configured with Firestore

### Installation

1. **Clone the repository:**
   - Download the ZIP file and open it in Android Studio, or use Git:
     ```bash
     git clone https://your-repository-url.git
     ```
2. **Build and run:**
   - Open the project in Android Studio.
   - Sync Gradle and build the project.
   - Click the "Run" button (green play icon) and select an emulator or connected device.

## Usage

- **Expense Tracking:** Navigate to the Expense Screen to add, view, or edit your expenses. Swipe or click on an expense to see details.
- **Budget Setup:** Go to the Budget Screen to set up and edit your monthly budgets. Use the year picker to review past budgets.
- **Reminders:** Access the Reminders section from your profile or main menu to add new reminders. Customize the reminder time and repeat options.
- **Financial News:** Check out the Financial News page for up-to-date articles on finance.
- **CSV Import:** Tap the "Import Expenses from CSV" button on the profile screen. (Note: This button currently only opens a dialog and does not perform an import yet.)

## Built With

- [Kotlin](https://kotlinlang.org/) – Primary programming language.
- [Jetpack Compose](https://developer.android.com/jetpack/compose) – Modern UI toolkit.
- [Firebase Firestore](https://firebase.google.com/docs/firestore) – Cloud database for storing user data.
- [Room Database](https://developer.android.com/training/data-storage/room) – Local database persistence.
- [Retrofit](https://square.github.io/retrofit/) – Networking library (for fetching financial news).
- [Coil](https://coil-kt.github.io/coil/) – Image loading library.
- Other libraries and tools as required.

## Contributing

Thanks to all the contributors and teammates who have worked on Moneta. If you wish to contribute, please fork the repository and create a pull request. All contributions are welcome!

## Future Updates

Yes, I will try to develop further for this project in my Free Time.

## License

This project is licensed under the [Apache 2.0 License](LICENSE). See the LICENSE file for details.

## Contact

- **Kino:** [kaungkhantlin999@gmail.com](mailto:kaungkhantlin999@gmail.com)
