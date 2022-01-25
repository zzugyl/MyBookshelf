package com.smartjinyu.mybookshelf;

import java.util.List;

/**
 * created by wyj in 2022/1/24
 */
public class DouBanSearchResultByIsbn {

    private boolean success;
    private DataDTO data;
    private boolean is_cache;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public DataDTO getData() {
        return data;
    }

    public void setData(DataDTO data) {
        this.data = data;
    }

    public boolean isIs_cache() {
        return is_cache;
    }

    public void setIs_cache(boolean is_cache) {
        this.is_cache = is_cache;
    }

    public static class DataDTO {
        private String title;
        private String subtitle;
        private String original_title;
        private String id;
        private String isbn;
        private List<String> author;
        private List<String> translator;
        private String publish;
        private String producer;
        private String publishDate;
        private String pages;
        private String price;
        private String binging;
        private String series;
        private String book_intro;
        private String author_intro;
        private List<String> catalog;
        private List<String> original_texts;
        private List<String> labels;
        private String cover_url;
        private String url;
        private RatingDTO rating;
        private List<CommentsDTO> comments;
        private List<ReviewsDTO> reviews;
        private List<?> notes;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getOriginal_title() {
            return original_title;
        }

        public void setOriginal_title(String original_title) {
            this.original_title = original_title;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getIsbn() {
            return isbn;
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }

        public List<String> getAuthor() {
            return author;
        }

        public void setAuthor(List<String> author) {
            this.author = author;
        }

        public List<String> getTranslator() {
            return translator;
        }

        public void setTranslator(List<String> translator) {
            this.translator = translator;
        }

        public String getPublish() {
            return publish;
        }

        public void setPublish(String publish) {
            this.publish = publish;
        }

        public String getProducer() {
            return producer;
        }

        public void setProducer(String producer) {
            this.producer = producer;
        }

        public String getPublishDate() {
            return publishDate;
        }

        public void setPublishDate(String publishDate) {
            this.publishDate = publishDate;
        }

        public String getPages() {
            return pages;
        }

        public void setPages(String pages) {
            this.pages = pages;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getBinging() {
            return binging;
        }

        public void setBinging(String binging) {
            this.binging = binging;
        }

        public String getSeries() {
            return series;
        }

        public void setSeries(String series) {
            this.series = series;
        }

        public String getBook_intro() {
            return book_intro;
        }

        public void setBook_intro(String book_intro) {
            this.book_intro = book_intro;
        }

        public String getAuthor_intro() {
            return author_intro;
        }

        public void setAuthor_intro(String author_intro) {
            this.author_intro = author_intro;
        }

        public List<String> getCatalog() {
            return catalog;
        }

        public void setCatalog(List<String> catalog) {
            this.catalog = catalog;
        }

        public List<String> getOriginal_texts() {
            return original_texts;
        }

        public void setOriginal_texts(List<String> original_texts) {
            this.original_texts = original_texts;
        }

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        public String getCover_url() {
            return cover_url;
        }

        public void setCover_url(String cover_url) {
            this.cover_url = cover_url;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public RatingDTO getRating() {
            return rating;
        }

        public void setRating(RatingDTO rating) {
            this.rating = rating;
        }

        public List<CommentsDTO> getComments() {
            return comments;
        }

        public void setComments(List<CommentsDTO> comments) {
            this.comments = comments;
        }

        public List<ReviewsDTO> getReviews() {
            return reviews;
        }

        public void setReviews(List<ReviewsDTO> reviews) {
            this.reviews = reviews;
        }

        public List<?> getNotes() {
            return notes;
        }

        public void setNotes(List<?> notes) {
            this.notes = notes;
        }

        public static class RatingDTO {
            private int count;
            private String info;
            private double value;
            private double five_star_pre;
            private double four_star_pre;
            private double three_star_pre;
            private double two_star_pre;
            private double one_star_pre;

            public int getCount() {
                return count;
            }

            public void setCount(int count) {
                this.count = count;
            }

            public String getInfo() {
                return info;
            }

            public void setInfo(String info) {
                this.info = info;
            }

            public double getValue() {
                return value;
            }

            public void setValue(double value) {
                this.value = value;
            }

            public double getFive_star_pre() {
                return five_star_pre;
            }

            public void setFive_star_pre(double five_star_pre) {
                this.five_star_pre = five_star_pre;
            }

            public double getFour_star_pre() {
                return four_star_pre;
            }

            public void setFour_star_pre(double four_star_pre) {
                this.four_star_pre = four_star_pre;
            }

            public double getThree_star_pre() {
                return three_star_pre;
            }

            public void setThree_star_pre(double three_star_pre) {
                this.three_star_pre = three_star_pre;
            }

            public double getTwo_star_pre() {
                return two_star_pre;
            }

            public void setTwo_star_pre(double two_star_pre) {
                this.two_star_pre = two_star_pre;
            }

            public double getOne_star_pre() {
                return one_star_pre;
            }

            public void setOne_star_pre(double one_star_pre) {
                this.one_star_pre = one_star_pre;
            }
        }

        public static class CommentsDTO {
            private int vote;
            private String user_name;
            private String user_page;
            private int rating;
            private String date;
            private String content;

            public int getVote() {
                return vote;
            }

            public void setVote(int vote) {
                this.vote = vote;
            }

            public String getUser_name() {
                return user_name;
            }

            public void setUser_name(String user_name) {
                this.user_name = user_name;
            }

            public String getUser_page() {
                return user_page;
            }

            public void setUser_page(String user_page) {
                this.user_page = user_page;
            }

            public int getRating() {
                return rating;
            }

            public void setRating(int rating) {
                this.rating = rating;
            }

            public String getDate() {
                return date;
            }

            public void setDate(String date) {
                this.date = date;
            }

            public String getContent() {
                return content;
            }

            public void setContent(String content) {
                this.content = content;
            }
        }

        public static class ReviewsDTO {
            private String user_avator;
            private String user_name;
            private String user_page;
            private int rating;
            private String time;
            private String publisher;
            private String publisher_page;
            private String title;
            private String url;
            private String short_content;
            private int usefull_count;
            private int useless_count;
            private int reply_count;

            public String getUser_avator() {
                return user_avator;
            }

            public void setUser_avator(String user_avator) {
                this.user_avator = user_avator;
            }

            public String getUser_name() {
                return user_name;
            }

            public void setUser_name(String user_name) {
                this.user_name = user_name;
            }

            public String getUser_page() {
                return user_page;
            }

            public void setUser_page(String user_page) {
                this.user_page = user_page;
            }

            public int getRating() {
                return rating;
            }

            public void setRating(int rating) {
                this.rating = rating;
            }

            public String getTime() {
                return time;
            }

            public void setTime(String time) {
                this.time = time;
            }

            public String getPublisher() {
                return publisher;
            }

            public void setPublisher(String publisher) {
                this.publisher = publisher;
            }

            public String getPublisher_page() {
                return publisher_page;
            }

            public void setPublisher_page(String publisher_page) {
                this.publisher_page = publisher_page;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getShort_content() {
                return short_content;
            }

            public void setShort_content(String short_content) {
                this.short_content = short_content;
            }

            public int getUsefull_count() {
                return usefull_count;
            }

            public void setUsefull_count(int usefull_count) {
                this.usefull_count = usefull_count;
            }

            public int getUseless_count() {
                return useless_count;
            }

            public void setUseless_count(int useless_count) {
                this.useless_count = useless_count;
            }

            public int getReply_count() {
                return reply_count;
            }

            public void setReply_count(int reply_count) {
                this.reply_count = reply_count;
            }
        }
    }
}
