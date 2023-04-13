![GitHub release](https://img.shields.io/github/release/SkJsonTeam/skJson?style=for-the-badge)
![GitHub issues](https://img.shields.io/github/issues-raw/SkJsonTeam/skJson?style=for-the-badge)
![GitHub closed issues](https://img.shields.io/github/issues-closed-raw/SkJsonTeam/skJson.svg?style=for-the-badge)
![GitHub All Downloads](https://img.shields.io/github/downloads/SkJsonTeam/skJson/total?style=for-the-badge)
[![Discord](https://img.shields.io/discord/425192525091831808.svg?style=for-the-badge)](https://discord.gg/dsZq5Cs9fd)
![License](https://img.shields.io/github/license/SkJsonTeam/skJson?style=for-the-badge)
[![CodeFactor](https://www.codefactor.io/repository/github/SkJsonTeam/skjson/badge)](https://www.codefactor.io/repository/github/SkJsonTeam/skjson)

<iframe src="https://discord.com/widget?id=1033624578653683723&theme=dark" width="350" height="500" allowtransparency="true" frameborder="0" sandbox="allow-popups allow-popups-to-escape-sandbox allow-same-origin allow-scripts"></iframe>


<p align="center" width="100%">
    <img width="60%" size="64" src="https://media.discordapp.net/attachments/967325659523321926/1089508231329624215/skJsonBanner.png"> 
</p>


#### This addon uses Google Json (Gson) API to work with Json in Skript, allowing users to edit Json files or even directly Variables that contain json.

<svg height="12px" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 127.14 96.36"><defs></defs><g id="图层_2" data-name="图层 2"><g id="Discord_Logos" data-name="Discord Logos"><g id="Discord_Logo_-_Large_-_White" data-name="Discord Logo - Large - White"><path class="cls-1" d="M107.7,8.07A105.15,105.15,0,0,0,81.47,0a72.06,72.06,0,0,0-3.36,6.83A97.68,97.68,0,0,0,49,6.83,72.37,72.37,0,0,0,45.64,0,105.89,105.89,0,0,0,19.39,8.09C2.79,32.65-1.71,56.6.54,80.21h0A105.73,105.73,0,0,0,32.71,96.36,77.7,77.7,0,0,0,39.6,85.25a68.42,68.42,0,0,1-10.85-5.18c.91-.66,1.8-1.34,2.66-2a75.57,75.57,0,0,0,64.32,0c.87.71,1.76,1.39,2.66,2a68.68,68.68,0,0,1-10.87,5.19,77,77,0,0,0,6.89,11.1A105.25,105.25,0,0,0,126.6,80.22h0C129.24,52.84,122.09,29.11,107.7,8.07ZM42.45,65.69C36.18,65.69,31,60,31,53s5-12.74,11.43-12.74S54,46,53.89,53,48.84,65.69,42.45,65.69Zm42.24,0C78.41,65.69,73.25,60,73.25,53s5-12.74,11.44-12.74S96.23,46,96.12,53,91.08,65.69,84.69,65.69Z"/></g></g></g></svg> **Discord:** https://discord.gg/trwWpUkmQp
<!-- > **Documentations**: https://cooffeerequireds.gitbook.io/skript-gson/ \ -->

### 📑 Requirements
* Java 16+
* Minecraft 1.16.5+
* Skript 2.6.4+

### 🔑 Recommended
* VSCode = [Download link](https://code-visualstudio-com.translate.goog/download?_x_tr_sl=en&_x_tr_tl=cs&_x_tr_hl=cs&_x_tr_pto=sc)
* Externsion = [Download link](https://marketplace.visualstudio.com/items?itemName=JohnHeikens.skript)
* The Extension will help you with coding and debugging skJson

[<img height="70px" src="https://skripthub.net/static/addon/ViewTheDocsButton.png">](https://skripthub.net/docs/?addon=skJson)
[<img height="70px" src="https://skunity.com/branding/buttons/get_on_docs_4.png">](https://docs.skunity.com/syntax/search/addon:skjson)
[<img height="70px" src="https://static.spigotmc.org/img/spigot.png">](https://www.spigotmc.org/resources/skjson.106019/)


## ✨ Features
### 👀 JsonWatcher 
If the file changes, your json loaded in memory changes.
```vb
on load:
    link json file "plugins/raw/raw.json" as "test"
    make jsonwatcher listen to "test"

command listenedJson:
    trigger:
        send cached json "test"
```
### 📩 JsonRequest (POST|GET)
skJson can report POST/GET requests.
1. `execute GET request to "https://dog.ceo/api/breeds/image/random%20Fetch!" with headers '{"json-encode+": "true"}'`
2. `execute POST request to "https://dog.ceo/api/breeds/image/random%20Fetch!" with headers '{"json-encode+": "true"}' and with body '{"user": "%player%"}'`
3. `set {_body} to request's body`

### 📟 Json Parser
Objects obtained from json will be automatically parsed.

Input: `set {_json} to json from location(0,0,1, world "world")`
```json
{
    "==": "org.bukkit.Location",
    "world": "world",
    "x":0.0,
    "y":0.0,
    "z":1.0,
    "pitch":0.0,
    "yaw":0.0
}
```
Output: `x: 0, y: 0, z: 1, yaw: 0, pitch: 0 in 'world'`

### 📝 Json Changer
#### ➕ ADD
Using `add` you will be able to add values only to the json sheet. Here is an example of the syntax: `add player's location to json list "pathxys" in {_json}`
#### ✔ SET
With `set` you will be able to add values to json object or to `set`, here is an example of syntax `set json value "test:A" in {_json} to diamond sword`, While `A` is the value of the key, so always the last element in the string is the definition of the key.
#### ➖ REMOVE
Using remove you will be able to remove using key or using values or using the defined index of the JsonArray case.
Here are some example syntax:
1. `remove diamond sword from {_json}`
2. `remove 2nd element from json list "pathxys" in {_json}`
3. `remove player's location from json list "pathxys" in {_json}`
4. `remove "hello" from keys of json object "pathxys" in {_json}`
5. `remove diamond sword from values of json object "pathxys" in {_json}`
