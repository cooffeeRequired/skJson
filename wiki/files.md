<p align="center" style="align: center; text-align: center">
<img align="center" alt="SkJson" width="40%" src="https://skjson.xyz/imgs/skjson/banner.png">
</p>

<h1 align="center">Pages - Files</h1>

> Working with files is always a bit of a nuisance, let's see how we can work with files.
> The first thing to mention is that we already know the syntax for reading a file and >creating a json object from it,
> namely `json from file "..."` and what's next?
>
>### Creating a file
>Suppose we have the path `./plugins/Script/Jsons/test.json` but this file does not >exist. Let's create one
>```applescript
>on script load:
>    # without content & without check
>    new json file "./plugins/Script/Jsons/test.json"
>
>    # without content /w check
>    if json file "./plugins/Script/Jsons/test.json" doesn't exist:
>        new json file "./plugins/Script/Jsons/test.json"
>
>    # with content /w check
>    # Suppose we want to write some content
>    set {_content} to json from "{'A': 'Test'}
>    if json file "./plugins/Script/Jsons/test.json" doesn't exist:
>        new json file "./plugins/Script/Jsons/test.json" with content {_content}
>```
>
>
>### Writing
>Consider that we have created a variable `{-JSON}` which contains json from some >loaded file
>```applescript
>write {-JSON} to json file "./plugins/Script/Jsons/test.json"
>```
>
>### Changes?
>The changes to the dirrectly file are kind of meh, but let's show it. Suppose we have >a
> file `/plugins/Script/Jsons/test.json` and this json spits out like this `{"data": >[{"username": "Franta"}]}`, let's
> change "Franta"
>```applescript
>on script load:
>
>    # /w edit effect
>    edit value "data[0]::username" of json file "/plugins/Script/Jsons/test.json" to >"Jakub"
>    # Now the /plugins/Script/Jsons/test.json file will look like this
>    # {"data": [{"username": "James"}]}
>
>    # /wo edit effect
>    set {_json} to json from file "/plugins/Script/Jsons/test.json"
>    set value of json object "data[0]::username" in {_json} to "Jakub"
>    write {_json} to json file "./plugins/Script/Jsons/test.json"
>```