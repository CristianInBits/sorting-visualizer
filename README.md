# Sorting Visualizer in JavaFX

A dynamic sorting algorithm visualizer built with JavaFX.  
This project helps students and developers understand how different sorting algorithms work by displaying step-by-step animations of the sorting process.

---

## 🚀 Features

- Real-time visualization of multiple sorting algorithms
- Step-by-step execution and pause support
- Adjustable animation speed with a slider
- Interactive controls: generate new array, select algorithm, start animation
- Real-time counters for comparisons and moves (array writes that change a value,
  so the four algorithms stay comparable — Merge Sort performs no swaps at all)
- Automatic disabling of controls during sorting
- Clean and responsive UI built with JavaFX
- About dialog and dark/light theme support

---

## 📚 Algorithms Implemented

| Algorithm      | Status     |
|----------------|------------|
| Bubble Sort    | ✅ Done     |
| Selection Sort | ✅ Done     |
| Merge Sort     | ✅ Done     |
| Quick Sort     | ✅ Done     |
| Insertion Sort | ⏳ Planned  |
| Heap Sort      | 🟡 Optional |

---

## 🛠️ Technologies

- Java 17+
- JavaFX 21+
- Maven (via the bundled wrapper)
- JUnit 5

---

## ▶️ How to Run

Make sure you have:

- Java 17+ installed and configured as `JAVA_HOME`

Maven does **not** need to be installed: the project ships with the
[Maven Wrapper](https://maven.apache.org/wrapper/), which fetches the pinned
version (3.9.16) on first use.

Then run the app with:

```bash
./mvnw clean javafx:run
```

On Windows (CMD or PowerShell) use `mvnw.cmd clean javafx:run`.

---

## 🧪 Tests

The algorithms carry no user interface, so the suite runs without starting a
JavaFX toolkit:

```bash
./mvnw test
```

Every algorithm is checked against random arrays and edge cases, and each one
is also replayed from the steps it reported, so an algorithm cannot quietly
disagree with what the animation shows.

---

## 📦 Project Structure

```bash
src/main/java/app/
├── algorithms/   # The algorithms, as plain Java. No JavaFX in here.
│                 # Each one sorts an int[] and reports its steps to a
│                 # SortTrace, so the same code drives the animation and
│                 # the tests.
├── player/       # AnimatedTrace: turns those steps into the animation.
│                 # The only place that knows about bars, colours and timing.
├── controller/   # Controls, run state, worker thread
├── view/         # Bars and layout
└── Main.java     # Application entry point

src/test/java/app/
└── algorithms/   # Headless tests: no toolkit, no window, ~0.1 s
```

---

## 📸 Preview

### 🔁 Bubble Sort Animation

<p align="center">
  <img src="assets/preview/sort.gif" width="300" alt="Sorting animation">
</p>

### 💡 Light Mode vs 🌙 Dark Mode

<table align="center">
  <tr>
    <td align="center"><strong>Light Mode</strong></td>
    <td align="center"><strong>Dark Mode</strong></td>
  </tr>
  <tr>
    <td align="center">
      <img src="assets/preview/light-mode.png" width="400" alt="Light mode UI">
    </td>
    <td align="center">
      <img src="assets/preview/dark-mode.png" width="400" alt="Dark mode UI">
    </td>
  </tr>
</table>

<table align="center">
  <tr>
    <td align="center"><strong>🧮 Algorithms</strong></td>
    <td align="center"><strong>⏩ Step by step Mode</strong></td>
  </tr>
  <tr>
    <td align="center">
      <img src="assets/preview/algorithms.png" width="200" alt="Algorithms">
    </td>
    <td align="center">
      <img src="assets/preview/sbs.gif" width="400" alt="sbs">
    </td>
  </tr>
</table>

---

## 🧑‍💻 Author

Built with ❤️ by a Cristian Laurentiu Sindila, Computer Engineering student passionate about algorithms, data structures, and clean code.

---

## 🤝 Contributing

Interested in improving the project?
Check out the [CONTRIBUTING.md](CONTRIBUTING.md) guide to get started.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
