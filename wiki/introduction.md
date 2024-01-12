<p align="center" style="align: center; text-align: center">
<img align="center" alt="SkJson" width="40%" src="https://skjson.xyz/imgs/skjson/banner.png">
</p>

<h1 align="center">Pages - Introduction</h1>

## Introduction

### What is Json?

> JavaScript Object Notation (JSON) is a computer platform-independent way of writing data (data format) for
> transferring data that can be organized in arrays or aggregated in objects. The input is any data structure (a number, a
> string, a boolean, an object or an array composed of them), the output is always a string. There is no theoretical limit
> to the complexity of the input variable hierarchy.
> JSON can hold arrays of values (both unindexed and indexed, the so-called hash), objects (as arrays of index:value
> pairs) and individual values, which can be strings, numbers (integers and floating point) and special values true, false
> and null. Field indices in an object are notated as strings; strings are enclosed in quotes and escaped using a
> backslash. There can be arbitrary whitespace between elements and values, which does not change the result. JSON as a
> format does not address text encoding, but the default encoding is UTF-8.\
> Default Example of Json
> ```json
> {
>   "0": 1,
>    "1": -2,
>    "2": 3.333,
>    "3": 4.0e+17,
>    "4": "abc",
>    "5": "\u00e1\n",
>    "6": null,
>    "7": [
>        2.1,
>        2.2,
>        [
>            "2.2.1"
>        ]
>    ],
>    "8": false,
>    "9": true,
>    "10": "",
>    "key": "value",
>    "abc\"def": []
>}
>```

### What is SkJson and why was it created?

