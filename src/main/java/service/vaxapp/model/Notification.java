package service.vaxapp.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private int type;//1=appointment completion; 2=question answered; 3=question unanswered; 4=appointment unapproved;

    @Column
    private String message;

    @Column
    private String url;

    @Column(nullable = false)
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean isRead = false;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "time")
    private LocalTime time;

    // Bidirectional many-to-one relationship (A user may have multiple notifications)
    @ManyToOne
    private User userToNotify;

    public Notification() {
    }

    public Notification(String message, String url, int type, LocalDate date, LocalTime time, User userToNotify) {
        this.message = message;
        this.url = url;
        this.type = type;
        this.date = date;
        this.time = time;
        this.userToNotify = userToNotify;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public User getUserToNotify() {
        return userToNotify;
    }

    public void setUserToNotify(User userToNotify) {
        this.userToNotify = userToNotify;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}