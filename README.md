# MultiSting

![Bees flying](https://i.imgur.com/TFgJFnV.png)

**Tired of your bees running out of stinger? Want bees to hate you more?** Then this mod is for you! Make bees sting to your heart's content!

**Hate the fact that bees care when you hurt them?** Well, just open up the configuration and make the bees never have a stinger in the first place! This mod truly lets you do it all! If "all" is changing bee stingers, of course.

## Where's that pesky configuration file?

![.minecraft/saves/worldname/serverconfig/multisting-server.toml](https://i.imgur.com/x7s6DnL.png)

## You're gonna make me copy the configuration file / how do I make this work in modpacks?

Use the Forge "defaultconfigs" folder! Just copy "multisting-server.toml" (as seen in the above image) to the "defaultconfigs" folder in the .minecraft folder (as seen in the above image, as well).

## How do I use the thing?

Install the mod, and then customize your configuration! See above how to get to your configuration.

Here's the default configuration:

```
#How many times a bee can sting. -1 = infinite, 0 = never, 1 = normal behaviour
#Range: > -1
sting_amount = -1
```

As you can see, just change the number after `sting_amount=` to how many stings you want the bees to have:
 - -1 will result in infinite stings (this is the default behaviour)
 - 0 will result in no stings (EVER!)
 - Any other number (>0) will result in that many stings.
