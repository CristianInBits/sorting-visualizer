<div align="center">

# Sorting Visualizer

**Watch classic sorting algorithms work, one step at a time.**

A JavaFX desktop app that animates Bubble, Selection, Quick and Merge Sort —
with a step-by-step mode you can drive by hand, live counters, and a light and
dark theme.

[![build](https://github.com/CristianInBits/sorting-visualizer/actions/workflows/build.yml/badge.svg)](https://github.com/CristianInBits/sorting-visualizer/actions/workflows/build.yml)
[![Java](https://img.shields.io/badge/Java-17%2B-007396)](https://adoptium.net/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-1f8ac0)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

<img src="assets/preview/hero.png" width="860" alt="Quick Sort running: one bar red as it is compared, the pivot orange on the right, counters showing 30 comparisons and 34 moves">

</div>

---

## Run it

```bash
git clone https://github.com/CristianInBits/sorting-visualizer.git
cd sorting-visualizer
./mvnw javafx:run
```

**Java 17 or newer is the only thing you need to install.** Maven comes with the
repository through the wrapper, which fetches the pinned version on first use.

On Windows, use `mvnw.cmd javafx:run`.

---

## What you can do

- **Step through it by hand.** Tick *Step by step* and the run pauses on every
  comparison and every move. Untick it mid-run and it carries on from where it
  paused.
- **Watch the cost add up.** Comparisons and moves are counted live, and they
  stay on screen when the run ends so you can actually read them.
- **Set the pace.** A delay of 1 to 200 ms per frame, with a live readout.
- **Stop whenever.** The array is always left consistent, never half-copied.
- **Switch themes.** Light and dark, bars included.
- **Resize freely.** The bars fill the window and follow it.

<br>

<div align="center">
<img src="assets/preview/sbs.gif" width="620" alt="Step-by-step mode: each click on Next step advances one comparison or one move, and the counters tick up">
<br>
<em>Step-by-step mode — one click, one step.</em>
</div>

---

## The algorithms

<div align="center">
<img src="assets/preview/sort.gif" width="680" alt="Bubble Sort running: the compared pair is red, and the sorted tail grows from the right">
<br>
<em>Bubble Sort — the largest value bubbles to the end on every pass.</em>
</div>

<br>

| Algorithm | Comparisons | Moves | Extra memory | Stable |
|---|---|---|---|---|
| **Bubble Sort** | O(n²) | O(n²) | O(1) | Yes |
| **Selection Sort** | O(n²) | O(n) | O(1) | No |
| **Quick Sort** | O(n log n) avg, O(n²) worst | O(n log n) avg | O(log n) | No |
| **Merge Sort** | O(n log n) | O(n log n) | O(n) | Yes |

> Bubble Sort is the textbook version without the early-exit flag, so it always
> performs the full n(n − 1)/2 comparisons even on an already sorted array.
> Quick Sort partitions with Lomuto on the last element, so a sorted input is
> its worst case.

### What the app actually measures

Averages over 500 random arrays of 50 elements, the same arrays for every
algorithm:

| Algorithm | Comparisons | Moves |
|---|---:|---:|
| Bubble Sort | 1225 | 1223 |
| Selection Sort | 1225 | **91** |
| Quick Sort | **259** | 212 |
| Merge Sort | 222 | 222 |

A **move** is an array write that actually changes a value — that is, a bar that
changes height. A swap of two different values counts as 2, one merge copy
counts as 1.

Counting moves rather than swaps is what makes the four comparable: Merge Sort
performs no swaps at all, it copies through a buffer, so a swap counter made it
look like it shuffled more data than Quick Sort when it does not.

---

## Themes

<div align="center">
<table>
<tr>
<td align="center"><strong>Light</strong></td>
<td align="center"><strong>Dark</strong></td>
</tr>
<tr>
<td><img src="assets/preview/light-mode.png" width="420" alt="The app in the light theme"></td>
<td><img src="assets/preview/dark-mode.png" width="420" alt="The app in the dark theme"></td>
</tr>
</table>
</div>

Bar colours live in the stylesheets, not in the Java code, so they follow the
theme along with everything else.

---

## How it works

The algorithms are plain Java. They know nothing about JavaFX, threads or
timing: each one sorts an `int[]` and reports what it did to a `SortTrace`.

```
        BubbleSort · SelectionSort · QuickSort · MergeSort
                  sort(int[] values, SortTrace trace)
                                 │
                 ┌───────────────┴────────────────┐
                 ▼                                ▼
          AnimatedTrace                    RecordingTrace
   bars, colours, delay, stop         counts, replays, asserts
        (the running app)                   (the tests)
```

Two consequences worth having:

- **The algorithms are testable** without starting a window. The whole suite
  runs in about a second.
- **The narration is verified, not assumed.** Every test run is rebuilt from the
  reported steps alone and has to reproduce the sorted array, so an algorithm
  cannot quietly disagree with what the animation draws.

Stopping is an exception thrown by the trace, so an algorithm needs no
`isStopRequested()` checks scattered through its loops — it just lets the
exception unwind.

---

## Tests

```bash
./mvnw test
```

22 tests, no JavaFX toolkit, about a second:

- Each algorithm against 200 random arrays, plus the edge cases — empty, one
  element, all equal, already sorted, reversed, duplicates, extreme values.
- Each one **stopped at every single step** of a run, checking the array is
  never left with values lost or invented.
- The step-by-step gate, driven by real threads: a Next Step that arrives early
  is not dropped, and unticking the box releases a run that is parked.

---

## Project structure

```
src/main/java/app/
├── algorithms/   Plain Java. No JavaFX in here.
│                 Each algorithm sorts an int[] and reports to a SortTrace.
├── player/       AnimatedTrace turns those steps into the animation;
│                 StepGate paces the run. The only JavaFX-aware layer.
├── controller/   Controls, run state, worker thread
├── view/         Bars and layout. Converts values to pixels, so the
│                 drawing scales with the window.
└── Main.java     Entry point, header and theme switching

src/main/resources/
├── base.css      Shape, spacing, typography. No colour literals.
├── light.css     Palette only
└── dark.css      Palette only

src/test/java/app/
├── algorithms/   Headless algorithm tests
└── player/       Concurrency tests for the step gate
```

<div align="center">
<img src="assets/preview/algorithms.png" width="200" alt="The algorithm picker, open, showing Bubble, Quick, Selection and Merge Sort">
</div>

---

## Contributing

Bug reports, feature requests and pull requests are all welcome — see
[CONTRIBUTING.md](CONTRIBUTING.md) for the branch naming, the commands to run
before opening a PR, and the issue templates.

Two algorithms are still open if you are looking for somewhere to start:
**Insertion Sort** and **Heap Sort**. Adding one is a small class in
`algorithms/` plus an entry in the picker, and it inherits the existing test
suite for free.

---

## Author

Built by [Cristian Laurentiu Sindila](https://github.com/CristianInBits),
a Computer Engineering student interested in algorithms, data structures and
clean code.

## License

[MIT](LICENSE).
