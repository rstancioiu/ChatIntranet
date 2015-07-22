package model;

public class User {
    private String lastname;
    private String description;
    
    public User(String lastname, String description) {
        this.lastname=lastname;
        this.description=description;
    }
    public User(String lastname) {
        this.lastname=lastname;
        description=null;
    }
    
    String getNom() {
        return lastname;
    }
    String getDescription() {
        return description;
    }
}
