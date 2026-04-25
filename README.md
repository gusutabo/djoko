# djoko

> [!IMPORTANT]
> This project is minimal.

A small terminal tennis game simulator written in Clojure.

## Functionalities

* Real tennis game scoring with deuce and advantage
* Point simulation with rallies instead of a single weighted roll
* Parabolic ball trajectory affecting net clearance and landing depth
* Player vs CPU shot selection
* Difficulty selection before the match starts
* CPU behavior that reacts to scoreboard pressure
* Game loop with terminal interaction
* Winner detection

## Usage Method

### Clone the repository
```bash
git clone https://github.com/gusutabo/djoko.git
cd djoko
```

### Run the game
```bash
lein run
```

## Gameplay

During the game, you will be prompted to choose a shot profile:

Before the first point, you also choose a difficulty:

```text
Choose difficulty:
  (e) Easy
  (n) Normal
  (h) Hard
```

* `e` → more margin for the player, more mistakes from the cpu
* `n` → balanced conditions
* `h` → less margin for the player, cleaner execution from the cpu
* `q` → leave the game immediately

```text
Choose your shot: (s) safe | (b) balanced | (a) aggressive | (q) quit
```

* `s` → lower risk, longer rallies
* `b` → balanced trade-off between consistency and pace
* `a` → higher risk, higher chance to finish the point early
* `q` → quit the current match

The system simulates each point as a rally. Every shot follows a simplified parabolic trajectory, and net clearance plus landing depth feed back into the point outcome.

The terminal flow also gives a quick read on match pressure and the CPU posture before each point, then follows up with a short rally summary after the exchange. The screen is redrawn between interactions so the terminal stays clean instead of stacking old frames.

## Example Output

```text
Choose difficulty:
  (e) Easy
  (n) Normal
  (h) Hard
  (q) Quit
n
Normal: balanced match conditions.

Score: 0-0
Level score. No one has the edge yet.
Cpu read: holding a neutral court position.
Choose your shot:
  (s) Safe
  (b) Balanced
  (a) Aggressive
  (q) Quit
b
Balanced: trade margin for pace.
Player took a measured rally by striking first. The ball traveled with heavy net clearance and landed through the middle third. Player: balanced. Cpu: balanced.
That ball pushed deep and rushed the reply.

Score: 15-0
Player is ahead. A solid point here would tighten control.
Cpu read: stepping in and looking to take time away.
Choose your shot:
  (s) Safe
  (b) Balanced
  (a) Aggressive
  (q) Quit
a
Aggressive: press for a short point.
Cpu took a short exchange by drawing the mistake. The ball traveled with a low margin over the net and landed close to the baseline. Player: aggressive. Cpu: aggressive.
Cpu absorbed the pressure and came through.

...

Score: Advantage player
```

## Goal
The goal of this project is to explore: functional programming concepts, immutable state transitions and simple game design in Clojure. 

## License
This project is licensed under the MIT License.
