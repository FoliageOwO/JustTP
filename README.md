# JustTP

A lightweight mod that lets any player use the vanilla `/tp` command, no OP or cheat mode, no `/tpa` or request/accept
flow.

This mod overrides the vanilla `/tp` command:

- All players can use `/tp` without OP or any permissions.
- For fair use, `/tp` only supports selector `@s`. Selectors like `@e`, `@p`, `@r`, UUID targets, and multi-target usage
  are not supported (e.g. `/tp @e @s`, `/tp <uuid>`).

Players can still teleport to other players or coordinates just like vanilla `/tp` does.

Example available commands:

- `/tp Player`
- `/tp x y z`
- `/tp PlayerA PlayerB`
- `/tp @s Player`
- `/tp Player @s`
- `/tp @s x y z`
- `/tp Player x y z`

Example configuration file (`justtp-common.toml`):

```toml
#Whether /tp supports coordinates
enableCoordinateTp = true
#Teleport message mode: OFF, BOTH, ALL
#Allowed Values: OFF, BOTH, ALL
tpMessageMode = "ALL"
```
