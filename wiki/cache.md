<p align="center" style="align: center; text-align: center">
<img align="center" alt="SkJson" width="40%" src="https://skjson.xyz/imgs/skjson/banner.png">
</p>

<h1 align="center">Pages - Cache</h1>

> What is cache? How can I use it and what is it used for... In the previous slide we showed how to create/read/modify
> files, but usually writing a file is a difficult operation. And when we changed a file we actually had to read change it
> and write it and so on and so on, that's why there is a cache. **`Cache or MME storage`**
> is used to link one file with an object that is stored in memory and thus we only need to read this file once and we are
> done, `Cache` or our file / file link lives as long as the server is running and therefore we need to ensure that the
> cache is written back to the File. Let's show you how to do it.

### Cache /w file

> #### Creating
> First we create a Json file and connect it to the cache using `link`
> ```applescript
> on script load:
>     new json file "<path-to-your-json>" if json file "<path-to-your-json>" doesn't > exist
>     link json file "<path-to-your-json>" as "myJson"
> ```
> now we have a linked file and json living in cachi.˙
>
> #### How can I work with `Cached Json` we can access it using `json <our json id>`
> ```applescript
>     send json "myJson"
>     # Or we can access all stored ones using
>     send all cached jsons
> ```
>
> Any change is made
> using [@Changer](https://github.com/SkJsonTeam/skJson/wiki/1.-Introduction#changing-setremoveremove-all) which we have
> already shown in [Introduction](https://github.com/SkJsonTeam/skJson/wiki/1.-Introduction)
>
>
>#### How do I save the cache back?
>```applescript
>save json "myJson"
>```
>this will guarantee that the json will be updated back in our file as well, or we can save all cached jsons using
>```applescript
>save all jsons
>```
> ㅤ

### Cache /wo file

> it's practically the same although there is no file assigned, so this `cache` is only partial because you can't save
> to a file using `save json "..."` And yet you can still modify and manipulate this json anyway. Except for saving it..
> if you wanted to save such a json you would have to use
> the `write effect` [@Writing](https://github.com/SkJsonTeam/skJson/wiki/4.-Files#writing)
>
> #### Creating
> ```applescript
> on script load:
> 	create new json storage named "json-storage"
> 	send json "json-storage"
> ```
> ㅤ

### Listening (Json Watcher)

> As we have linked the file above we can also link it here but with the proviso that this cache will be automatically
> restored the moment the file is changed in some way, in other words the file and the cache will always be identical if
> the file itself is manipulated
`link json file "<path to file>" as "mine.id" and make json watcher listen` How can we observe that the file has
> changed?
> By using the `on json watcher save` event
> ```applescript
> on script load:
>     link json file "<path to file>" as "mine.id" and make json watcher listen
> 
> on json watcher save:
>     if event-link is "mine.id":
>         send "CHANGED: %event-json%"
> ```
>  ㅤ
