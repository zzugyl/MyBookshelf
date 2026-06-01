package com.smartjinyu.mybookshelf;

import java.util.List;

/**
 * Google Books API JSON response model
 * https://developers.google.com/books/docs/v1/reference/volumes
 */

public class GoogleBooksJson {

    /**
     * {
     *   "totalItems": 1,
     *   "items": [{
     *     "volumeInfo": {
     *       "title": "...",
     *       "authors": ["..."],
     *       "publisher": "...",
     *       "publishedDate": "2009-03",
     *       "pageCount": 200,
     *       "imageLinks": {"thumbnail": "..."},
     *       "industryIdentifiers": [{"type": "ISBN_13", "identifier": "..."}],
     *       "infoLink": "..."
     *     }
     *   }]
     * }
     */

    private int totalItems;
    private List<ItemBean> items;

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public List<ItemBean> getItems() {
        return items;
    }

    public void setItems(List<ItemBean> items) {
        this.items = items;
    }

    public static class ItemBean {
        private VolumeInfoBean volumeInfo;

        public VolumeInfoBean getVolumeInfo() {
            return volumeInfo;
        }

        public void setVolumeInfo(VolumeInfoBean volumeInfo) {
            this.volumeInfo = volumeInfo;
        }
    }

    public static class VolumeInfoBean {
        private String title;
        private List<String> authors;
        private String publisher;
        private String publishedDate;
        private int pageCount;
        private ImageLinksBean imageLinks;
        private List<IndustryIdentifierBean> industryIdentifiers;
        private String infoLink;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<String> getAuthors() {
            return authors;
        }

        public void setAuthors(List<String> authors) {
            this.authors = authors;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public String getPublishedDate() {
            return publishedDate;
        }

        public void setPublishedDate(String publishedDate) {
            this.publishedDate = publishedDate;
        }

        public int getPageCount() {
            return pageCount;
        }

        public void setPageCount(int pageCount) {
            this.pageCount = pageCount;
        }

        public ImageLinksBean getImageLinks() {
            return imageLinks;
        }

        public void setImageLinks(ImageLinksBean imageLinks) {
            this.imageLinks = imageLinks;
        }

        public List<IndustryIdentifierBean> getIndustryIdentifiers() {
            return industryIdentifiers;
        }

        public void setIndustryIdentifiers(List<IndustryIdentifierBean> industryIdentifiers) {
            this.industryIdentifiers = industryIdentifiers;
        }

        public String getInfoLink() {
            return infoLink;
        }

        public void setInfoLink(String infoLink) {
            this.infoLink = infoLink;
        }
    }

    public static class ImageLinksBean {
        private String thumbnail;

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }
    }

    public static class IndustryIdentifierBean {
        private String type;
        private String identifier;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
    }
}