> SkJson is a tool for [Skript](https://github.com/SkriptLang/Skript/releases) which allows us to process any `json`
> or `yaml` files and then use it as storage or also allows us to work
> with `Json String`, `WebRequests`, `Discord webbhooks` and `Serialization/Deserialization of data`

### How can I use SkJson?

#### pretty print

> pretty print allows pirnt json nicelly without or with colors
> ```applescript
> set {_json} to json from "{'A': false}"
> send {_json} with pretty print
> send {_json} with uncolored pretty print
> ```

#### new json

> The very basic syntax here is to create a Json, and that's relatively easy, let's say we create a new **`Command`** in
> a Script. In addition, I'll mention here that we can *cheat* a little bit because instead of `Skript escape` we can use,
> for example, `"{'Key': 'Value'}"` with Script escape `"{""Key": ""Value""}"`, also SkJson has the ability to fix a
> corrupted Json say from forgetting quotes for some key in the object `"{Key: ""Value""}"` SkJson thanks to the GSON
> library can even load such a corrupted Json correctly.
>
> Now we will see how to create Json from Text and other sources.
> ```applescript
>command newJson:
>   trigger:
>       # Text
>       set {_json} to json from "{Key: 20}"
>       # {_json} = {"Key": 20}
>       set {_jsons::*} to json from "{Something: true}", "{Another: false}"
>       # {_jsons::*} = {"Something": true}, {"Another": false}
>       
>       # Files
>       set {_json} to json from file "<path-to-your-json>/test.json"
>       set {_json} to json from yaml file "<path-to-your-yaml>/test.yaml"
>       
>       # Objects
>       set {_json} to json from player's location
>       # {_json} = {"==":"org.bukkit.Location","yaw":0.0,"world":"world","x":0.0,"y":0.0,"z":0.0,"pitch":0.0}
>       set {_jsonItem} to json from diamond sword named "Test"
>       # {_jsonItem} = {"==":"org.bukkit.inventory.ItemStack","v":3578,"meta":{"==":"ItemMeta","meta-type":"UNSPECIFIC","display-name":"{\"extra\":[{\"text\":\"Test\"}],\"text\":\"\"}"},"type":"DIAMOND_SWORD"}
>
>       # Website
>       set {_json} to json from website file "https://support.oneskyapp.com/hc/en-us/article_attachments/202761627"      
> ```

#### Changing (SET/REMOVE/REMOVE ALL)

The changes are made using syntax. Here is an example of all of them.

```applescript
# {_d} = Delimiter from your config.yml (skjson_getdelim())


local function jsonPayload() :: json:
    return json from text "[1, {'list': [true]}]"

local function jsonObjectPayload() :: json:
    return json from text "{data: {key: 'test key'}, list: [{inner: {}}]}"

local function testArrayListADD(json: json, d: string) :: boolean:
    add "[]" to json array in {_json}
    add swords to json array "2" in {_json}
    return true if "[1,{""list"":[true]},[{""=="":""org.bukkit.inventory.ItemStack"",""v"":3465,""type"":""WOODEN_SWORD""},{""=="":""org.bukkit.inventory.ItemStack"",""v"":3465,""type"":""STONE_SWORD""},{""=="":""org.bukkit.inventory.ItemStack"",""v"":3465,""type"":""GOLDEN_SWORD""},{""=="":""org.bukkit.inventory.ItemStack"",""v"":3465,""type"":""IRON_SWORD""},{""=="":""org.bukkit.inventory.ItemStack"",""v"":3465,""type"":""DIAMOND_SWORD""},{""=="":""org.bukkit.inventory.ItemStack"",""v"":3465,""type"":""NETHERITE_SWORD""}]]" is "%{_json}%"


local function testArrayListREMOVE(json: json, d: string) :: boolean:
    add "Hello true" to json list in {_json}
    remove values 1 and "Hello true" of json list from {_json}
    add "A" to json list in {_json}
    remove 2nd element of json list from {_json}
    add diamond sword to json list "0%{_d}%list" in {_json}
    add diamond axe to json list "0%{_d}%list" in {_json}
    remove diamond axe of json list "0%{_d}%list" from {_json}
    return true if "[{""list"":[true,{""=="":""org.bukkit.inventory.ItemStack"",""v"":3465,""type"":""DIAMOND_SWORD""}]}]" is "%{_json}%"


local function testObjectSET(json: json, d: string) :: boolean:
	# {data: {key: 'test key'}, list: [{inner: {}}]}
	set value of json object "data%{_d}%key" in {_json} to "new test key"
	ann({_json}, "After set value - new test key")
	set key of json object "data%{_d}%key" in {_json} to "test key"
	ann({_json}, "After set key - test key")
	set value of json object "list[0]%{_d}%inner" in {_json} to false
	ann({_json}, "After set value - false")
	set key of json object "list[0]%{_d}%inner" in {_json} to "inner-false"
	ann({_json}, "After set key - inner-false")
	set value of json object "data" in {_json} to false
	ann({_json}, "After set value - false")
	set key of json object "list" in {_json} to "array"
	ann({_json}, "After set key - array")
	set value of json object "this-a test-what i need to <>-_%{_d}%data" in {_json} to iron sword
	ann({_json}, "After set value - iron sword")
	return true if "{""data"":false,""array"":[{""inner-false"":false}],""this-a test-what i need to <>-_"":{""data"":{""=="":""org.bukkit.inventory.ItemStack"",""v"":3465,""type"":""IRON_SWORD""}}}" is "%{_json}%"

local function testObjectREMOVE(json: json, d: string) :: boolean:
	# {data: {key: 'test key'}, list: [{inner: {}}]}
	remove key "inner" of json object "list[0]" from {_json}
	remove key "list" of json object from {_json}
	remove value "test key" of json object "data" from {_json}
	return true if "{""data"":{}}" is "%{_json}%"


local function testREMOVEALL(json: json, d: string) :: boolean:
	# {data: {key: 'test key'}, list: [{inner: {}}]}
	set value of json object "data%{_d}%key2" in {_json} to "new test key"
	set value of json object "data%{_d}%key3" in {_json} to "new test key"
	set value of json object "root-key" in {_json} to "new test key"
	remove all "new test key" of json object "data" from {_json}
	remove all "new test key" of json object from {_json}
	set value of json object "array[0]%{_d}%root" in {_json} to "new test key"
	remove all "new test key" of json list "array[0]" from {_json}
	# {'data':{'key':'test key'},'list':[{'inner':{}}],'array':[{}]}
	return true if "{""data"":{""key"":""test key""},""list"":[{""inner"":{}}],""array"":[{}]}" is "%{_json}%"

```

#### value/values (Looping)

> The basic methodology has always been to use mapping, but that is no longer needed here.
> ```applescript
> command values:
>   trigger:
>       set {_json} to json from "{'array': [1, 2, false, 'random']}"
>       loop values "array" of {_json}:
>           send json-value # 1, 2, false, random
>           send json-key # 1,2,3,4
>       
>       # Get Multiple values
>       set {_json} to json from "{'A': false, 'V': true, 'Something': {}, 'array': [true, false, null]}"
>       set {_values::*} to values of {_json}
>       # {_values::*} = false, true, {}, [true, false, null]
>
>       # Get Single value
>       set {_json} to json from "{'A': false, 'V': true, 'Something': {}, 'array': [true, false, null]}"
>       set {_value} to value 'A' of {_json}
>       # {_value} = false       
> ```

#### type of json / counts / isEmpty

> Suppose we want to find out what type of json we have stored in variable..., or also how many identical elements our
> json contains
> ```applescript
>local function test(input: json, maxSameElements: number, value: object) :: boolean:
>    if json element {_input} isn't empty:
>        if type of {_input} is json object:
>            if {_input} has value {_value}:
>                set {_same} to number of value {_value} in {_json}
>                if {_same} <= {maxSameElements}:
>                    return true
>
>    return false
>
>command checkJson:
>    trigger:
>        set {_json} to json from text "{'A': 1, 'B': 1, 'C': 1}"
>        send test({_json}, 3, 1)  
> ```