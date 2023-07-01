const pages = {
    "get-started": {
        title: 'Get Started',
        color: '#0000',
        html: `
        <div class="container-page-docs">
            <p> something nice </p>
            #code
        </div>
        `,
        codes: [
            {
                't': 'javascript',
                'code': 
                `
                    function greeting(name) {
                        console.log('Hello, ' + name + '!');
                    }
                
                    greeting('John');
                `
            }
        ]
    },
    "about-coder": {
        title: 'About coder',
        color: '#0000',
        html: `
        <div class="container-page">
            <p>My name is Jiri, I'm 24 years old. and for a long time I programmed in PHP. about a year ago I started to be interested in other languages such as Ruby, Java, C# and in addition at that time I was the owner of one of the bigger CZ/SK minecraft server so I was constantly digging in plugins until one day I came across the plugin Skript and I started to use it after a while I got the idea to create my own plugin and it was just SkJson. now they are working on other projections...</p>
            <h4>Where am I from?</h4>
            map here
            Discord
        </div>`,
    },
    "about-skjson": {
        title: 'About SkJson',
        color: '#0000',
        html: `
        <div class="container-page">
            <p>SkJson is a library for working with JSON in Skript,
            it supports all known versions of minecraft as well as server types (Paper, Purpur, Spigot).<p>
            <p>With SkJson you can easily read and write JSON data, traverse JSON structures and perform various operations on JSON objects and arrays.</p>
            <p>SkJson is designed to be lightweight and efficient, making it a great choice for working with JSON data in scripts. It was created last year.</p>
            <p>It is primarily an add-on to the well-known Skript plugin.</p>
        </div>`,
    }
}

// main page.html
// Sidebar toggle functionality

function changeSelectedPage(pageCH=String) {
    const page = document.getElementById('page-content');
    const pageTitle = document.getElementById('page-name');
    if(pages[pageCH] != null) {
        pageTitle.innerHTML = pages[pageCH].title
        if (pageCH === 'get-started') {
            let cs = pages[pageCH].codes
            if (cs.length != 0) {
                // const pagedocs = document.querySelectorAll('.container-page-docs')
                // const pre = document.createElement('pre')
                // const code = document.createElement('code')

                // pagedocs.appendChild(pre)
                // pagedocs.appendChild(code)

            }

        }
        page.innerHTML = pages[pageCH].html
    }
}

const articleData = []

document.addEventListener('DOMContentLoaded', () => {

    const sidebarToggle = document.getElementById('sidebar-toggle');
    const sidebar = document.getElementById('sidebar');
    const sidebarItems = sidebar.querySelectorAll('li');

    sidebarToggle.addEventListener('click', () => {
        sidebar.classList.toggle('active');
        sidebarToggle.style.display = 'none';
    });

    // Close sidebar when clicked outside
    window.addEventListener('click', (event) => {
        if (!event.target.closest('.sidebar') && !event.target.closest('.sidebar-toggle')) {
            sidebar.classList.remove('active');
            sidebarToggle.style.display = 'flex';
        }
    });

    // Handle click events for sidebar items
    sidebarItems.forEach((item) => {
        const subList = item.querySelector('.sub-list');
        const link = item.querySelector('a');

        link.addEventListener('click', (event) => {
            event.preventDefault();
            if (subList) {
                subList.classList.toggle('active');
            }
        });
    });
});

function addArticle(title, image, url, text) {
    articleData.push(
    {
        title: title,
        image: image,
        text: text,
        url: url
    })
}

// Zaregistrujeme událost kliknutí na všechny odkazy "Read More"
document.addEventListener('DOMContentLoaded', () => {
    const readMoreLinks = document.querySelectorAll('.article-read-more');
    readMoreLinks.forEach((link) => {
        link.addEventListener('click', expandArticle);
    });

    getData().then(() => {
        loadArticles()
    })
});

function loadArticles() {
    const articlesContainer = document.querySelector('.articles-container');
    const art = document.getElementById('article-latest');
    if (articleData.length === 0) {
        art.classList.remove("article-latest")
        art.classList.add("article-latest-notfound")
        art.textContent = "Nothing here"
    } else {
        // change article-latest
        art.classList.add("article-latest")
        art.classList.remove("article-latest-notfound")
        art.textContent = "Latest"
        

        // Create article elements and append them to the container
        articleData.forEach((article) => {
            const articleElement = document.createElement('div');
            articleElement.classList.add('article');

            const articleImage = document.createElement('img');
            articleImage.src = article.image;
            articleImage.alt = 'Article Image';

            const articleContent = document.createElement('div');
            articleContent.classList.add('article-content');

            const articleTitle = document.createElement('h2');
            articleTitle.classList.add('article-title');
            articleTitle.textContent = article.title;

            const articleText = document.createElement('p');
            articleText.classList.add('article-text');
            articleText.textContent = article.text;

            const articleReadMore = document.createElement('a');
            articleReadMore.classList.add('article-read-more');
            articleReadMore.textContent = 'Read more on github';
            articleReadMore.href = article.url;

            articleContent.appendChild(articleTitle);
            articleContent.appendChild(articleText);
            articleContent.appendChild(articleReadMore);

            articleElement.appendChild(articleImage);
            articleElement.appendChild(articleContent);

            articlesContainer.appendChild(articleElement);
        });
    }
}

function cleanMD(mdString, maxSize) {
    const converter = new showdown.Converter();
    let cleanText = converter.makeHtml(mdString);;
    let cleanen = cleanText.replace(/<\/?[^>]+(>|$)/g, '');
    if (cleanen.length >= 100) cleanen = cleanen.slice(0, maxSize);
    cleanen = cleanen.replace(/<code>/g, '&lt;code&gt;');
    cleanen = cleanen.replace(/<\/code>/g, '&lt;/code&gt;');
    return cleanen;
}



async function getData() {
    let static_logo =  "assets/fav.png"
    let max_lengh  = 6;
    let array = []
    fetch('https://api.github.com/repos/SkJsonTeam/SkJson/releases', {
        method: 'GET',
        headers: {
            'Accept': 'application/vnd.github+json'
        }
    }).then(rsp => rsp.json()).then(json => {
        for (var i = 0; i < max_lengh; i++) {
            let j = json[i]
            addArticle
            (
                j['tag_name'],
                static_logo,
                j['html_url'],
                cleanMD(j['body'], 200)
            )
        }
        loadArticles()
    })
}