# djoko

> [!IMPORTANT]
> This project is minimal.

A small tennis game simulator written in Clojure.

## Functionalities

* Point progression (0, 15, 30, 40)
* Game simulation (player vs CPU)
* Simple decision system: Defensive play and Aggressive play.
* Randomized point outcomes
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

During the game, you will be prompted to choose an action:

```text
Escolha: (d) defensiva | (a) atacar
```

* `d` → safer play (more consistent, lower risk)
* `a` → aggressive play (higher risk, higher reward)

The system will simulate the point and update the score.

## Example Output

```text
Placar: {:p1 0, :p2 0}
Escolha: (d) defensiva | (a) atacar
d
Ponto para: :p1

Placar: {:p1 15, :p2 0}
Escolha: (d) defensiva | (a) atacar
a
Ponto para: :p2

...

Vencedor: :p1
```

## Goal
The goal of this project is to explore: functional programming concepts, immutable state transitions and simple game design in Clojure. 

## License
This project is licensed under the MIT License.