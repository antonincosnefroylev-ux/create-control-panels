# CC: Tweaked in-game test (branch `cc-improvements`)

## 1. Start the game

Use the dev client (CC: Tweaked is already on its classpath):

```
./gradlew :1.21.1-neoforge:runClient      # or :1.20.1-forge:runClient
```

(Set `JAVA_HOME` to a JDK 17+ first.) Create a **creative, flat** world.

## 2. Build the rig

```
   [Main] [Reactor] [Spare]          [Loner]      <- consoles, all on the floor,
     ^                                               same facing, same slope,
 [Computer]                                          same casing
```

- Place **three consoles side by side** (left to right as you face their front). They join when the casing frame runs round all three as one surface. Leave the slope at the default and don't change the casing on just one of them. Sloped consoles only join side by side, not front to back.
- Place a fourth console, **Loner**, a couple of blocks away so it doesn't join.
- Put an **Advanced Computer** touching **Main** (any side of the console block).

Fill them in the designer. Mount the parts in this order, because components are numbered by mounting order:

| Console | Name (designer, nothing selected → *Console Name* → Set Name) | Parts, in order |
|---|---|---|
| Main | leave **unnamed** | 1 toggle switch (no frequency), 2 lamp (no frequency), 3 bar meter (no frequency), 4 knob (no frequency), 5 lamp (frequency **iron ingot + redstone**) |
| Reactor | `Reactor` | 1 toggle switch, 2 lamp (both without a frequency) |
| Spare | `Spare` | 1 button, 2 lamp (both without a frequency) |
| Loner | `Loner` | 1 toggle switch (no frequency) |

Somewhere nearby (not touching a console): a Create **Redstone Link** in **receiver** mode set to **iron ingot + redstone**, with a redstone lamp next to it. Put a second Redstone Link in **transmitter** mode, set to **gold ingot + redstone**, on a lever.

## 3. Copy the programs onto the computer

Open the computer and **drag all the `.lua` files from this folder onto its screen**. CC: Tweaked uploads them, and the names lose the `.lua`. If dragging doesn't work, copy them into
`run/1.21.1/client/saves/<world>/computercraft/computer/<id>/`, where `<id>` is the number `id` prints on the computer.

## 4. Tests

| # | Run | Do / look for | Expect |
|---|---|---|---|
| 1 | `cclist` | read the list | 3 consoles: Main first and marked `<- touched`, then Reactor and Spare. **Loner is absent.** Main #5 shows `iron_ingot + redstone`; everything else shows `unlinked`. |
| 2 | `ccselftest` | read the result | All PASS (`… passed, 0 failed`). |
| 3 | `ccunlinked` | watch Main | Lamp 2 and meter 3 sweep up, then hold at 8. **The Create receiver's lamp stays off.** |
| 3b | (still running) | flip switch 1, turn knob 4 on Main | Lamp 2 and meter 3 follow: switch on → 15, knob → its value. |
| 3c | Ctrl+T, then hold **Ctrl+S** (shut down) | watch Main | Lamp 2 and meter 3 **go dark**. The switch and knob stay where they were. |
| 4 | `ccwatch` | flip switches and press buttons on Main, Reactor and Spare, then on Loner | One line per change, naming `Reactor` / `Spare` or Main's x,y,z. **Nothing for Loner.** |
| 5 | `ccunlinked Reactor` | flip Reactor's switch | Reactor's lamp follows (selection by name). |
| 6 | `ccmirror - 1 Reactor 2` | flip Main's switch 1 | Reactor's lamp 2 follows (works across consoles). |
| 6b | `ccmirror Spare 1 - 2` | press Spare's button | Main's lamp 2 blinks for the button's pulse. |
| 7 | `ccfreq 5 iron_ingot redstone`, then `lua` and type `peripheral.find("control_console").setValue(5, 15)` | watch the Create receiver | It lights (a linked lamp still sends on its frequency). `setValue(5, 0)` turns it off. |
| 8 | `ccfreq 2 gold_ingot redstone` | open Main's designer and select lamp 2 | It shows gold ingot + redstone. |
| 8b | (close the designer) | flip the lever on the gold transmitter | Main's lamp 2 follows the lever (the frequency set from CC works on the network). |
| 8c | `ccfreq 2 - -` | `cclist` | Lamp 2 is back to `unlinked` and **dark**. |
| 9 | Name Spare `Reactor` too, then `ccselftest` | read the result | "shared name 'Reactor' refused" is listed as PASS. Rename it `Spare` afterwards. |
| 10 | Break Reactor and place it back from its item | `cclist` | Reactor is listed again, with its name. |
| 11 | Break Spare (the group is now Main + Reactor) | `cclist` | Two consoles. |

Things to note if they come up: whether a frequency set from CC shows in the designer right away, how the name field feels, and any Lua error messages that read badly.
