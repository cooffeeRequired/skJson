<p align="center" style="align: center; text-align: center">
<img align="center" alt="SkJson" width="40%" src="https://skjson.xyz/imgs/skjson/banner.png">
</p>

<h1 align="center">Pages - WebRequests</h1>

>What are and how do webRequests work in `SkJson` how can I use them, what is possible and what methods work

## WebRequest
> We can specify the Body and Header for a given web request, we can also choose between `asynchronous` processing and `synchronous` processing.
> ### 1. Async & Wait
> * Allows to run `asynchronous` run, i.e. the web request will be executed in the background, but the result will be available almost immediately. Inside the section `execute a new GET request to "https://dummyjson.com/products/2" and save it to {_data}` we will perform the pairing the response content will be saved to `{_content}`, of course we can pair multiple response values not only content, there are `content, status code, url, headers`\
**Recommended** for getting data from the website.
>
> ```applescript
> make new GET request to "https://dummyjson.com/products/2" and store it in {_data}:
>     content: {_content}
> execute {_data} and wait
> send {_content}
> ```
> ### 2. Async
> * Allows us to run an `asynchronous` request to the web and again this request will be run in the background but the difference is that the data from this request will not be available immediately but will be available only when the request is completed.As in the previous example in the `save` section we will perform the pairing the response content will be saved to `{-content}` and others values what we describe before. However, here we have a change. Let's take a look at it. In addition we have `save incorrect response (boolean)`, `lenient (boolean)`, `headers (string|json)`, `content (string|json)`.
if it is not in the `save` section it means that it is the data we set for `request`, i.e. what we want to send to the given `URL`, also `save incorect response` allows us to store also corrupted json or response that is not json type at all, for example `HTML, YML` and so on. Last but not least, `lenient` allows us to automatically fix corrupted json either in the `save` section and the `content` value or in our `contet` for the request
> 
> ```applescript
> on script load:
>     async make POST request to "https://dummyjson.com/carts/add":
>         headers: "Content-Type: application/json"
>         content: json from text "{userId: 1, products: [{id: 1, quantity: 1}, {id: 50, > quantity: 2}]}"
>         save incorrect response: true
>         lenient: true
>         save:
>             content: {-content}
>             headers: {-header}
>             status code: {-code}
>             url: {-url}
> ```
>
> ### 3. Sync
> * It is exactly the same as [2.Asynch](#2-async), the only difference is that the whole server waits for a response from the server, so recommended only for low latency web servers up to 30ms max, or also only when you really know you need synchronous\
>.
>

## WebHooks / Discord Hooks
> What is Webhooks => Webhook is a web address (URL) that you can set in the administration and the system sends messages / data to this address according to the selected rules. In the webhook settings you define the URL, the data format and the event to which the data is sent. The system takes care of the rest.
> ### 1. Discord
> * For discord we can send files, embeds, we have available settings 
> * `header (string|json)`
> * `attachment | attachments`
> * * If it is an `attachment` then only one file can be attached! but if it's `attachments` we can attach as many files as we want using key: value
> * * * `1: ./test.sk`
> * * * `2: ./test2.sk` 
> * `data (nested format)`
> * * `username (string)` 
> * * `avatar-url (string)`
> * * `tts (boolean)` 
> * * `content (string ! Cannot be null, shall be empty)`
> * * `embed (nested format)`
> * * * `id (int|null|auto)` 
> * * * `fields (string-json)`
> * * * `title (string)`
> * * * `thumbnail (nested json | json)`
> * * * `color (hex color. e.g. ##21a7c2)`
> ### 2. Web
> * For web we can send files, we have available settings
> * `attachment | attachments`
> * * If it is an `attachment` then only one file can be attached! but if it's `attachments` we can attach as many files as we want using key: value
> * `content | contents`
> * * If it is an `content` then only one line can be posted! but if it's `contents` we can post as many lined as we want using key: value
> * * * `1: This is random text`
> * * * `2: This is a random text 2`
> * `header (string|json)`
> ### 3. Examples
>```applescript
>command web:
>    trigger:
>        async send web request "https://webhook.site/>4e2e350b-4a8f-4863-85c5-e833e4ec110b":
>            attachments:
>                1: "C:\\Users\\nexti\\Documents\\Lekce\\index.html"
>            content: "{fromSkJson: '?', ?: true}"
>            
>            
>command without-embed:
>    trigger:
>        async send discord request "https://discord.com/api/webhooks/>1128075537919770798/>y78NK-odks6Lod5kimmhcd9YWQfhFzPU1YA-VyD5bqWMGxaeYXxp5jTxpnNI9Yhw1Rgt":
>            header: "Content-Type: application/json"
>            data:
>                tts: true
>                content: "{'payload:' true}" # this can be any json encoded string or >json
>            
>            
>command embed:
>    trigger:
>        async send discord request "https://discord.com/api/webhooks/>1128075537919770798/>y78NK-odks6Lod5kimmhcd9YWQfhFzPU1YA-VyD5bqWMGxaeYXxp5jTxpnNI9Yhw1Rgt":
>            header: "Content-Type: application/json"
>            data:
>                username: "AAAA"
>                avatar-url: "https://google.com"
>                tts: true
>                content: "" # content never can be empty, so when you want to send >only embed, you need to put here empty string
>                embed:
>                    id: 102018 # when you put here null, or auto, the value will be >generated automatically.
>                    fields: "{}"
>                    author: "{name: 'CoffeeRequired'}"
>                    title: "Hello there"
>                    thumbnail: "{url: 'https://cravatar.eu/helmhead/_F0cus__/600.png'}"
>                    color: "##21a7c2" # that support all hex colors.. not minecraft
>            
>command embedAtt:
>    trigger:
>        async send discord request "https://discord.com/api/webhooks/>1128075537919770798/>y78NK-odks6Lod5kimmhcd9YWQfhFzPU1YA-VyD5bqWMGxaeYXxp5jTxpnNI9Yhw1Rgt":
>            attachments:
>                1: "*/generate_doc.sk" # star means the parser will search for the >file recursively from the root directory
>            data:
>                tts: false
>                content: "hello from attachments"
>```