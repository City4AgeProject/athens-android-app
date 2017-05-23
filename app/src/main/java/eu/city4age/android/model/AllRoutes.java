package eu.city4age.android.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lgiampouras on 2/3/15.
 */

public class AllRoutes implements Serializable {

    public List<Route> allRoutes;

    //Constuctor
    public AllRoutes() {
        allRoutes = new ArrayList<Route>();
    }

}
