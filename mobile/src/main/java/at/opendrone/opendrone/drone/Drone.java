package at.opendrone.opendrone.drone;

import java.io.Serializable;

class Drone implements Serializable {

    private long id;
    private String name;
    private String description;
    private String type;
    private String imageURI;

    public Drone() {
    }

    public Drone(String name, String desc, String type) {
        this(name,desc,type,"");
    }

    public Drone(String name, String desc, String type, String imageURI) {
        this.id = System.currentTimeMillis();
        this.name = name;
        this.description = desc;
        this.type = type;
        this.imageURI = imageURI;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }
}