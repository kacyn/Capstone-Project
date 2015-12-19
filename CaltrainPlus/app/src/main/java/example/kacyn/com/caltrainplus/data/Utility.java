package example.kacyn.com.caltrainplus.data;

/**
 * Created by kacyn on 12/19/15.
 */
public class Utility {

    public static double getLatFromStopCode(int stopCode) {
        switch(stopCode) {
            case 70011: //SF
                return 37.776552;
            case 70021: //22nd
                return 37.757548;
            case 70031: //Bayshore
                return 37.708751;
            case 70041: //So SF
                return 37.6559832;
            case 70051: //San Bruno
                return 37.6309323;
            case 70061: //Millbrae
                return 37.599223;
            case 70081: //Burlingame
                return 37.580246;
            case 70091: //San Mateo
                return 37.567616;
            case 70101: //Hayward Park
                return 37.5525849;
            case 70111: //Hillsdale
                return 37.538995;
            case 70112: //Belmont
                return 37.520713;
            case 70131: //San Carlos
                return 37.507621;
            case 70141: //Redwood City
                return 37.4854555;
            case 70161: //Menlo Park
                return 37.454382;
            case 70171: //Palo Alto
                return 37.443777;
            case 70191: //Cal ave
                return 37.42952;
            case 70201: //San Antonio
                return 37.4072274;
            case 70211: //Mt view
                return 37.3945523;
            case 70221: //Sunnyvale
                return 37.378613;
            case 70231: //Lawrence
                return 37.3704117;
            case 70241: //Santa Clara
                return 37.3529026;
            case 70251: //College Park
                return 37.3425991;
            case 70261: //San Jose Diridon
                return 37.3306668;
            case 70271: //Tamien
                return 37.3119275;
            default:
                return -1;
        }
    }

    public static double getLngFromStopCode(int stopCode) {
        switch(stopCode) {
            case 70011: //SF
                return -122.395387;
            case 70021: //22nd
                return -122.3927918;
            case 70031: //Bayshore
                return -122.401522;
            case 70041: //So SF
                return -122.4052182;
            case 70051: //San Bruno
                return -122.4120296;
            case 70061: //Millbrae
                return -122.3867732;
            case 70081: //Burlingame
                return -122.345145;
            case 70091: //San Mateo
                return -122.4120296;
            case 70101: //Hayward Park
                return -122.3090312;
            case 70111: //Hillsdale
                return -122.2979067;
            case 70112: //Belmont
                return -122.275574;
            case 70131: //San Carlos
                return -122.260396;
            case 70141: //Redwood City
                return -122.2319727;
            case 70161: //Menlo Park
                return -122.18202;
            case 70171: //Palo Alto
                return -122.1649696;
            case 70191: //Cal ave
                return -122.142258;
            case 70201: //San Antonio
                return -122.1071158;
            case 70211: //Mt view
                return -122.0760376;
            case 70221: //Sunnyvale
                return -122.030683;
            case 70231: //Lawrence
                return -121.9959844;
            case 70241: //Santa Clara
                return -121.9368448;
            case 70251: //College Park
                return -121.9155766;
            case 70261: //San Jose Diridon
                return -121.9023781;
            case 70271: //Tamien
                return -121.8848087;
            default:
                return -1;
        }
    }

}
