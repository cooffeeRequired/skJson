<p align="center" style="align: center; text-align: center">
<img align="center" alt="SkJson" width="40%" src="https://skjson.xyz/imgs/skjson/banner.png">
</p>

<h1 align="center">Pages - Serialization</h1>

> What we can imagine by `Serialization/Deserialization` we can talk about it as some kind of conversion of an object into some form for example into `text` or into `base64` or just into `json`. Minecraft is a lot of object whether it is `Location, Block, Item etc.` and therefore here we have a way to convert the whole object simply to `json` and back to `json` to the given object.. **How?** `SkJson` actually takes care of this in the background all transcreations of data are checked and then converted if they meet the given conditions for serialization... **Need an example**.. Let's write a little script to store the player's Invetary.

> ```applescript
> command saveInv:
>     trigger:
>         set {inv::%player's uuid%} to json from player's inventory
>         send {inv::%player's uuid%} with uncoloured pretty print to console
> 
> command loadInv:
>     trigger:
>         set player's inventory to {inv::%player's uuid%}
> ```
> 
> **How does Inventory Json look like?** *Exactly like this*
> 
> ```json
> {
>   "..": "org.bukkit.craftbukkit.v1_20_R2.inventory.CraftInventoryPlayer",
>   "title": "Crafting",
>   "type": "PLAYER",
>   "holder": "_F0cus__",
>   "contents": {
>     "Slot 0": {
>       "==": "org.bukkit.inventory.ItemStack",
>       "v": 3578,
>       "meta": {
>         "==": "ItemMeta",
>         "meta-type": "UNSPECIFIC",
>         "Damage": 2
>       },
>       "type": "IRON_SWORD"
>     },
>     "Slot 1": {
>       "==": "org.bukkit.inventory.ItemStack",
>       "v": 3578,
>       "meta": {
>         "==": "ItemMeta",
>         "meta-type": "UNSPECIFIC",
>         "Damage": 114
>       },
>       "type": "IRON_SHOVEL"
>     },
>     "Slot 2": {
>       "==": "org.bukkit.inventory.ItemStack",
>       "v": 3578,
>       "meta": {
>         "==": "ItemMeta",
>         "meta-type": "UNSPECIFIC",
>         "Damage": 107
>       },
>       "type": "STONE_PICKAXE"
>     },
>     "Slot 3": {
>       "==": "org.bukkit.inventory.ItemStack",
>       "v": 3578,
>       "meta": {
>         "==": "ItemMeta",
>         "meta-type": "UNSPECIFIC",
>         "Damage": 48
>       },
>       "type": "STONE_AXE"
>     },
>     "Slot 4": null,
>     "Slot 5": null,
>     "Slot 6": null,
>     "Slot 7": null,
>     "Slot 8": null,
>     "Slot 9": null,
>     "Slot 10": null,
>     "Slot 11": null,
>     "Slot 12": null,
>     "Slot 13": null,
>     "Slot 14": null,
>     "Slot 15": null,
>     "Slot 16": null,
>     "Slot 17": null,
>     "Slot 18": null,
>     "Slot 19": null,
>     "Slot 20": null,
>     "Slot 21": null,
>     "Slot 22": null,
>     "Slot 23": null,
>     "Slot 24": null,
>     "Slot 25": null,
>     "Slot 26": null,
>     "Slot 27": null,
>     "Slot 28": null,
>     "Slot 29": null,
>     "Slot 30": null,
>     "Slot 31": null,
>     "Slot 32": null,
>     "Slot 33": null,
>     "Slot 34": null,
>     "Slot 35": null,
>     "Slot 36": null,
>     "Slot 37": null,
>     "Slot 38": null,
>     "Slot 39": null,
>     "Slot 40": null
>   }
> }
> ```


> as said you don't need to know any magic syntax or spells to serialize/deserialize data, let's show one more small example. Let's say we want to create a Json right away  that will contain a serialized position. Let's take a look at how to do this properly.
> 
> ```applescript
> on script load:
>     # Wrong
>     set {_json} to json from "{'Location': 'location(0,0,0)', 'Test': true}"
>     send {_json}
>     # {_json} = {"Location":"location(0,0,0)","Test":true}
> 
>     # Wrong
>     set {_json} to json from "{'Location': '%location(0,0,0)%', 'Test': true}"
>     send {_json}
>     # {_json} = "{'Location': x: 0, y: 0, z: 0, yaw: 0, pitch: 0 in 'world', 'Test': > true}"
> 
>     # Correct
>     set {_json} to json from "{'Location': %json from location(0,0,0)%, 'Test': true}"
>     send {_json}
>     # {_json} = {"Location":{"==":"org.bukkit.Location","yaw":0.0,"world":"world", "x":0.0,"y":0.0,"z":0.0,"pitch":0.0},"Test":true}
> 
>     # Now we will get the location from the Json back to Object
>     set {_loc} to value "Location" of {_json}
>     # {_loc} =  x: 0, y: 0, z: 0, yaw: 0, pitch: 0 in 'world'
> ```
> 
> And that's it for the data serialization... It's simple. Let's move on