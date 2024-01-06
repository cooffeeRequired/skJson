<p align="center" style="align: center; text-align: center">
<img align="center" alt="SkJson" width="40%" src="https://skjson.xyz/imgs/skjson/banner.png">
</p>

<h1 align="center">Pages - Mapping</h1>

## ⚠️ Obsolete

>Mapping/Formatting is a way to get values from `Skript Variable` or conversely how to >make `json` from `Skript Variable`
>
>### Mapping
>```applescript
>    set {_json} to json from "{'A': true, 'B': 'Test', 'C': {'Another': 'player'}}"
>    map {_json} to {_json::*}
>
>    send {_json::C::Another}
>    # returns player
>```
>
>### Formatted
>```applescript
>    set {_format::test} to false
>    set {_format::test2::username} to "Jakub"
>    set {_format::test2::password} to "Some"
>
>    send {_format::*}'s form
>    # returns {"test": false, "test2": {"username": "Jakub", "password": "Some"}}
>```