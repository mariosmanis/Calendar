package com.unipi.p18196.callendar.Models;

import java.io.Serializable;

public class EventModel implements Serializable {

    // variables for storing our image and name.
    private String Title, Start_Time, End_Time, Members, Creator, Date, documentId, Comments, Comments_creator, Longitude, Latitude;

    public EventModel() {
        // empty constructor required for firebase.
    }

    public EventModel(String title, String start_Time, String end_Time, String members, String creator, String date, String documentId, String comments, String comments_creator, String longitude, String latitude) {
        Title = title;
        Start_Time = start_Time;
        End_Time = end_Time;
        Members = members;
        Creator = creator;
        Date = date;
        this.documentId = documentId;
        Comments = comments;
        Comments_creator = comments_creator;
        Longitude = longitude;
        Latitude = latitude;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getStart_Time() {
        return Start_Time;
    }

    public void setStart_Time(String start_Time) {
        Start_Time = start_Time;
    }

    public String getEnd_Time() {
        return End_Time;
    }

    public void setEnd_Time(String end_Time) {
        End_Time = end_Time;
    }

    public String getMembers() {
        return Members;
    }

    public void setMembers(String members) {
        Members = members;
    }

    public String getCreator() {
        return Creator;
    }

    public void setCreator(String creator) {
        Creator = creator;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getComments() {
        return Comments;
    }

    public void setComments(String comments) {
        Comments = comments;
    }

    public String getComments_creator() {
        return Comments_creator;
    }

    public void setComments_creator(String comments_creator) {
        Comments_creator = comments_creator;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }
}
