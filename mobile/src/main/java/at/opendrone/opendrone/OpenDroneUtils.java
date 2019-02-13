/*
 * Last modified: 23.10.18 08:23
 * Copyright (c) OpenDrone, 2018.  All rights reserved.
 * The entire project (including this file) is licensed under the GNU GPL v3.0
 */

package at.opendrone.opendrone;

public class OpenDroneUtils {
    public static String SP_POINTS = "sp_points";
    public static final int RQ_GPS = 7;
    public static String SP_FLIGHTPLANS = "sp_flightplans";
    //public static String SP_FLIGHTPLAN_HOLDER = "sp_flightplan";
    public static String SP_FLIGHTPLAN_NAME = "sp_flightplan_name";
    public static String SP_FLIGHTPLAN_DESC = "sp_flightplan_desc";
    public static String SP_FLIGHTPLAN_POSITION = "sp_flightplan_pos";
    public static String SP_SETTINGS_LANGUAGE = "sp_settings_language";
    public static String SP_SETTINGS_PROMODE = "sp_settings_promode";
    public static String SP_SETTINGS_MAXHEIGHT = "sp_settings_maxHeight";
    public static final double DEFAULT_LAT = 48.2468036;
    public static final double DEFAULT_LNG = 14.6199875;

    public static final int LF_HOME = 0;
    public static final int LF_DRONE = 1;
    public static final int LF_FP = 2;
    public static final int LF_FLY = 3;
    public static final int LF_SETTINGS = 4;
    public static final int LF_ADJUST_PID = 5;

    public static final int CODE_THROTTLE_UP= 1;
    public static final int CODE_THROTTLE = 1;
    public static final int CODE_THROTTLE_DOWN= 2;
    public static final int CODE_YAW_RIGHT= 3;
    public static final int CODE_YAW_LEFT= 4;
    public static final int CODE_PITCH_FORWARD= 5;
    public static final int CODE_PITCH_BACKWARD= 6;
    public static final int CODE_ROLL_LEFT= 7;
    public static final int CODE_ROLL_RIGHT= 8;
    public static final int CODE_GO_HOME= 9;
    public static final int CODE_ABORT= 10;
    public static final int CODE_ARM = 30;
    public static final int CODE_CALIBRATE = 20;

    public static final int CODE_PID_P = 333;
    public static final int CODE_PID_I = 334;
    public static final int CODE_PID_D = 335;


    public static final int CODE_LOCAL_FAIL = -1;
    public static final int CODE_CONTROLLER_TEMP = 1;
    public static final int CODE_AIR_TEMP = 2;
    public static final int CODE_POSITTION = 3;
    public static final int CODE_PRESSURE = 4;
    public static final int CODE_HEIGHT = 5;
    public static final int CODE_STATUS = 6;
    public static final int CODE_VELOCITY = 7;
    public static final int CODE_ERROR = 255;

    public static final int CODE_CALIBRATE_START = 1;
}
