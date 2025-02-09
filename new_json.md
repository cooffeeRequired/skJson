<p align="center" style="align: center; text-align: center">
<img align="center" alt="SkJson" width="30%" src="https://i.ibb.co/zV3Pxht/New-Project-4.png">
</p>

<h1 align="center">Creation of Json</h1>

### Json creation options
- from the string:
    - Creating from a string is simple, but the syntax must be followed.
    > [!IMPORTANT]
    > In scriptLang it is not possible to escaping strings as you know it, namely `""` but in scriptLang you escaping by doubling i.e. `""` and thus the string you want to serialize must look like that
    >
    ```coffeescript
    set {_apple} to "new apple"

    set {_json} to json from "{"key": 1}" # ❌ this is not correct, the mistake here is not using escape.
    set {_json} to json from "{""key"": {_apple}}" # ❌ this is not correct because the apple variable is not called in the string, the correct way would have to be using ""%{_apple}%"" for example

    # Example of correct usage
    set {_json} to json from "{""key"": 1}"
    set {_json} to json from "{key: 1}"
    set {_json} to json from "{'key': 1}"
    # here we have 3 options for how to properly write this json string for serialization.
    # however, the value 1 is an integer and thus does not need to be in quotes, otherwise every string must be in quotes.
    ```
- from the object
  - Creating from a object is simple, that means you can serialize any bukkit object that is commonly used
  > [!IMPORTANT]
  > If you want to serialize something that is not supported, you can create a new issue on github and I'll be happy to take a look.
  >
  ```coffeescript
  set {_location} to location of player
  set {_json} to json from {_location}

  # of course it can be done without creating a {_location} variable
  set {_json} to json from location of player

  # Here's an example of how to create a json from player's inventory
  set {_json} to json from inventory of player
  ```
- from the file
    - as with the previous two points, here it is similar and quite simple
    > [!IMPORTANT]
    > The file from which you want to create the json must exist and must always have the correct syntax, if `SkJson` detects that the syntax is not correct, the file will not be read and the json will not be created. \
    Also, the path to the file must always start at root i.e. the root folder of your server \
    This is only different if you use `~` in the file path, then you say that `SkJson` should start in the `plugins/Scripts/scripts` folder.
    >
    ```coffeescript
    set {_file_location} to "./plugins/jsons/myJson.json"
    set {_file_loc} to "~myjson.json"
    set {_json} to json from file {_file_loc}
    ```
- from the web response (request)
  - and therefore the same applies!
  > [!IMPORTANT]  
  > in this case, the server proof runs synchronously with the server thread! \
  > all requests are made using the `GET` method
  > if you would like to use another method you must use [**requests**](./work_with_request.md)
  >
  ```coffeescript
  set {_json} to json from website "https://dummyjson.com/test"
  ```


<center>

[<img style="width: 10%; margin-right: 1rem;" src="https://skripthub.net/static/addon/ViewTheDocsButton.png">](https://skripthub.net/docs/?addon=skJson)
[<img style="width: 12%; margin-right: 1rem;" src="https://skunity.com/branding/buttons/get_on_docs_4.png">](https://docs.skunity.com/syntax/search/addon:skjson)
[<img style="width: 5%" src="https://static.spigotmc.org/img/spigot.png">](https://www.spigotmc.org/resources/skjson.106019/)

</center>