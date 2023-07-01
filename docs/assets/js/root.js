// main page.html
function saveDataAsJSON(data) {
    var jsonData = JSON.stringify(data);
    var downloadLink = document.createElement('a');
    downloadLink.href = 'data:text/json;charset=utf-8,' + encodeURIComponent(jsonData);
    downloadLink.download = 'data.json';
    document.body.appendChild(downloadLink);
    downloadLink.click();
    document.body.removeChild(downloadLink);
  }

// Sidebar toggle functionality
const articleData = [
]
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

// Funkce pro rozšiřování článků
function expandArticle(event) {
    const articleContent = event.target.parentNode;
    articleContent.classList.toggle('article-expanded');
}

// Zaregistrujeme událost kliknutí na všechny odkazy "Read More"
document.addEventListener('DOMContentLoaded', () => {
    const readMoreLinks = document.querySelectorAll('.article-read-more');
    readMoreLinks.forEach((link) => {
        link.addEventListener('click', expandArticle);
    });
});

function loadArticles() {
    const articlesContainer = document.querySelector('.articles-container');
    if (articleData.length === 0) {

    } else {
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
            articleReadMore.textContent = 'Read More';
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


// Call the loadArticles function to dynamically load articles
document.addEventListener('DOMContentLoaded', () => {
    addArticle("2.8.6", "../assets/fav.png", "https://github.com/SkJsonTeam/skJson/releases/tag/2.8.6", "Fixed nulls elements by (SET/ADD).Added effect for load json files from folder");
    addArticle("2.8.5", "../assets/fav.png", "https://github.com/SkJsonTeam/skJson/releases/tag/2.8.5", "Fixed java issue and file writing");
    loadArticles();
    saveDataAsJSON(articleData)
});