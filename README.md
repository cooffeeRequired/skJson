![GitHub release](https://img.shields.io/github/release/SkJsonTeam/skJson?style=for-the-badge)
![GitHub issues](https://img.shields.io/github/issues-raw/SkJsonTeam/skJson?style=for-the-badge)
![GitHub closed issues](https://img.shields.io/github/issues-closed-raw/SkJsonTeam/skJson.svg?style=for-the-badge)
![GitHub All Downloads](https://img.shields.io/github/downloads/SkJsonTeam/skJson/total?style=for-the-badge)
[![Discord](https://img.shields.io/discord/425192525091831808.svg?style=for-the-badge)](https://discord.gg/dsZq5Cs9fd)
![License](https://img.shields.io/github/license/SkJsonTeam/skJson?style=for-the-badge)
[![CodeFactor](https://www.codefactor.io/repository/github/SkJsonTeam/skjson/badge)](https://www.codefactor.io/repository/github/SkJsonTeam/skjson)
[![Java CI with Maven](https://github.com/SkJsonTeam/skJson/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/SkJsonTeam/skJson/actions/workflows/maven.yml)



<br />

[//]: # (<- Header ->)
<p align="center" style="align: center; text-align: center">
<img align="center" alt="SkJson" width="40%" src="https://skjson.xyz/imgs/skjson/banner.png">
</p>
<h1 align="center">SkJson</h1>

<h3 align="center">The modern way how to handle JSON in the Skript</h3>
<h6 align="center">The addon uses GSON &#40;Google JSON&#41; for work with JSON in Skript</h6>
<hr>

### 📑 Requirements

* **Java 17+**
* **Minecraft 1.16.5+**
* **Skript 2.8.0 +**

### 🔑 Recommended

* **[**Visual Studio Code**](https://code-visualstudio-com.translate.goog/download?_x_tr_sl=en&_x_tr_tl=cs&_x_tr_hl=cs&_x_tr_pto=sc)**
* **_[**Extension**](https://marketplace.visualstudio.com/items?itemName=JohnHeikens.skript)_**

### 🆘 Where can I get help?

* **[Discord](https://discord.gg/dsZq5Cs9fd)**
* **[SkUnity](https://skunity.com/)**
* **[Email](mailto:nextikczcz@gmail.com)** (Only the biggest projects)

### 💡 How can I start with SkJson?

#### All tutorials are based on the latest version **`2.9`**

for the first time and recommend working with [SkJson Documentation](https://skjson.xyz/)

#### 🗝️ Create json object/array from sources.

```shell
on script load:
  # From String
  # We can use escape sequences so two double quotes, or we can use single quote
  # Or we can define only KEYS without any quotes
  set {_json} to json from text "{""A"": false}"
  set {_json} to json from text "{'A': false}"
  set {_json} to json from text "{A: false}"
  
  # From file (json/yaml)
  # YAML: you can your old yaml file and SkJson will convert that to JSON file
  set {_json} to json from json file "plugins/SkJson/Files/test.json"
  set {_json} to json from yaml file "plugins/SkJson/Files/test.yaml"
  
  # From website
  # That will work only for GET! If you want to use other methods you should use requests
  set {_json} to json from website "https://dummyjson.com/products/1"
  
  # From any Skript/Bukkit object
  set {_json} to json from location(10, 20, 30, world("world"))
```

This depends on what you want to do in SkJson, if you just want to work with JSON you can just use `Map/From` but if you
want for example `Request's` or work with files we have a small guide here.

#### 🗝️ Example for requests.

Suppose we have some API and we want to use Skript to work with that API and get JSON responses from that API (server),
SkJson offers [Request's](https://skjson.xyz/documentation/beta#section-Request) according to its own.

```sh
on script load:
	async make POST request to "https://dummyjson.com/carts/add":
		header: "Content-Type: application/json"
		content: json from text "{userId: 1, products: [{id: 1, quantity: 1}, {id: 50, quantity: 2}]}"
		save incorrect response: true
		lenient: true
		save:
			content: {-content}
			headers: {-header}
			status code: {-code}
			url: {-url}
command response:
	trigger:
		send {-content} with pretty print
```

What does mean `lenient` ?
**__`lenient`__** means attempting to repair corrupted JSON from a server response. (thanks to `@mnight4`)
<br />

#### 🗝️ Example for handling JSON file /wo cache.

SkJson can work with files whether it is writing, editing or appending. See
also [Write](https://skjson.xyz/documentation/beta#effect-Write), [New](https://skjson.xyz/documentation/beta#effect-New), [Edit](https://skjson.xyz/documentation/beta#effect-Edit)

```sh
# here we will create a new file
options:
    file_path: "plugins/SkJson/jsons/test.json"

on script load:
    new json file {@file_path} if json file {@file_path} does not exist
    
    # here we will work with the json file
    
    set {_json} to json from file {@file_path}
    
    # writing to file
    set {_data} to json from location(10, 20, 30, world("world"))
    write {_data} to json file {@file_path}
    
    # editing directly file
    edit value "world" of json file {@file_path} to "New World"
    
    # editing file with step over
    
    # getting the json file as Json object
    set {_json} to json from file {@file_path}
    set value of json object "world" in {_json} to "New World(By rewrote)"
    
    # write file back to JSON
    write {_json} to json file {@file_path}
```

#### 🗝️ Example for handling JSON file /w cache.

What is `cache`? Cache is known for storing JSON in memory instead of SkJson having to open and close the file it puts
its reference in memory, and you are working with memory all the time and if you would like to save the file from memory
to a real file. you can do it at any time with `save <json-id>`

Check out this
documentation.: [Write](https://skjson.xyz/documentation/beta#effect-Write), [New](https://skjson.xyz/documentation/beta#effect-New), [Edit](https://skjson.xyz/documentation/beta#effect-Edit), [Link File](https://skjson.xyz/documentation/beta#effect-LinkFile), [Save File](https://skjson.xyz/documentation/beta#effect-SaveCache), [Unlink File](https://skjson.xyz/documentation/beta#effect-UnlinkFile), [Get Cached JSON](https://skjson.xyz/documentation/beta#expression-GetCachedJson)

```sh
options:
    file_path: "plugins/SkJson/jsons/test.json"

on script load:
    # here we will create a new file
    new json file {@file_path} if json file {@file_path} does not exist
    # here we will linked our file to our memory.
    link json file {@file_path} as "your_specified_value" if json file {@file_path} exists
    
    # here we will set value to memory reference of your file.
    set value of json objct "location" in (json "your_specified_value") to location(10, 20, 30, world("world"))
    
    # here we will get location of memory
    set {_location} to value "location" of (json "your_specified_value")
    # that will return {"==":"org.bukkit.Location","yaw":0.0,"world":"world","x":10.0,"y":20.0,"z":30.0,"pitch":0.0}
    
    # here we will save memory reference back to file
    save json "your_specified_value"
```

#### 🗝️ Now we'll look at is how SkJson works with `Skript/Bukkit` objects

```sh
# let's say we have a command test and we work with Player Location. 
command test:
  trigger:
    set {_json_location} to json from location of player
    teleport player to {_json_location}
    # that will teleport player to location converted from JSON object to location
    
    set {_item} to diamond sword named "Test"
    set lore of {_item} to "&6Gold" and "&7Silver"
    enchant {_item} with Sharpness 5
    set {_json_item} to json from {_item}
    
    give {_json_item} to player
```

So conclusion, if the Json object contains the correct object key `Skript/SkJson` will try to parse the JSON as a real
object.
<hr />

[<img style="width: 20%" src="https://skripthub.net/static/addon/ViewTheDocsButton.png">](https://skripthub.net/docs/?addon=skJson)
[<img style="width: 22%" src="https://skunity.com/branding/buttons/get_on_docs_4.png">](https://docs.skunity.com/syntax/search/addon:skjson)
[<img style="width: 10%" src="https://static.spigotmc.org/img/spigot.png">](https://www.spigotmc.org/resources/skjson.106019/)
