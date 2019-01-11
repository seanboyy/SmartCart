# SmartCart
A simple, lightweight transportation plugin for MineCraft.  No powered rails required!  [Download It Now!](https://www.spigotmc.org/resources/smartcart.63098/history)

## Control Blocks
These are all configurable. Default values are:
- Black Wool (spawn - use adjacent button)
- Yellow Wool (destroy)
- Green Wool (intersection)
- Red Wool (elevator)
- Orange Wool (slow)
- Gray Wool (train spawn - use adjacent button)

## Control Signs
Control signs can be placed two blocks below the rail (just under the supporting block), anywhere on the supporting block, or anywhere immediately next to the rail.  To be recognized as control signs, the sign must begin with "SC: " (by default; this can be changed).  The text that follows this prefix must be in the following format:

<code>\<setting>:\<value>|\<setting>:\<value></code>

For example:

<code>SC: $SPD:0.2 | $MSG:Ciao Amigo</code> will cause the cart speed to be set at 0.2 (half of normal speed) and will send "Ciao Amigo" to the passenger. White space is trimmed (except on the prefix).

One sign can contain as many setting/value pairs as you can fit, but they must be separated by pipe symbols (<code>|</code>), and a single setting/value pair should not span lines on the sign ($MSG is an exception).
If you have multiple lines of settings and values, remember to add a pipe symbol between the lines; it won't be added automatically.  It is also extrememly important to not add colons or pipes anywhere except as seperators.
Below is a list of currently supported settings and values.  If you would like to see more, please open an issue here on GitHub. 

| Setting | Example | Description |
|:--------|:-------:|:------------|
| $AMT | <code>$AMT:N2</code> | Amount - used for spawning trains. Tries to spawn a number of carts opposite the direction specified, then launches the assembled train.  If less than one, will be one. If greater than server max, will be server max. See below for more information |
| $HOLD | <code>$HOLD</code> | Hold - pauses the minecart while the sign with $HOLD is powered by redstone. Once power is removed, the cart releases |
| $LOCK | <code>$LOCK</code> | Lock - Makes players unable to leave cart until unlocked or the cart is destroyed |
| $UNLOCK | <code>$UNLOCK</code> | Unlock - Unlocks the cart, allowing players to leave the cart |
| $PLM | <code>$PLM:2</code> | Plummet - Speeds up a cart falling off the edge of track |
| $LEV | <code>$LEV</code> | Levitate - Slows down a cart falling off the edge of track |
| $SPD | <code>$SPD:0.2</code> | Speed - Sets the speed of the cart. 0.4 is vanilla max cart speed. Must be numeric and within the bounds of the server settings. Leading zero and decimal are optional.|
| $MSG | <code>$MSG:Hi There!</code> | Message - Sends the value text to cart's passenger. Do not use colons (<code>:</code>) or pipes (<code>\|</code>) in the text.|
| $END or $TAG | <code>$END:Oz</code> or <code>$TAG:Oz</code> | Endpoint/Tag - Sets the endpoint that this cart will attempt to reach.  After setting $END, you can use signs under intersections (no wool required) to direct the cart to the correct endpoint. See example below.|
| Oz (example) | <code>Oz:W</code> | Endpoint direction - This instructs the cart which direction to go at an intersection (no wool required) to reach the endpoint on the sign.|
| $DEF | <code>$DEF:S</code> | Default direction - This instructs the cart which direction to go at an intersection (no wool required) if no other directions are matched.|
| $LNC | <code>$LNC:N</code> | Launch - launches cart when spawned. Will not trigger the spawn message asking player to move in a direction.|
| $EJT | <code>$EJT:N3</code> | Eject - ejects extends the yellow wool block to put the player a specified number of blocks away. Can have multiple $EJT on the same sign to make more complicated ejects. Valid directions are U, D, N, S, E, W.|
| $N or $S or $E or $W | <code>$N:E</code> | Direction - will check for carts coming from the first direction, and send them to the second direction.|


## Command
There is now a command to add a tag/endpoint to a cart while riding that cart. to use it, type /scSetTag <tagname>

## Endpoints/Tags & Intersections

This can be slightly confusing, so here is a full explanation.  With SmartCart, you can affix a label to a cart that tells it where it should end up.  For example, when a cart passes over $END:Oz or $TAG:Oz, from then on the cart knows it is headed to Oz. Any time it encounters a control sign, it checks the sign for directions (in the format of Endpoint/Tag:Direction, where Endpoint/Tag would be "Oz" and Direction would be "N", "S", "E", or "W".  If the cart encounters a sign with directions, it attempts to move in that direction.  These directions are only useful under intersections.  Wool is not required for Endpoints to function, and will be ignored if valid directions (or $DEF) are encountered. Regardless, is recommended to use intersection wool as a fallback unless you use $DEF, in case someone hops on the track in the middle and misses the initial endpoint assignment.

## Trains

#### Spawning

Apply a redstone signal to a gray wool block with an $AMT sign near it. If there is no $AMT sign, nothing will happen.

Carts will spawn opposite the direciton specified in the $AMT sign.

Immediately after spawning, the carts will attempt to move in the direction specified in the $AMT sign.

#### Behaviour

All carts in a train move together. All actions that affect one cart affect all carts.

Trains cannot do everything that normal carts can do.

- Example 1
- Example 2



## Requirements
This plugin is built against the [Spigot](http://www.spigotmc.org) Minecraft server.  Your mileage may vary with other servers.

To install, simply place the JAR in your plugins folder.

## Roadmap
Currently taking suggestions!

## Configuration
The following config.yml options are available:

| Keyword | Default | Description |
|:--------|:-------:|:------------|
| boost_empty_carts | false | Plugin ignores empty carts unless true |
| normal_cart_speed | 0.4 | This is the speed carts run at by default |
| max_cart_speed | 0.4 | The fastest a cart will travel.  Expect problems with higher values. |
| slow_cart_speed | 0.1 | Adjust to change the speed when traveling over slowing blocks |
| pickup_radius | 3 | How many blocks away a freshly spawned cart will look for a player to grab |
| control_sign_prefix_regex | "^\\s\*SC: " | A sign with text matching this regex will be considered a control sign |
| elevator_block_material | "RED_WOOL" | A block with this material will be treated as an elevator block |
| intersection_block_material | "GREEN_WOOL" | A block with this material will be treated as an intersection block |
| kill_block_material | "YELLOW_WOOL" | A block with this material will kill carts |
| slow_block_material | "ORANGE_WOOL" | A block with this material will slow carts |
| spawn_block_material | "BLACK_WOOL" | A block with this material will spawn carts once a redstone signal is applied |
| train_spawn_block_material | "GRAY_WOOL" | A block with this material will spawn a series of carts in a line, according to a sign below it. If no sign is present, just spawns one cart. Uses redstone to activate |
| max_train_length | 10 | $AMT signs with values greater than this will instead produce this many carts |
| empty_cart_timer | 10 | Number of seconds before an empty cart will despawn |
| empty_cart_timer_ignore_storagemincart | true | empty_cart_timer is ignored for storage carts if true |
| empty_cart_timer_ignore_spawnermincart | true | empty_cart_timer is ignored for spawner carts if true |
| empty_cart_timer_ignore_poweredmincart | true | empty_cart_timer is ignored for powered carts if true |
| empty_cart_timer_ignore_hoppermincart | true | empty_cart_timer is ignored for hopper carts if true |
| empty_cart_timer_ignore_explosiveminecart | true | empty_cart_timer is ignored for explosive carts if true |
| empty_cart_timer_ignore_commandminecart | true | empty_cart_timer is ignored for command carts if true |

## Contribute
If you have specific suggestions or if you find a bug, please don't hesitate to open an issue.  If you have time clone this repo and submit a pull request for your idea, go for it!

## Notice
This software mimics many of the functions of previous (abandoned) minecart plugins, but is written 100% from scratch.

## License
SmartCart is distributed under the MIT license.  Be free!!
