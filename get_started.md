<p align="center" style="align: center; text-align: center">
<img align="center" alt="SkJson" width="30%" src="https://i.ibb.co/zV3Pxht/New-Project-4.png">
</p>

<h1 align="center">Introduction</h1>

### What is SkJson?
> [!NOTE]
> - `SkJson` is a tool developed for `SkriptLang`, which extends the above mentioned > SkriptLang by the possibility of processing `.json|.jsonc` file, it allows to > serialize/deserialize various objects that are freely available in MC, it is also > possible to read and write files or to link files to the so-called `cache`.
> 
> - As for the mentioned `Cache`, it serves as a place in ram memory from where writing > and reading different files is extremely fast, and allows easy manipulation of files > directly in the code.
> 
> - `Skjson` also supports working with `WebRequests` and hence you can send web > requests of type *`POST, GET, PUT, PATCH, HEAD, MOCK, DELETE`* directly from the > Script and process the response directly.
> 
> ### Simple example of **JSON**
> ```json
> {
>     "object": {},
>     "array": [],
>     "string": "Hello world!",
>     "integer": 1,
>     "float": 1.1,
>     "boolean": true,
>     "null": null
> }
> ```
> - As for the `.jsonc` format, it is similar, but it can also contain comments
> - ❗❗ **`JSONC`** is not yet available, but is planned for `SkJson 4.6`
> ```jsonc
> {
>     // this serves as a sample JSONC
>     "object": {}, // this is an object
>     "array": [], // this is an array
>     "string": "Hello world!",
>     "integer": 1,
>     "float": 1.1,
>     "boolean": true,
>     "null": null
> }
> ```

### Usage of `SkJson`
> [!IMPORTANT]
> - [`create new json`](./new_json.md) - here we will > show how to easily create `json` wishes in the > mentioned scriptlang
> - [`work with json`](./work_with_json.md) - here we > show how to work with `json`, change, delete, add
> - [`working with files`](./work_with_files.md) - here > we show how to work with files.
> - [`working with requests`](./work_with_request.md) - > here we show how to work with requests
> - [`working with cache`](./work_with_cache.md) - here > we show how to work with cache and how to link it to > files.
> 


<center>

[<img style="width: 10%; margin-right: 1rem;" src="https://skripthub.net/static/addon/ViewTheDocsButton.png">](https://skripthub.net/docs/?addon=skJson)
[<img style="width: 12%; margin-right: 1rem;" src="https://skunity.com/branding/buttons/get_on_docs_4.png">](https://docs.skunity.com/syntax/search/addon:skjson)
[<img style="width: 5%" src="https://static.spigotmc.org/img/spigot.png">](https://www.spigotmc.org/resources/skjson.106019/)

</center>