/*
 * Last modified: 30.10.18 12:19
 * Copyright (c) OpenDrone, 2018.  All rights reserved.
 * The entire project (including this file) is licensed under the GNU GPL v3.0
 */

package at.opendrone.opendrone.flightplan;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class Flightplan implements Serializable {
    private int id;
    private String name;
    private String description;
    public LinkedList<GeoPoint> points;

    public Flightplan() {
    }

    public Flightplan(int id, String name, String desc, LinkedList<GeoPoint> points) {
        this.name = name;
        this.description = desc;
        this.id = id;
        this.points = points;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public LinkedList<GeoPoint> getCoordinates() {
        return points;
    }

    public void setCoordinates(LinkedList<GeoPoint> points) {
        this.points = points;
    }
}
